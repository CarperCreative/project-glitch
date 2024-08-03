package com.carpercreative.projectglitch.util

import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack

object InventoryHelper {
	fun canInsert(stack: ItemStack, targetInventory: Inventory, fromSlot: Int, toSlot: Int): Boolean {
		if (stack.isEmpty) return true

		var countRemaining = stack.count

		for (slotIndex in fromSlot..toSlot) {
			val targetStack = targetInventory.getStack(slotIndex)

			if (targetStack.count < targetStack.maxCount && (targetStack.isEmpty || ItemStack.canCombine(stack, targetStack))) {
				countRemaining -= targetStack.maxCount - targetStack.count

				if (countRemaining <= 0) return true
			}
		}

		return false
	}

	/**
	 * @return `true` if the entire [stack] was transferred, `false` if some or all of it remains.
	 */
	fun tryInsert(stack: ItemStack, targetInventory: Inventory, fromSlot: Int, toSlot: Int): Boolean {
		if (stack.isEmpty) return true

		for (slotIndex in fromSlot..toSlot) {
			val targetStack = targetInventory.getStack(slotIndex)

			// Amount which can be inserted into the target stack, limited to the amount of items in the source stack.
			val insertableCount = (targetStack.maxCount - targetStack.count).coerceAtMost(stack.count)

			if (insertableCount > 0 && (targetStack.isEmpty || ItemStack.canCombine(stack, targetStack))) {
				if (targetStack.isEmpty) {
					targetInventory.setStack(slotIndex, stack.copyAndEmpty())
					return true
				} else {
					targetStack.increment(insertableCount)
					stack.decrement(insertableCount)

					if (stack.count <= 0) return true
				}
			}
		}

		return false
	}

	fun tryInsert(stack: ItemStack, targetInventory: Inventory, fromSlot: Int, toSlot: Int, preferredSlot: Int): Boolean {
		if (tryInsert(stack, targetInventory, preferredSlot, toSlot)) return true
		if (tryInsert(stack, targetInventory, fromSlot, preferredSlot - 1)) return true

		return false
	}
}
