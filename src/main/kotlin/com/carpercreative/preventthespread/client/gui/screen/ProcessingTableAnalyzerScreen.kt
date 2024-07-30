package com.carpercreative.preventthespread.client.gui.screen

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.blockEntity.ProcessingTableAnalyzerBlockEntity
import com.carpercreative.preventthespread.screen.ProcessingTableAnalyzerScreenHandler
import com.carpercreative.preventthespread.screen.slot.AnalyzerBookSlot
import com.google.common.collect.Lists
import kotlin.math.roundToInt
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.BookScreen.WrittenBookContents
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.WrittenBookItem
import net.minecraft.text.OrderedText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class ProcessingTableAnalyzerScreen(
	handler: ProcessingTableAnalyzerScreenHandler,
	inventory: PlayerInventory,
	title: Text,
) : HandledScreen<ProcessingTableAnalyzerScreenHandler>(handler, inventory, title) {
	private var cachedBookStack: ItemStack? = null
	private var cachedBookTooltip = Lists.newArrayList<OrderedText>()

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(context, mouseX, mouseY, delta)
		drawMouseoverTooltip(context, mouseX, mouseY)
	}

	override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
		context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight)

		val analyzingSlot = handler.propertyDelegate.get(ProcessingTableAnalyzerBlockEntity.ANALYZING_SLOT_PROPERTY_INDEX)
		if (analyzingSlot >= 0) {
			val analysisProgress = handler.propertyDelegate.get(ProcessingTableAnalyzerBlockEntity.ANALYSIS_PROGRESS_PROPERTY_INDEX)
			val progressFill = (analysisProgress.toFloat() / ProcessingTableAnalyzerBlockEntity.ANALYSIS_DURATION * 16).roundToInt()

			val progressX = x + 80
			val progressY = y + 26

			context.drawGuiTexture(EMPTY_PROGRESS_TEXTURE, 16, 16, 0, 0, progressX, progressY, 16, 16)
			context.drawGuiTexture(FULL_PROGRESS_TEXTURE, 16, 16, 0, 16 - progressFill, progressX, progressY + 16 - progressFill, 16, progressFill)
		}
	}

	override fun drawMouseoverTooltip(context: DrawContext, x: Int, y: Int) {
		val analyzerBookSlot = focusedSlot as? AnalyzerBookSlot

		if (handler.cursorStack.isEmpty && analyzerBookSlot != null) {
			if (!analyzerBookSlot.hasStack()) {
				// Show hint about the purpose of the book slot.
				context.drawTooltip(textRenderer, hintTooltip, x, y)
				return
			}

			// Show a preview of the book contents.
			val bookStack = analyzerBookSlot.stack
			if (
				bookStack != null
				&& bookStack.isOf(Items.WRITTEN_BOOK)
				&& WrittenBookItem.getPageCount(bookStack) >= 1
			) {
				if (cachedBookStack != bookStack) {
					cachedBookStack = bookStack

					// Create a tooltip and cache it.
					cachedBookTooltip.clear()
					cachedBookTooltip.add(BOOK_PREVIEW_TITLE_TEXT.asOrderedText())

					val pageText = WrittenBookContents(bookStack).getPage(0)
						// Copied from BookScreen.
						?.let { textRenderer.wrapLines(it, 114) }
						// Remove empty lines from the end of the tooltip.
						?.dropLastWhile { text -> textRenderer.getWidth(text) <= 0 }

					pageText?.also { cachedBookTooltip.addAll(it) }
				}

				context.drawOrderedTooltip(textRenderer, cachedBookTooltip, x, y)
				return
			} else {
				// Forget the item as and tooltip as soon as possible to prevent hanging onto stale references.
				cachedBookStack = null
				cachedBookTooltip.clear()
			}
		}

		super.drawMouseoverTooltip(context, x, y)
	}

	companion object {
		private val TEXTURE = PreventTheSpread.identifier("textures/gui/container/processing_table_analyzer.png")
		private val EMPTY_PROGRESS_TEXTURE = PreventTheSpread.identifier("container/processing_table_analyzer/progress_empty")
		private val FULL_PROGRESS_TEXTURE = PreventTheSpread.identifier("container/processing_table_analyzer/progress_full")

		private val BOOK_PREVIEW_TITLE_TEXT = Text.translatable("container.${PreventTheSpread.MOD_ID}.processing_table_analyzer.book_preview.title").formatted(Formatting.YELLOW)

		private val hintTooltip = listOf(
			Text.translatable("container.${PreventTheSpread.MOD_ID}.processing_table_analyzer.empty_book_tooltip.0"),
			Text.translatable("container.${PreventTheSpread.MOD_ID}.processing_table_analyzer.empty_book_tooltip.1"),
		)
	}
}
