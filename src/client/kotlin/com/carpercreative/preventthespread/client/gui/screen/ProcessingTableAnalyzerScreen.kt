package com.carpercreative.preventthespread.client.gui.screen

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.screen.ProcessingTableAnalyzerScreenHandler
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
	}

	companion object {
		private val TEXTURE = PreventTheSpread.identifier("textures/gui/container/processing_table_analyzer.png")
	}
}
