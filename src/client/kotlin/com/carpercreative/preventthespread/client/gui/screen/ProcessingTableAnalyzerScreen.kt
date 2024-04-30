package com.carpercreative.preventthespread.client.gui.screen

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.blockEntity.ProcessingTableAnalyzerBlockEntity
import com.carpercreative.preventthespread.screen.ProcessingTableAnalyzerScreenHandler
import kotlin.math.roundToInt
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

class ProcessingTableAnalyzerScreen(
	handler: ProcessingTableAnalyzerScreenHandler,
	inventory: PlayerInventory,
	title: Text,
) : HandledScreen<ProcessingTableAnalyzerScreenHandler>(handler, inventory, title) {
	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(context, mouseX, mouseY, delta)
		drawMouseoverTooltip(context, mouseX, mouseY)
	}

	override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
		context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight)

		val analyzingSlot = handler.propertyDelegate.get(ProcessingTableAnalyzerBlockEntity.ANALYZING_SLOT_PROPERTY_INDEX)
		if (analyzingSlot >= 0) {
			val analysisProgress = handler.propertyDelegate.get(ProcessingTableAnalyzerBlockEntity.ANALYSIS_PROGRESS_PROPERTY_INDEX)
			val progressFill = (analysisProgress.toFloat() / 16f * 16).roundToInt()

			val progressX = x + 8 + analyzingSlot * 18
			val progressY = y + 17 + 18

			context.drawGuiTexture(EMPTY_PROGRESS_TEXTURE, 16, 16, 0, 0, progressX, progressY, 16, 16)
			context.drawGuiTexture(FULL_PROGRESS_TEXTURE, 16, 16, 0, 16 - progressFill, progressX, progressY + 16 - progressFill, 16, progressFill)
		}
	}

	companion object {
		private val TEXTURE = PreventTheSpread.identifier("textures/gui/container/processing_table_analyzer.png")
		private val EMPTY_PROGRESS_TEXTURE = PreventTheSpread.identifier("container/processing_table_analyzer/progress_empty")
		private val FULL_PROGRESS_TEXTURE = PreventTheSpread.identifier("container/processing_table_analyzer/progress_full")
	}
}
