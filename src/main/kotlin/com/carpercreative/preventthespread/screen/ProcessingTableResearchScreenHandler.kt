package com.carpercreative.preventthespread.screen

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.blockEntity.ProcessingTableAnalyzerBlockEntity
import com.carpercreative.preventthespread.blockEntity.ProcessingTableResearchBlockEntity
import com.carpercreative.preventthespread.screen.slot.ResearchInputSlot
import com.carpercreative.preventthespread.util.grantAdvancement
import kotlin.jvm.optionals.getOrNull
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

class ProcessingTableResearchScreenHandler(
	syncId: Int,
	private val playerInventory: PlayerInventory,
	private val inventory: Inventory,
) : ScreenHandler(
	PreventTheSpread.PROCESSING_TABLE_RESEARCH_SCREEN_HANDLER,
	syncId,
) {
	constructor(
		syncId: Int,
		playerInventory: PlayerInventory,
	) : this(
		syncId,
		playerInventory,
		SimpleInventory(ProcessingTableAnalyzerBlockEntity.SLOT_COUNT),
	)

	private var selectedResearchId: Identifier? = null

	val researchItemCount: Int
		get() = inventory.getStack(ProcessingTableResearchBlockEntity.RESEARCH_SLOT_INDEX).count

	init {
		addSlot(ResearchInputSlot(inventory, ProcessingTableResearchBlockEntity.RESEARCH_SLOT_INDEX, 202, 140))

		for (y in 0..2) {
			for (x in 0..8) {
				addSlot(Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 140 + y * 18))
			}
		}
		for (x in 0..8) {
			addSlot(Slot(playerInventory, x, 8 + x * 18, 198))
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
			if (!insertItem(slotStack, ProcessingTableResearchBlockEntity.RESEARCH_SLOT_INDEX, ProcessingTableResearchBlockEntity.RESEARCH_SLOT_INDEX + 1, false)) {
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

	override fun onButtonClick(player: PlayerEntity, id: Int): Boolean {
		var sendContentUpdates = false

		when (id) {
			RESEARCH_BUTTON_ID -> {
				// Nothing to research.
				val selectedResearchId = selectedResearchId
					?: return false

				val researchStack = inventory.getStack(ProcessingTableResearchBlockEntity.RESEARCH_SLOT_INDEX)
				// Not enough research items to perform this action.
				if (researchStack.isEmpty) return false

				player as ServerPlayerEntity
				if (!player.grantAdvancement(selectedResearchId)) return false

				inventory.removeStack(ProcessingTableResearchBlockEntity.RESEARCH_SLOT_INDEX, 1)

				this.selectedResearchId = null
				sendContentUpdates = true
			}
		}

		return sendContentUpdates
	}

	fun onResearchSelected(researchAdvancementId: Identifier?) {
		if (researchAdvancementId == null) {
			selectedResearchId = null
			return
		}

		val player = playerInventory.player as ServerPlayerEntity
		val advancementLoader = player.server.advancementLoader
		val advancementTracker = player.advancementTracker

		val researchAdvancement = advancementLoader.get(researchAdvancementId)
			?: return

		// Can't select an already researched advancement.
		if (advancementTracker.getProgress(researchAdvancement).isDone) return

		// Previous research must be done to research its descendants.
		val parentAdvancement = advancementLoader.get(researchAdvancement.value.parent.getOrNull())
		if (parentAdvancement != null && !advancementTracker.getProgress(parentAdvancement).isDone) return

		selectedResearchId = researchAdvancementId
	}

	companion object {
		const val RESEARCH_BUTTON_ID = 0
	}
}