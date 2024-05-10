package com.carpercreative.preventthespread.team

import net.minecraft.entity.player.PlayerEntity

class SimpleTeam(
	private val teamManager: TeamManager,
) : Team {
	override val players = arrayListOf<PlayerEntity>()

	override fun addPlayer(player: PlayerEntity): Boolean {
		if (players.contains(player)) return false

		players.add(player)

		teamManager.playerAddedEvent.invoker().onTeamPlayerAdded(teamManager, this, player)

		return true
	}

	override fun removePlayer(player: PlayerEntity): Boolean {
		if (!players.remove(player)) return false

		teamManager.playerRemovedEvent.invoker().onTeamPlayerRemoved(teamManager, this, player)

		return true
	}
}
