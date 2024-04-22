package com.carpercreative.preventthespread.item

import com.carpercreative.preventthespread.PreventTheSpread
import net.minecraft.block.BlockState
import net.minecraft.item.HoeItem
import net.minecraft.item.ItemStack
import net.minecraft.item.ToolMaterial

class SurgeryHoeItem(
	material: ToolMaterial,
	attackDamage: Int,
	attackSpeed: Float,
	settings: Settings,
) : HoeItem(material, attackDamage, attackSpeed, settings) {
	override fun getMiningSpeedMultiplier(stack: ItemStack, state: BlockState): Float {
		return if (state.isIn(PreventTheSpread.SURGERY_HOE_MINEABLE_BLOCK_TAG))
			this.miningSpeed
		else
			super.getMiningSpeedMultiplier(stack, state)
	}

	override fun isSuitableFor(state: BlockState): Boolean {
		return if (state.isIn(PreventTheSpread.SURGERY_HOE_MINEABLE_BLOCK_TAG))
			true
		else
			super.isSuitableFor(state)
	}
}
