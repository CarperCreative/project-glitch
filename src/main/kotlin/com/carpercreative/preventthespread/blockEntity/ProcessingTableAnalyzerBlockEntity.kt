package com.carpercreative.preventthespread.blockEntity

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.Storage
import com.carpercreative.preventthespread.block.ProcessingTableBlock
import com.carpercreative.preventthespread.item.ProbeItem
import com.carpercreative.preventthespread.screen.ProcessingTableAnalyzerScreenHandler
import com.carpercreative.preventthespread.screen.slot.AnalyzerBookSlot
import com.carpercreative.preventthespread.screen.slot.AnalyzerInputSlot
import com.carpercreative.preventthespread.util.InventoryHelper
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.WrittenBookItem
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

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

		if (slot in INPUT_SLOTS_RANGE) {
			if (!performingAnalysis && !stack.isEmpty) {
				startAnalyzingNextSlot()
			} else if (analyzingSlot == slot) {
				// Stack in slot currently being analyzed was changed - restart analysis.
				startAnalyzingSlot(slot)
			}
		} else if (slot == BOOK_SLOT_INDEX) {
			if (stack.isEmpty && performingAnalysis && analysisRequiresBook(getStack(analyzingSlot))) {
				// Book was removed while analyzing stack requiring the book - start the next valid analysis.
				startAnalyzingNextSlot()
			} else if (!stack.isEmpty && !performingAnalysis) {
				// Book was inserted and we're not analyzing anything - attempt to start analysis.
				startAnalyzingNextSlot()
			}
		}

		markDirty()
	}

	override fun isValid(slot: Int, stack: ItemStack): Boolean {
		return when (slot) {
			in OUTPUT_SLOTS -> false
			BOOK_SLOT_INDEX -> AnalyzerBookSlot.isValid(stack)
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

	val performingAnalysis
		get() = analyzingSlot >= 0

	fun containsBook(): Boolean {
		val bookStack = getStack(BOOK_SLOT_INDEX)

		return !bookStack.isEmpty
			&& (bookStack.nbt?.getList(WrittenBookItem.PAGES_KEY, NbtElement.STRING_TYPE.toInt())?.size ?: 0) < WrittenBookItem.MAX_PAGES
	}

	private fun startAnalyzingSlot(slot: Int) {
		analyzingSlot = if (slot !in 0 until QUEUE_SLOT_COUNT) -1 else slot

		analysisTime = 0

		world?.also { world ->
			val newBlockState = world.getBlockState(pos)
				.with(ProcessingTableBlock.PROCESSING, analyzingSlot != -1)
			world.setBlockState(pos, newBlockState)
		}
	}

	private fun startAnalyzingNextSlot() {
		startAnalyzingSlot(-1)

		for (inputSlot in INPUT_SLOTS_RANGE) {
			val inputStack = getStack(inputSlot)
			if (inputStack.isEmpty) continue

			// Analyze probes only if there's a book to output to.
			if (analysisRequiresBook(inputStack) && !containsBook()) continue

			// Ignore items which we can't insert into the outputs.
			val analysisOutput = getAnalysisOutput(inputStack.copy())
			if (!InventoryHelper.canInsert(analysisOutput, this, getQueueOutputSlotIndex(0), getQueueOutputSlotIndex(QUEUE_SLOT_COUNT - 1))) continue

			startAnalyzingSlot(inputSlot)
			break
		}
	}

	object Ticker : BlockEntityTicker<ProcessingTableAnalyzerBlockEntity> {
		override fun tick(world: World, pos: BlockPos, state: BlockState, blockEntity: ProcessingTableAnalyzerBlockEntity) {
			if (world.isClient) return
			world as ServerWorld

			// Do nothing until an item is inserted.
			val analyzedSlot = blockEntity.analyzingSlot
			if (analyzedSlot < 0) return

			val analyzedStack = blockEntity.getStack(analyzedSlot)

			if (!AnalyzerInputSlot.isValid(analyzedStack)) {
				blockEntity.startAnalyzingNextSlot()
				return
			}

			blockEntity.analysisTime++

			if (blockEntity.analysisTime >= ANALYSIS_DURATION) {
				// Analysis finished.
				if (analyzedStack.isOf(PreventTheSpread.PROBE_ITEM)) {
					analyzeProbe(blockEntity, analyzedStack)
				}

				val analysisOutputStack = getAnalysisOutput(analyzedStack)

				// Ignore failures, as failure to move the item will result in it remaining in the inputs.
				moveStackToOutputSlot(analysisOutputStack, blockEntity, analyzedSlot)

				blockEntity.startAnalyzingNextSlot()
			}
		}

		private fun moveStackToOutputSlot(stack: ItemStack, blockEntity: ProcessingTableAnalyzerBlockEntity, queueSlot: Int): Boolean {
			val firstOutputSlot = getQueueOutputSlotIndex(0)
			val lastOutputSlot = getQueueOutputSlotIndex(QUEUE_SLOT_COUNT - 1)
			val equivalentOutputSlot = getQueueOutputSlotIndex(queueSlot)

			if (InventoryHelper.tryInsert(stack, blockEntity, equivalentOutputSlot, lastOutputSlot)) return true
			if (InventoryHelper.tryInsert(stack, blockEntity, firstOutputSlot, equivalentOutputSlot - 1)) return true

			return false
		}

		private fun analyzeProbe(blockEntity: ProcessingTableAnalyzerBlockEntity, stack: ItemStack) {
			if (!stack.isOf(PreventTheSpread.PROBE_ITEM)) return

			val cancerBlobId = ProbeItem.getSampleCancerBlobId(stack) ?: return
			val cancerBlob = Storage.cancerBlob.getCancerBlobByIdOrNull(cancerBlobId)

			if (cancerBlob == null) {
				prependBook(blockEntity, Text.translatable("${PreventTheSpread.MOD_ID}.analysis.invalid"))
				return
			}

			prependBook(
				blockEntity,
				Text.translatable(
					"${PreventTheSpread.MOD_ID}.analysis.results",
					cancerBlob.type.displayName,
					Text.empty().also { text ->
						cancerBlob.type.treatments.forEach { treatment ->
							text.append(
								Text.translatable(
									"${PreventTheSpread.MOD_ID}.analysis.treatment_item",
									treatment.displayName,
								)
							)
						}
					},
					Text.empty().also { notes ->
						if (cancerBlob.isMetastatic) {
							notes.append(Text.translatable("${PreventTheSpread.MOD_ID}.analysis.is_metastatic"))
						}
					},
				),
			)
		}

		private fun signBook(itemStack: ItemStack): ItemStack? {
			return when {
				itemStack.isOf(Items.WRITTEN_BOOK) -> itemStack
				itemStack.isOf(Items.BOOK) -> ItemStack(Items.WRITTEN_BOOK).apply {
					setSubNbt(WrittenBookItem.PAGES_KEY, NbtList())
					setSubNbt(WrittenBookItem.AUTHOR_KEY, NbtString.of(BOOK_AUTHOR))
					setSubNbt(WrittenBookItem.TITLE_KEY, NbtString.of(BOOK_TITLE))
				}
				itemStack.isOf(Items.WRITABLE_BOOK) -> ItemStack(Items.WRITTEN_BOOK).apply {
					val pagesList = NbtList().also { list ->
						itemStack.nbt?.getList(WrittenBookItem.PAGES_KEY, NbtElement.STRING_TYPE.toInt())
							?.forEach { list.add(NbtString.of(Text.Serialization.toJsonString(Text.literal(it.asString())))) }
					}
					setSubNbt(WrittenBookItem.PAGES_KEY, pagesList)
					setSubNbt(WrittenBookItem.AUTHOR_KEY, NbtString.of(BOOK_AUTHOR))
					setSubNbt(WrittenBookItem.TITLE_KEY, NbtString.of(BOOK_TITLE))
				}
				else -> null
			}
		}

		private fun prependBook(itemStack: ItemStack, text: Text): ItemStack {
			val book = signBook(itemStack) ?: return itemStack

			val pages = book.getOrCreateNbt().getList(WrittenBookItem.PAGES_KEY, NbtElement.STRING_TYPE.toInt())
				?: NbtList().also { book.setSubNbt(WrittenBookItem.PAGES_KEY, it) }

			pages.add(0, NbtString.of(Text.Serialization.toJsonString(text)))

			return book
		}

		private fun prependBook(blockEntity: ProcessingTableAnalyzerBlockEntity, text: Text) {
			val book = prependBook(blockEntity.getStack(BOOK_SLOT_INDEX), text)
			blockEntity.setStack(BOOK_SLOT_INDEX, book)
		}
	}

	companion object {
		const val QUEUE_SLOT_COUNT = 6
		const val BOOK_SLOT_INDEX = QUEUE_SLOT_COUNT * 2

		const val SLOT_COUNT = QUEUE_SLOT_COUNT * 2 + 1

		val INPUT_SLOTS_RANGE = 0..QUEUE_SLOT_COUNT
		val OUTPUT_SLOTS_RANGE = QUEUE_SLOT_COUNT..(QUEUE_SLOT_COUNT * 2)

		val INPUT_SLOTS = INPUT_SLOTS_RANGE.toList().toIntArray()
		val OUTPUT_SLOTS = OUTPUT_SLOTS_RANGE.toList().toIntArray()

		fun getQueueInputSlotIndex(queueIndex: Int) = queueIndex
		fun getQueueOutputSlotIndex(queueIndex: Int) = QUEUE_SLOT_COUNT + queueIndex

		const val ANALYSIS_DURATION = 8 * 20

		const val BOOK_AUTHOR = "Processing Table"
		const val BOOK_TITLE = "Analysis Results"

		const val ANALYZING_SLOT_PROPERTY_INDEX = 0
		const val ANALYSIS_PROGRESS_PROPERTY_INDEX = 1
		const val PROPERTY_COUNT = 2

		/**
		 * @return An item the analyzed stack gets converted to.
		 */
		fun getAnalysisOutput(analyzedStack: ItemStack): ItemStack {
			return when {
				analyzedStack.isOf(PreventTheSpread.PROBE_ITEM) -> {
					ProbeItem.setSampleCancerBlobId(analyzedStack, null)
					analyzedStack
				}
				analyzedStack.isOf(PreventTheSpread.CANCEROUS_MATERIAL_ITEM) -> {
					analyzedStack.count = 0
					PreventTheSpread.RESEARCH_ITEM.defaultStack
				}
				// If we somehow analyzed an item we don't recognize, output it as is.
				else -> analyzedStack
			}
		}

		fun analysisRequiresBook(itemStack: ItemStack): Boolean {
			return itemStack.isOf(PreventTheSpread.PROBE_ITEM)
		}
	}
}
