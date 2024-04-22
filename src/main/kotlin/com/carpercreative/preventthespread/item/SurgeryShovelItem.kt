package com.carpercreative.preventthespread.item

import com.carpercreative.preventthespread.PreventTheSpread
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.item.ShovelItem
import net.minecraft.item.ToolMaterial

class SurgeryShovelItem(
	material: ToolMaterial,
	attackDamage: Float,
	attackSpeed: Float,
	settings: Settings,
) : ShovelItem(material, attackDamage, attackSpeed, settings) {
	override fun getMiningSpeedMultiplier(stack: ItemStack, state: BlockState): Float {
		return if (state.isIn(PreventTheSpread.SURGERY_SHOVEL_MINEABLE_BLOCK_TAG))
			this.miningSpeed
		else
			super.getMiningSpeedMultiplier(stack, state)
	}

	override fun isSuitableFor(state: BlockState): Boolean {
		return if (state.isIn(PreventTheSpread.CANCEROUS_BLOCK_TAG))
			true
		else
			super.isSuitableFor(state)
	}
}