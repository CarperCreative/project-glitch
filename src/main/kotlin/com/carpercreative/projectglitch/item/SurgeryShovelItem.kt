package com.carpercreative.projectglitch.item

import com.carpercreative.projectglitch.ProjectGlitch
import net.fabricmc.fabric.api.item.v1.EnchantingContext
import net.minecraft.block.BlockState
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.Enchantments
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
		return if (state.isIn(ProjectGlitch.SURGERY_SHOVEL_MINEABLE_BLOCK_TAG))
			this.miningSpeed
		else
			super.getMiningSpeedMultiplier(stack, state)
	}

	override fun isSuitableFor(state: BlockState): Boolean {
		return if (state.isIn(ProjectGlitch.SURGERY_SHOVEL_MINEABLE_BLOCK_TAG))
			true
		else
			super.isSuitableFor(state)
	}

	override fun canBeEnchantedWith(stack: ItemStack, enchantment: Enchantment, context: EnchantingContext): Boolean {
		if (enchantment == Enchantments.EFFICIENCY) return false
		return super.canBeEnchantedWith(stack, enchantment, context)
	}
}