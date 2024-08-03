package com.carpercreative.projectglitch.controller

import com.carpercreative.projectglitch.Storage
import com.carpercreative.projectglitch.team.SimpleTeam
import com.carpercreative.projectglitch.team.Team
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld

/**
 * Creates a single team and adds everyone on the server to it.
 */
object EveryoneTeamController {
	private var everyoneTeam: Team? = null

	fun init() {
		ServerTickEvents.END_SERVER_TICK.register(::onEndServerTick)

		ServerWorldEvents.UNLOAD.register(::onWorldUnload)
	}

	private fun onWorldUnload(server: MinecraftServer, world: ServerWorld) {
		if (server.overworld == world) {
			everyoneTeam = null
		}
	}

	private fun getOrCreateEveryoneTeam(): Team {
		everyoneTeam?.also { return it }

		val team = SimpleTeam(Storage.teamManager)

		everyoneTeam = team
		Storage.teamManager.addTeam(team)

		return team
	}

	private fun onEndServerTick(server: MinecraftServer) {
		val everyoneTeam = getOrCreateEveryoneTeam()

		// Synchronize team members with server players.
		// Work on array copies to prevent concurrent modification.
		for (player in everyoneTeam.players.toTypedArray()) {
			if (server.playerManager.playerList.contains(player)) continue

			everyoneTeam.removePlayer(player)
		}

		for (player in server.playerManager.playerList.toTypedArray()) {
			if (everyoneTeam.players.contains(player)) continue

			everyoneTeam.addPlayer(player)
		}
	}
}
