package com.carpercreative.preventthespread.blockEntity

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.screen.ProcessingTableAnalyzerScreenHandler
import com.carpercreative.preventthespread.screen.slot.AnalyzerInputSlot
import net.minecraft.block.BlockState
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class ProcessingTableAnalyzerBlockEntity(
	pos: BlockPos,
	state: BlockState,
) : LockableContainerBlockEntity(
	PreventTheSpread.PROCESSING_TABLE_BLOCK_ENTITY,
	pos,
	state,
), SidedInventory {
	private var inventory = DefaultedList.ofSize(size(), ItemStack.EMPTY)

	private var analyzingSlot = -1
	private var analysisTime = 0

	private val propertyDelegate = object : PropertyDelegate {
		override fun get(index: Int) = when (index) {
			ANALYZING_SLOT_PROPERTY_INDEX -> analyzingSlot
			ANALYSIS_PROGRESS_PROPERTY_INDEX -> analysisTime
			else -> 0
		}

		override fun set(index: Int, value: Int) {
			when (index) {
				ANALYZING_SLOT_PROPERTY_INDEX -> analyzingSlot = value
				ANALYSIS_PROGRESS_PROPERTY_INDEX -> analysisTime = value
			}
		}

		override fun size(): Int = PROPERTY_COUNT
	}

	override fun clear() {
		inventory.clear()
	}

	override fun size(): Int {
		return SLOT_COUNT
	}

	override fun isEmpty(): Boolean {
		return inventory.all { it.isEmpty }
	}

	override fun getStack(slot: Int): ItemStack {
		return inventory[slot]
	}

	override fun removeStack(slot: Int, amount: Int): ItemStack {
		val stack = Inventories.splitStack(inventory, slot, amount)
		if (!stack.isEmpty) {
			markDirty()
		}
		return stack
	}

	override fun removeStack(slot: Int): ItemStack {
		return Inventories.removeStack(inventory, slot)
	}

	override fun setStack(slot: Int, stack: ItemStack) {
		inventory[slot] = stack
		if (stack.count > maxCountPerStack) {
			stack.count = maxCountPerStack
		}
		markDirty()
	}

	override fun isValid(slot: Int, stack: ItemStack): Boolean {
		return when (slot) {
			in OUTPUT_SLOTS -> false
			else -> AnalyzerInputSlot.isValid(stack)
		}
	}

	override fun canPlayerUse(player: PlayerEntity): Boolean {
		return Inventory.canPlayerUse(this, player)
	}

	override fun getAvailableSlots(side: Direction): IntArray {
		return when (side) {
			Direction.DOWN -> OUTPUT_SLOTS
			else -> INPUT_SLOTS
		}
	}

	override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?): Boolean {
		return isValid(slot, stack)
	}

	override fun canExtract(slot: Int, stack: ItemStack, dir: Direction?): Boolean {
		return if (dir == Direction.DOWN) {
			slot in OUTPUT_SLOTS
		} else {
			slot in INPUT_SLOTS
		}
	}

	override fun getContainerName(): Text {
		return Text.translatable("container.${PreventTheSpread.MOD_ID}.processing_table_analyzer")
	}

	override fun createScreenHandler(syncId: Int, playerInventory: PlayerInventory): ScreenHandler {
		return ProcessingTableAnalyzerScreenHandler(syncId, playerInventory, this, propertyDelegate)
	}

	override fun readNbt(nbt: NbtCompound) {
		super.readNbt(nbt)

		inventory = DefaultedList.ofSize(size(), ItemStack.EMPTY)
		Inventories.readNbt(nbt, inventory)
	}

	override fun writeNbt(nbt: NbtCompound) {
		super.writeNbt(nbt)

		Inventories.writeNbt(nbt, inventory)
	}

	companion object {
		const val QUEUE_SLOT_COUNT = 9

		const val SLOT_COUNT = QUEUE_SLOT_COUNT * 2

		val INPUT_SLOTS = (0..QUEUE_SLOT_COUNT).toList().toIntArray()
		val OUTPUT_SLOTS = (QUEUE_SLOT_COUNT..(QUEUE_SLOT_COUNT * 2)).toList().toIntArray()

		const val ANALYSIS_DURATION = 8 * 20

		const val ANALYZING_SLOT_PROPERTY_INDEX = 0
		const val ANALYSIS_PROGRESS_PROPERTY_INDEX = 1
		const val PROPERTY_COUNT = 2
	}
}
