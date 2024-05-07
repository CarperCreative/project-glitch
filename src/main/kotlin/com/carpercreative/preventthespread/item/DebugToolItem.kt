package com.carpercreative.preventthespread.item

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.Storage
import com.carpercreative.preventthespread.cancer.CancerLogic
import com.carpercreative.preventthespread.cancer.CancerType
import com.carpercreative.preventthespread.persistence.BlobMembershipPersistentState.Companion.getBlobMembershipPersistentState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class DebugToolItem(
	settings: Settings,
) : Item(settings) {
	enum class DebugMode(
		val translationKey: String,
	) {
		INSPECT("inspect"),
		CREATE_CANCER("createCancer"),
		HASTEN_SPREAD("hastenSpread"),
	}

	override fun getTranslationKey(stack: ItemStack): String {
		return super.getTranslationKey(stack) + "." + getDebugMode(stack).translationKey
	}

	override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
		if (world.isClient) return TypedActionResult.success(user.getStackInHand(hand))
		world as ServerWorld

		val stack = user.getStackInHand(hand)

		if (user.isSneaking) {
			setNextDebugMode(stack)
			return TypedActionResult.success(user.getStackInHand(hand))
		}

		return TypedActionResult.pass(stack)
	}

	override fun useOnBlock(context: ItemUsageContext): ActionResult {
		val world = context.world
		if (world.isClient) return ActionResult.SUCCESS
		world as ServerWorld

		val stack = context.stack
		val debugMode = getDebugMode(stack)

		return when (debugMode) {
			DebugMode.INSPECT -> {
				val blobMembershipPersistentState = world.getBlobMembershipPersistentState()

				val cancerBlobId = blobMembershipPersistentState.getMembershipOrNull(context.blockPos)
				val cancerBlob = cancerBlobId?.let { Storage.cancerBlob.getCancerBlobById(it) }

				context.player?.sendMessage(Text.literal("Identifier of blob by membership: $cancerBlobId\nBlob from store: ${cancerBlob?.let { "type = ${it.type}" }}"))

				ActionResult.SUCCESS
			}
			DebugMode.CREATE_CANCER -> {
				if (context.player?.isCreativeLevelTwoOp != true) return ActionResult.FAIL

				val cancerBlob = CancerLogic.createCancerBlob(world, context.blockPos, maxSize = 7, CancerType.entries.random())

				if (cancerBlob == null) {
					context.player?.sendMessage(Text.literal("Failed to create new cancer blob. Can cancer spread to the target block? Does the target block already have a cancer membership?"))
					ActionResult.FAIL
				} else {
					ActionResult.SUCCESS
				}
			}
			DebugMode.HASTEN_SPREAD -> {
				CancerLogic.hastenSpread(world, context.blockPos, world.random)
				ActionResult.SUCCESS
			}
		}
	}

	companion object {
		private const val KEY_DEBUG_MODE = "${PreventTheSpread.MOD_ID}:debugMode"

		fun getDebugMode(stack: ItemStack): DebugMode {
			return try {
				stack.nbt
					?.getString(KEY_DEBUG_MODE)
					?.let(DebugMode::valueOf)
			} catch (_: IllegalArgumentException) { null }
				?: DebugMode.INSPECT
		}

		fun setDebugMode(stack: ItemStack, debugMode: DebugMode) {
			val nbt = stack.getOrCreateNbt()
			nbt.putString(KEY_DEBUG_MODE, debugMode.name)
		}

		fun setNextDebugMode(stack: ItemStack): DebugMode {
			val currentDebugMode = getDebugMode(stack)
			val nextDebugMode = DebugMode.entries[(currentDebugMode.ordinal + 1) % DebugMode.entries.size]
			setDebugMode(stack, nextDebugMode)
			return nextDebugMode
		}
	}
}
