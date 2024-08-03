package com.carpercreative.projectglitch.screen.slot

import com.carpercreative.projectglitch.ProjectGlitch
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot

class ResearchInputSlot(
	inventory: Inventory,
	index: Int,
	x: Int,
	y: Int,
) : Slot(inventory, index, x, y) {
	override fun canInsert(stack: ItemStack): Boolean {
		return isValid(stack)
	}

	companion object {
		fun isValid(stack: ItemStack): Boolean {
			return stack.isOf(ProjectGlitch.RESEARCH_ITEM)
		}
	}
}
