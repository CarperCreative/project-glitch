package com.carpercreative.preventthespread.item

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.cancer.BlobIdentifier
import com.carpercreative.preventthespread.cancer.CancerLogic.isCancerous
import com.carpercreative.preventthespread.persistence.BlobMembershipPersistentState.Companion.getBlobMembershipPersistentState
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class ProbeItem(
	settings: Settings,
) : Item(settings) {
	override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
		val stack = user.getStackInHand(hand)

		if (user.isSneaking) {
			val cancerBlobId = getSampleCancerBlobId(stack)

			if (cancerBlobId == null) {
				return TypedActionResult.pass(stack)
			} else {
				setSampleCancerBlobId(stack, null)
				return TypedActionResult.success(stack)
			}
		}

		return TypedActionResult.pass(stack)
	}

	override fun useOnBlock(context: ItemUsageContext): ActionResult {
		val world = context.world

		if (context.player?.isSneaking == true && containsSample(context.stack)) {
			if (!world.isClient) {
				setSampleCancerBlobId(context.stack, null)
			}

			return ActionResult.SUCCESS
		}

		if (containsSample(context.stack)) return ActionResult.FAIL

		if (world.isClient) {
			// Heuristics for whether the item use animation should be triggered.
			val targetBlockState = context.world.getBlockState(context.blockPos)
			if (targetBlockState?.isCancerous() == true) return ActionResult.SUCCESS

			return ActionResult.FAIL
		}

		world as ServerWorld

		val cancerBlobId = world.getBlobMembershipPersistentState().getMembershipOrNull(context.blockPos)
			?: return ActionResult.FAIL

		setSampleCancerBlobId(context.stack, cancerBlobId)

		return ActionResult.SUCCESS
	}

	override fun getTranslationKey(stack: ItemStack): String {
		return super.getTranslationKey(stack) + (if (containsSample(stack)) ".full" else ".empty")
	}

	override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
		super.appendTooltip(stack, world, tooltip, context)

		val sampleId = getSampleCancerBlobId(stack)

		tooltip.add(Text.translatable("${super.getTranslationKey(stack)}.tooltip" + (if (sampleId != null) ".full" else ".empty")).formatted(Formatting.GRAY))

		if (context.isAdvanced && sampleId != null) {
			tooltip.add(Text.translatable("${super.getTranslationKey(stack)}.tooltip.blobIdentifier", sampleId).formatted(Formatting.DARK_GRAY))
		}
	}

	companion object {
		private const val KEY_SAMPLE_ID = "${PreventTheSpread.MOD_ID}:cancerBlobId"

		fun containsSample(stack: ItemStack): Boolean {
			return stack.nbt?.contains(KEY_SAMPLE_ID) ?: false
		}

		fun getSampleCancerBlobId(stack: ItemStack): BlobIdentifier? {
			return stack.nbt?.takeIf { it.contains(KEY_SAMPLE_ID) }?.getInt(KEY_SAMPLE_ID)
		}

		fun setSampleCancerBlobId(stack: ItemStack, blobId: BlobIdentifier?) {
			if (blobId == null) {
				stack.nbt?.remove(KEY_SAMPLE_ID)
			} else {
				stack.getOrCreateNbt().putInt(KEY_SAMPLE_ID, blobId)
			}
		}
	}
}
