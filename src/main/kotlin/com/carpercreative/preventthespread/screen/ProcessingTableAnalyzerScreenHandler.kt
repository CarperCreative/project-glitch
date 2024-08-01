package com.carpercreative.preventthespread.screen

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.blockEntity.ProcessingTableAnalyzerBlockEntity
import com.carpercreative.preventthespread.screen.slot.AnalyzerBookSlot
import com.carpercreative.preventthespread.screen.slot.AnalyzerInputSlot
import com.carpercreative.preventthespread.screen.slot.AnalyzerOutputSlot
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot

class ProcessingTableAnalyzerScreenHandler(
	syncId: Int,
	private val playerInventory: PlayerInventory,
	private val inventory: Inventory,
	val propertyDelegate: PropertyDelegate,
) : ScreenHandler(
	PreventTheSpread.PROCESSING_TABLE_ANALYZER_SCREEN_HANDLER,
	syncId,
) {
	constructor(
		syncId: Int,
		playerInventory: PlayerInventory,
	) : this(
		syncId,
		playerInventory,
		SimpleInventory(ProcessingTableAnalyzerBlockEntity.SLOT_COUNT),
		ArrayPropertyDelegate(ProcessingTableAnalyzerBlockEntity.PROPERTY_COUNT),
	)

	init {
		addProperties(propertyDelegate)

		for (queueIndex in 0 until ProcessingTableAnalyzerBlockEntity.QUEUE_SLOT_COUNT) {
			val x = queueIndex % 2
			val y = queueIndex / 2
			addSlot(AnalyzerInputSlot(inventory, ProcessingTableAnalyzerBlockEntity.getQueueInputSlotIndex(queueIndex), 8 + 18 + x * 18, 17 + y * 18))
		}

		for (queueIndex in 0 until ProcessingTableAnalyzerBlockEntity.QUEUE_SLOT_COUNT) {
			val x = queueIndex % 2
			val y = queueIndex / 2
			addSlot(AnalyzerOutputSlot(inventory, ProcessingTableAnalyzerBlockEntity.getQueueOutputSlotIndex(queueIndex), 8 + 18 + (18 * 5) + x * 18, 17 + y * 18))
		}

		addSlot(AnalyzerBookSlot(inventory, ProcessingTableAnalyzerBlockEntity.BOOK_SLOT_INDEX, 80, 53))

		for (y in 0..2) {
			for (x in 0..8) {
				addSlot(Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18))
			}
		}
		for (x in 0..8) {
			addSlot(Slot(playerInventory, x, 8 + x * 18, 142))
		}
	}

	override fun quickMove(player: PlayerEntity, slotIndex: Int): ItemStack {
		// This function doesn't match what the game does, as it always returns ItemStack.EMPTY.
		// Trying to follow what the game does (returning a copy of the original stack) results in an infinite loop.
		// Inventory handling in this game is just awful.
		val slot = getSlot(slotIndex)
		if (slot == null || !slot.hasStack()) return ItemStack.EMPTY

		val slotStack = slot.stack

		if (slot.inventory == playerInventory) {
			// Don't try to insert into the book slot if it's not empty - for some reason the game doesn't check this already.
			if (getSlot(ProcessingTableAnalyzerBlockEntity.BOOK_SLOT_INDEX).let { !it.hasStack() || it.stack.isEmpty }) {
				// Ignore result of this function, checking instead if the stack is empty before attempting below to insert the remainder into the input slots.
				insertItem(slotStack, ProcessingTableAnalyzerBlockEntity.BOOK_SLOT_INDEX, ProcessingTableAnalyzerBlockEntity.BOOK_SLOT_INDEX + 1, false)
			}
			if (
				!slotStack.isEmpty
				&& !insertItem(slotStack, ProcessingTableAnalyzerBlockEntity.getQueueInputSlotIndex(0), ProcessingTableAnalyzerBlockEntity.getQueueInputSlotIndex(ProcessingTableAnalyzerBlockEntity.QUEUE_SLOT_COUNT), false)
				&& !insertItem(slotStack, ProcessingTableAnalyzerBlockEntity.getQueueOutputSlotIndex(0), ProcessingTableAnalyzerBlockEntity.getQueueOutputSlotIndex(ProcessingTableAnalyzerBlockEntity.QUEUE_SLOT_COUNT), false)
			) {
				return ItemStack.EMPTY
			}
		} else if (slot.inventory == inventory) {
			if (!insertItem(slotStack, inventory.size(), slots.size, true)) {
				return ItemStack.EMPTY
			}
		}

		if (slotStack.isEmpty) {
			slot.stack = ItemStack.EMPTY
		} else {
			slot.markDirty()
		}

		return ItemStack.EMPTY
	}

	override fun canUse(player: PlayerEntity?): Boolean {
		return inventory.canPlayerUse(player)
	}

	override fun canInsertIntoSlot(stack: ItemStack, slot: Slot): Boolean {
		return slot.canInsert(stack)
	}
}
