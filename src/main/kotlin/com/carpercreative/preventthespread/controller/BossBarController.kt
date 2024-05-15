package com.carpercreative.preventthespread.controller

import com.carpercreative.preventthespread.ChallengeConstants
import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.Storage
import kotlin.math.roundToInt
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.entity.boss.BossBar
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text

object BossBarController {
	private val DANGER_BOSS_BAR_ID = PreventTheSpread.identifier("danger")

	private const val CANCEROUS_BLOCK_LIMIT = 1200

	fun init() {
		ServerTickEvents.END_SERVER_TICK.register { server ->
			tick(server)
		}
	}

	private fun tick(server: MinecraftServer) {
		if (server.ticks % 20 == 6) {
			val bossBarManager = server.bossBarManager

			val bossBar = bossBarManager.get(DANGER_BOSS_BAR_ID)
				?: bossBarManager.add(DANGER_BOSS_BAR_ID, Text.empty()).apply {
					color = BossBar.Color.RED
				}

			val dangerLevel = getDangerLevel()

			bossBar.isVisible = dangerLevel > 0

			bossBar.maxValue = 100
			// TODO: value updates could be reactive to block events
			bossBar.value = (dangerLevel * 100).roundToInt()
			bossBar.name = Text.translatable("${PreventTheSpread.MOD_ID}.boss_bar_controller.text", (dangerLevel * 100).roundToInt())

			// TODO: this could be reactive to player joins
			bossBar.addPlayers(server.playerManager.playerList)
		}
	}

	private fun getDangerLevel(): Float {
		val cancerousBlockCount = Storage.cancerBlob.getTotalCancerousBlockCount()

		return cancerousBlockCount.toFloat() / ChallengeConstants.CANCEROUS_BLOCK_LIMIT
	}
}
