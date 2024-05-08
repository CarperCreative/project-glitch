package com.carpercreative.preventthespread.client.gui.screen

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.screen.ProcessingTableResearchScreenHandler
import com.mojang.blaze3d.systems.RenderSystem
import kotlin.math.roundToInt
import net.minecraft.advancement.AdvancementEntry
import net.minecraft.advancement.AdvancementProgress
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.advancement.AdvancementTab
import net.minecraft.client.gui.screen.advancement.AdvancementWidget
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.network.ClientAdvancementManager
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec2f
import org.lwjgl.glfw.GLFW

class ProcessingTableResearchScreen(
	handler: ProcessingTableResearchScreenHandler,
	inventory: PlayerInventory,
	title: Text,
) : HandledScreen<ProcessingTableResearchScreenHandler>(handler, inventory, title) {
	private val player = inventory.player

	private lateinit var advancementHandler: ClientAdvancementManager

	private lateinit var advancementsScreen: AdvancementsScreen
	private val selectedTab get() = SELECTED_TAB_FIELD.get(advancementsScreen) as AdvancementTab?

	/**
	 * `true` if the advancements widget is being moved.
	 */
	private var movingTab = false

	private var selectedAdvancement: SelectedAdvancement? = null

	override fun init() {
		backgroundWidth = 252
		backgroundHeight = 222
		playerInventoryTitleY = -5000

		super.init()

		advancementHandler = client!!.player!!.networkHandler.advancementHandler

		// Awful. Cursed. What the fuck.
		advancementsScreen = AdvancementsScreen(advancementHandler)
		advancementsScreen.init(client, width, height)

		advancementsScreen.selectTab(advancementHandler.get(PreventTheSpread.ResearchAdvancement.ROOT_ID))
	}

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(context, mouseX, mouseY, delta)

		drawAdvancementTooltip(context, mouseX, mouseY, delta)

		drawMouseoverTooltip(context, mouseX, mouseY)
	}

	private fun drawAdvancementTooltip(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		selectedTab?.also { selectedTab ->
			context.matrices.push()
			context.matrices.translate((x + 9).toFloat(), (y + 18).toFloat(), 400.0f)
			RenderSystem.enableDepthTest()
			val relativeMouseX = selectedAdvancement?.selectedPos?.x?.roundToInt() ?: (mouseX - x - 9)
			val relativeMouseY = selectedAdvancement?.selectedPos?.y?.roundToInt() ?: (mouseY - y - 18)
			selectedTab.drawWidgetTooltip(context, relativeMouseX, relativeMouseY, x, y)
			RenderSystem.disableDepthTest()
			context.matrices.pop()
		}
	}

	override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
		selectedTab?.render(context, x + 9, y + 18)

		RenderSystem.enableBlend()
		context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight)
		RenderSystem.disableBlend()
	}

	override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
		if (button != 0) {
			movingTab = false
			return false
		}

		if (!movingTab && isInsideAdvancementsWidget(mouseX, mouseY)) {
			movingTab = true
			return true
		} else if (movingTab) {
			selectedTab?.move(deltaX, deltaY)
			return true
		}

		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		return handleMouseClicked(mouseX, mouseY, button)
			|| super.mouseClicked(mouseX, mouseY, button)
	}

	private fun handleMouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false

		if (selectedAdvancement != null && isInsideAdvancementsWidget(mouseX, mouseY)) {
			selectedAdvancement = null
			client!!.soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 0.75f))
			return true
		}

		val selectedTab = selectedTab ?: return false

		if (isInsideAdvancementsWidget(mouseX, mouseY)) {
			val relativeMouseX = (mouseX - x - 9).toInt()
			val relativeMouseY = (mouseY - y - 18).toInt()
			val originX = MathHelper.floor(selectedTab.originX_accessor)
			val originY = MathHelper.floor(selectedTab.originY_accessor)

			for ((advancementEntry, advancementWidget) in selectedTab.widgets_accessor) {
				if (!advancementWidget.shouldRender(originX, originY, relativeMouseX, relativeMouseY)) continue

				// Do not allow selecting researches which are already complete.
				if (advancementWidget.progress_accessor?.isDone != false) continue

				// Do not allow selecting researches which don't have the previous researches completed.
				if (advancementWidget.parent_accessor?.progress_accessor?.isDone != true) continue

				selectedAdvancement = SelectedAdvancement(
					advancementEntry.id,
					Vec2f(relativeMouseX.toFloat(), relativeMouseY.toFloat()),
				)

				client!!.soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))

				return true
			}
		}

		return false
	}

	override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (button == 0) {
			movingTab = false
		}

		return super.mouseReleased(mouseX, mouseY, button)
	}

	override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
		if (isInsideAdvancementsWidget(mouseX, mouseY)) {
			selectedTab?.move(horizontalAmount * 16.0, verticalAmount * 16.0)
			return true
		}

		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
	}

	private fun isInsideAdvancementsWidget(mouseX: Double, mouseY: Double): Boolean {
		return (mouseX.roundToInt() - x) in 9..243 && (mouseY.roundToInt() - y) in 18..131
	}

	private class SelectedAdvancement(
		val identifier: Identifier,

		/**
		 * Ugly hack to keep the advancement widget tooltip visible.
		 */
		val selectedPos: Vec2f,
	)

	companion object {
		private val TEXTURE = PreventTheSpread.identifier("textures/gui/container/processing_table_research.png")

		private val SELECTED_TAB_FIELD = AdvancementsScreen::class.java.getDeclaredField("selectedTab").also { it.trySetAccessible() }

		private val ADVANCEMENT_TAB_ORIGIN_X_FIELD = AdvancementTab::class.java.getDeclaredField("originX").also { it.trySetAccessible() }
		private val AdvancementTab.originX_accessor: Double get() = ADVANCEMENT_TAB_ORIGIN_X_FIELD.getDouble(this)
		private val ADVANCEMENT_TAB_ORIGIN_Y_FIELD = AdvancementTab::class.java.getDeclaredField("originY").also { it.trySetAccessible() }
		private val AdvancementTab.originY_accessor: Double get() = ADVANCEMENT_TAB_ORIGIN_Y_FIELD.getDouble(this)

		private val ADVANCEMENT_TAB_WIDGETS_FIELD = AdvancementTab::class.java.getDeclaredField("widgets").also { it.trySetAccessible() }
		@Suppress("UNCHECKED_CAST")
		private val AdvancementTab.widgets_accessor get() = ADVANCEMENT_TAB_WIDGETS_FIELD.get(this) as Map<AdvancementEntry, AdvancementWidget>

		private val ADVANCEMENT_WIDGET_PROGRESS_FIELD = AdvancementWidget::class.java.getDeclaredField("progress").also { it.trySetAccessible() }
		private val AdvancementWidget.progress_accessor get() = ADVANCEMENT_WIDGET_PROGRESS_FIELD.get(this) as AdvancementProgress?
		private val ADVANCEMENT_WIDGET_PARENT_FIELD = AdvancementWidget::class.java.getDeclaredField("parent").also { it.trySetAccessible() }
		private val AdvancementWidget.parent_accessor get() = ADVANCEMENT_WIDGET_PARENT_FIELD.get(this) as AdvancementWidget?
	}
}
