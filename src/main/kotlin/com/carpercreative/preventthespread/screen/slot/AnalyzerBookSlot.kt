package com.carpercreative.preventthespread.screen.slot

import com.carpercreative.preventthespread.blockEntity.ProcessingTableAnalyzerBlockEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.WrittenBookItem
import net.minecraft.screen.slot.Slot

class AnalyzerBookSlot(
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
			return stack.isOf(Items.WRITABLE_BOOK)
				|| (
					stack.isOf(Items.WRITTEN_BOOK)
						&& stack.nbt?.getString(WrittenBookItem.AUTHOR_KEY) == ProcessingTableAnalyzerBlockEntity.BOOK_AUTHOR
				)
		}
	}
}
