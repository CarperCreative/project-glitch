package com.carpercreative.preventthespread.client.gui.screen

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.networking.SelectResearchPacket
import com.carpercreative.preventthespread.screen.ProcessingTableResearchScreenHandler
import com.mojang.blaze3d.systems.RenderSystem
import kotlin.math.roundToInt
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.widget.ButtonWidget
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
	private val selectedTab get() = advancementsScreen.selectedTab

	private lateinit var researchButton: ButtonWidget

	/**
	 * `true` if the advancements widget is being moved.
	 */
	private var movingTab = false

	private var selectedAdvancement: SelectedAdvancement? = null
		set(value) {
			field = value

			ClientPlayNetworking.send(
				PreventTheSpread.SELECT_RESEARCH_PACKET_ID,
				PacketByteBufs.create().also { SelectResearchPacket(value?.identifier).write(it) },
			)
		}

	override fun init() {
		backgroundWidth = 252
		backgroundHeight = 222
		playerInventoryTitleY = -5000

		super.init()

		advancementHandler = client!!.player!!.networkHandler.advancementHandler

		// Awful. Cursed. What the fuck.
		advancementsScreen = AdvancementsScreen(advancementHandler)
		advancementsScreen.init(client, width, height)

		researchButton = ButtonWidget
			.builder(Text.translatable("container.${PreventTheSpread.MOD_ID}.processing_table_research.research_button")) {
				client!!.interactionManager!!.clickButton(handler.syncId, ProcessingTableResearchScreenHandler.RESEARCH_BUTTON_ID)

				selectedAdvancement = null
			}
			.position(x + 174, y + 165)
			.width(70)
			.build()
			.also(::addDrawableChild)
	}

	private fun updateResearchButton() {
		researchButton.active = selectedAdvancement != null && handler.researchItemCount > 0
	}

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		// Attempt to switch to the root research advancement until it's granted.
		if (selectedTab?.root?.advancementEntry?.id != PreventTheSpread.ResearchAdvancement.ROOT_ID) {
			advancementsScreen.selectTab(advancementHandler.get(PreventTheSpread.ResearchAdvancement.ROOT_ID))
		}

		updateResearchButton()

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
		if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
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
			val originX = MathHelper.floor(selectedTab.originX)
			val originY = MathHelper.floor(selectedTab.originY)

			for ((advancementEntry, advancementWidget) in selectedTab.widgets) {
				if (!advancementWidget.shouldRender(originX, originY, relativeMouseX, relativeMouseY)) continue

				// Do not allow selecting researches which are already complete.
				if (advancementWidget.progress?.isDone != false) continue

				// Do not allow selecting researches which don't have the previous researches completed.
				if (advancementWidget.parent?.progress?.isDone != true) continue

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
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			movingTab = false
		}

		return super.mouseReleased(mouseX, mouseY, button)
	}

	override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
		if (isInsideAdvancementsWidget(mouseX, mouseY)) {
			selectedTab?.move(horizontalAmount * 16.0, verticalAmount * 16.0)
			selectedAdvancement = null
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
	}
}
