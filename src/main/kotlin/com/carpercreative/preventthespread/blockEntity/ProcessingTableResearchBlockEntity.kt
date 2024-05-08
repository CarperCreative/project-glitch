package com.carpercreative.preventthespread.blockEntity

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.screen.ProcessingTableResearchScreenHandler
import net.minecraft.block.BlockState
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class ProcessingTableResearchBlockEntity(
	pos: BlockPos,
	state: BlockState,
) : LockableContainerBlockEntity(
	PreventTheSpread.PROCESSING_TABLE_BLOCK_ENTITY,
	pos,
	state,
), SidedInventory {
	private var inventory = DefaultedList.ofSize(size(), ItemStack.EMPTY)

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
		return stack.isOf(PreventTheSpread.RESEARCH_ITEM)
	}

	override fun canPlayerUse(player: PlayerEntity): Boolean {
		return Inventory.canPlayerUse(this, player)
	}

	override fun getAvailableSlots(side: Direction): IntArray {
		return intArrayOf(RESEARCH_SLOT_INDEX)
	}

	override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?): Boolean {
		return isValid(slot, stack)
	}

	override fun canExtract(slot: Int, stack: ItemStack, dir: Direction?): Boolean {
		return true
	}

	override fun getContainerName(): Text {
		return Text.translatable("container.${PreventTheSpread.MOD_ID}.processing_table_research")
	}

	override fun createScreenHandler(syncId: Int, playerInventory: PlayerInventory): ScreenHandler {
		return ProcessingTableResearchScreenHandler(syncId, playerInventory, this)
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
		const val RESEARCH_SLOT_INDEX = 0

		const val SLOT_COUNT = 1
	}
}
