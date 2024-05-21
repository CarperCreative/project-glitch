package com.carpercreative.preventthespread.screen.slot

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.item.ProbeItem
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot

class AnalyzerInputSlot(
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
			return (stack.isOf(PreventTheSpread.PROBE_ITEM) && ProbeItem.containsSample(stack))
				|| stack.isOf(PreventTheSpread.GLITCH_MATERIAL_ITEM)
		}
	}
}
