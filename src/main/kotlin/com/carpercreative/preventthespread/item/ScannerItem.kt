package com.carpercreative.preventthespread.item

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.persistence.BlobMembershipPersistentState.Companion.getBlobMembershipPersistentState
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.NbtOps
import net.minecraft.registry.RegistryKey
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.GlobalPos
import net.minecraft.world.World
import org.slf4j.LoggerFactory

class ScannerItem(
	settings: Settings,
) : Item(settings) {
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

		private const val KEY_TRACKED_POSITION = "${PreventTheSpread.MOD_ID}:tracked_position"
		private const val KEY_TRACKED_WORLD = "${PreventTheSpread.MOD_ID}:tracked_world"

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
			val trackedPosition = blobMembershipPersistentState.getNearestMemberOrNull(entity.blockPos) { true }
			if (trackedPosition != null) {
				setTrackedPosition(stack, world.registryKey, trackedPosition)
			} else {
				clearTrackedPosition(stack)
			}
		}
	}
}
