package com.carpercreative.projectglitch.item

import com.carpercreative.projectglitch.ProjectGlitch
import com.carpercreative.projectglitch.Storage
import com.carpercreative.projectglitch.cancer.BlobIdentifier
import com.carpercreative.projectglitch.cancer.CancerBlob
import com.carpercreative.projectglitch.persistence.BlobMembershipPersistentState.Companion.getBlobMembershipPersistentState
import kotlin.math.sqrt
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.NbtInt
import net.minecraft.nbt.NbtOps
import net.minecraft.registry.RegistryKey
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.GlobalPos
import net.minecraft.world.World
import org.slf4j.LoggerFactory

class ScannerItem(
	settings: Settings,
) : Item(settings) {
	override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
		val stack = user.getStackInHand(hand)

		if (!world.isClient) {
			cycleTrackedCancerBlob(stack, user)
		}

		return TypedActionResult.success(stack)
	}

	override fun useOnBlock(context: ItemUsageContext): ActionResult {
		val player = context.player
		if (!context.world.isClient && player != null) {
			val stack = player.getStackInHand(context.hand)
			cycleTrackedCancerBlob(stack, player)
		}

		return ActionResult.SUCCESS
	}

	override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
		if (world.isClient) return
		world as ServerWorld

		if (world.time % 20 == 0L) {
			trackNearestCancerousBlock(stack, entity)
		}
	}

	override fun allowNbtUpdateAnimation(player: PlayerEntity?, hand: Hand?, oldStack: ItemStack?, newStack: ItemStack?): Boolean {
		return false
	}

	// TODO: highlight researched cancerous blocks on use

	companion object {
		private val LOGGER = LoggerFactory.getLogger(ScannerItem::class.java)

		private const val KEY_TRACKED_CANCER_BLOB = "${ProjectGlitch.MOD_ID}:tracked_cancer_blob"
		private const val KEY_TRACKED_POSITION = "${ProjectGlitch.MOD_ID}:tracked_position"
		private const val KEY_TRACKED_WORLD = "${ProjectGlitch.MOD_ID}:tracked_world"

		fun getTrackedCancerBlob(stack: ItemStack): BlobIdentifier? {
			return (stack.nbt?.get(KEY_TRACKED_CANCER_BLOB) as? NbtInt)?.intValue()
		}

		fun setTrackedCancerBlob(stack: ItemStack, blobId: BlobIdentifier?) {
			if (blobId != null) {
				stack.getOrCreateNbt().putInt(KEY_TRACKED_CANCER_BLOB, blobId)
			} else {
				stack.nbt?.remove(KEY_TRACKED_CANCER_BLOB)
			}
		}

		fun cycleTrackedCancerBlob(stack: ItemStack, backwards: Boolean): Boolean {
			val currentBlobId = getTrackedCancerBlob(stack)
			val predicate: (cancerBlob: CancerBlob) -> Boolean = when (currentBlobId) {
				null -> ({ it.isAnalyzed && it.isActive })
				else -> when (backwards) {
					false -> ({ it.id > currentBlobId && it.isAnalyzed && it.isActive })
					true -> ({ it.id < currentBlobId && it.isAnalyzed && it.isActive })
				}
			}

			val values = Storage.cancerBlob.getCancerBlobs().values
			val nextBlobId = (if (!backwards) values.firstOrNull(predicate) else values.lastOrNull(predicate))?.id
			setTrackedCancerBlob(stack, nextBlobId)

			return currentBlobId != nextBlobId
		}

		fun cycleTrackedCancerBlob(stack: ItemStack, player: PlayerEntity) {
			val trackedBlobChanged = cycleTrackedCancerBlob(stack, player.isSneaking)

			trackNearestCancerousBlock(stack, player)

			if (trackedBlobChanged) {
				playModeChangedSound(player)
			}
		}

		fun isTracking(stack: ItemStack): Boolean {
			return stack.nbt?.run {
				contains(KEY_TRACKED_POSITION, NbtElement.COMPOUND_TYPE.toInt())
				contains(KEY_TRACKED_WORLD)
			} == true
		}

		fun getTrackedPosition(stack: ItemStack): BlockPos {
			return stack.nbt!!.getCompound(KEY_TRACKED_POSITION).let(NbtHelper::toBlockPos)
		}

		fun getTrackedWorld(stack: ItemStack): RegistryKey<World> {
			val worldKeyNbt = stack.nbt!!.get(KEY_TRACKED_WORLD)
			return World.CODEC.parse(NbtOps.INSTANCE, worldKeyNbt).result().get()
		}

		fun getTrackedGlobalPosition(stack: ItemStack): GlobalPos? {
			if (!isTracking(stack)) return null

			val blockPos = getTrackedPosition(stack)
			val worldKey = getTrackedWorld(stack)
			return GlobalPos.create(worldKey, blockPos)
		}

		fun setTrackedPosition(stack: ItemStack, worldKey: RegistryKey<World>, blockPos: BlockPos) {
			stack.getOrCreateNbt().apply {
				put(KEY_TRACKED_POSITION, NbtHelper.fromBlockPos(blockPos))
				World.CODEC.encodeStart(NbtOps.INSTANCE, worldKey)
					.resultOrPartial(LOGGER::error)
					.ifPresent { nbtElement -> put(KEY_TRACKED_WORLD, nbtElement) }
			}
		}

		fun clearTrackedPosition(stack: ItemStack) {
			stack.nbt?.apply {
				remove(KEY_TRACKED_POSITION)
				remove(KEY_TRACKED_WORLD)
			}
		}

		fun trackNearestCancerousBlock(stack: ItemStack, entity: Entity) {
			val world = entity.world
			if (world.isClient) return
			world as ServerWorld

			val blobMembershipPersistentState = world.getBlobMembershipPersistentState()
			val blobId = getTrackedCancerBlob(stack)
			val predicate: (cancerBlob: CancerBlob) -> Boolean = when (blobId) {
				null -> ({ !it.isAnalyzed })
				else -> ({ it.id == blobId })
			}
			val trackedPosition = blobMembershipPersistentState.getNearestMemberOrNull(entity.blockPos, predicate)
			if (trackedPosition != null) {
				setTrackedPosition(stack, world.registryKey, trackedPosition)
			} else {
				clearTrackedPosition(stack)

				if (blobId != null && !Storage.cancerBlob.getCancerBlobById(blobId).isActive) {
					// Stop tracking a blob once it's been defeated.
					setTrackedCancerBlob(stack, null)

					playModeChangedSound(entity)
				}
			}

			if (entity is ServerPlayerEntity && entity.isHolding { it == stack }) {
				entity.sendMessage(
					when (blobId) {
						null -> Text.translatable("${stack.item.translationKey}.tracking_nearest_unknown")
						else -> Text.translatable(
							"${stack.item.translationKey}.tracking_known",
							trackedPosition?.getSquaredDistance(entity.pos)?.let(::sqrt)?.let { "%.1f".format(it) } ?: "?",
						)
					},
					true,
				)
			}
		}

		private val MODE_CHANGED_SOUND = SoundEvent.of(ProjectGlitch.identifier("item.scanner.mode_changed"))

		private fun playModeChangedSound(entity: Entity) {
			entity.world.playSound(
				null,
				entity.x, entity.y, entity.z,
				MODE_CHANGED_SOUND,
				if (entity is PlayerEntity) SoundCategory.PLAYERS else SoundCategory.HOSTILE,
				1f, 1f,
			)
		}
	}
}