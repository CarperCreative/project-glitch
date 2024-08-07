package com.carpercreative.projectglitch.challenge.controller

import com.carpercreative.projectglitch.ProjectGlitch
import com.carpercreative.projectglitch.challenge.persistence.ChallengePersistentState.Companion.getChallengePersistentState
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.world.Difficulty

object ChallengeValidityController {
	fun init() {
		ServerTickEvents.END_SERVER_TICK.register(::onEndServerTick)
	}

	fun isValid(server: MinecraftServer): Boolean {
		val overworld = server.overworld

		if (overworld.difficulty.let { it != Difficulty.NORMAL && it != Difficulty.HARD }) return false

		if (!overworld.gameRules.run { getBoolean(ProjectGlitch.DO_GLITCH_SPAWNING_GAME_RULE) && getBoolean(ProjectGlitch.DO_GLITCH_SPREAD_GAME_RULE) }) return false

		return true
	}

	private fun onEndServerTick(server: MinecraftServer) {
		val challengePersistentState = server.getChallengePersistentState()
		if (!challengePersistentState.isInProgress) return

		if (!isValid(server)) {
			challengePersistentState.invalidateChallenge(server)
		}
	}
}