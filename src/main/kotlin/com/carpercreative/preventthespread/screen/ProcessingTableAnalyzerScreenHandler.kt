package com.carpercreative.preventthespread.screen

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.blockEntity.ProcessingTableAnalyzerBlockEntity
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
			addSlot(AnalyzerInputSlot(inventory, queueIndex, 8 + queueIndex * 18, 17))
			addSlot(AnalyzerOutputSlot(inventory, ProcessingTableAnalyzerBlockEntity.QUEUE_SLOT_COUNT + queueIndex, 8 + queueIndex * 18, 17 + 18 + 18))
		}

		for (y in 0..2) {
			for (x in 0..8) {
				addSlot(Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18))
			}
		}
		for (x in 0..8) {
			addSlot(Slot(playerInventory, x, 8 + x * 18, 142))
		}
	}

	override fun quickMove(player: PlayerEntity?, slot: Int): ItemStack {
		return ItemStack.EMPTY
	}

	override fun canUse(player: PlayerEntity?): Boolean {
		return inventory.canPlayerUse(player)
	}

	override fun canInsertIntoSlot(stack: ItemStack, slot: Slot): Boolean {
		return slot.canInsert(stack)
	}

	override fun canInsertIntoSlot(slot: Slot): Boolean {
		return slot.index !in ProcessingTableAnalyzerBlockEntity.OUTPUT_SLOTS
	}
}
