package com.carpercreative.projectglitch.team

import com.carpercreative.projectglitch.team.TeamManager.TeamPlayerAdded
import com.carpercreative.projectglitch.team.TeamManager.TeamPlayerRemoved
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.player.PlayerEntity

class TeamManager {
	private val teams = arrayListOf<Team>()

	fun getTeams(): List<Team> = teams

	fun addTeam(team: Team) {
		teams.add(team)
	}

	fun clear() {
		teams.clear()
	}

	fun interface TeamPlayerAdded {
		fun onTeamPlayerAdded(teamManager: TeamManager, team: Team, player: PlayerEntity)
	}

	val playerAddedEvent: Event<TeamPlayerAdded> = EventFactory.createArrayBacked(TeamPlayerAdded::class.java) { callbacks ->
		TeamPlayerAdded { teamManager, team, player ->
			callbacks.forEach { it.onTeamPlayerAdded(teamManager, team, player) }
		}
	}

	fun interface TeamPlayerRemoved {
		fun onTeamPlayerRemoved(teamManager: TeamManager, team: Team, player: PlayerEntity)
	}

	val playerRemovedEvent: Event<TeamPlayerRemoved> = EventFactory.createArrayBacked(TeamPlayerRemoved::class.java) { callbacks ->
		TeamPlayerRemoved { teamManager, team, player ->
			callbacks.forEach { it.onTeamPlayerRemoved(teamManager, team, player) }
		}
	}
}
