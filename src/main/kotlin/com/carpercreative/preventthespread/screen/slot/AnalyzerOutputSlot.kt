package com.carpercreative.preventthespread.screen.slot

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.item.ProbeItem
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot

class AnalyzerOutputSlot(
	inventory: Inventory,
	index: Int,
	x: Int,
	y: Int,
) : Slot(inventory, index, x, y) {
	override fun canInsert(stack: ItemStack): Boolean {
		return when {
			stack.isOf(PreventTheSpread.PROBE_ITEM) -> ProbeItem.getSampleCancerBlobId(stack) == null
			stack.isOf(PreventTheSpread.RESEARCH_ITEM) -> true
			else -> false
		}
	}
}
