package com.carpercreative.projectglitch.screen.slot

import com.carpercreative.projectglitch.ProjectGlitch
import com.carpercreative.projectglitch.item.ProbeItem
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
			stack.isOf(ProjectGlitch.PROBE_ITEM) -> ProbeItem.getSampleCancerBlobId(stack) == null
			stack.isOf(ProjectGlitch.RESEARCH_ITEM) -> true
			else -> false
		}
	}
}
