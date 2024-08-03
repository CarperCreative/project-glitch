package com.carpercreative.projectglitch.controller

import com.carpercreative.projectglitch.ProjectGlitch
import com.carpercreative.projectglitch.Storage
import com.carpercreative.projectglitch.team.Team
import com.carpercreative.projectglitch.team.TeamManager
import com.carpercreative.projectglitch.util.grantAdvancement
import com.carpercreative.projectglitch.util.hasAdvancement
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

/**
 * Synchronizes the unlocked research advancements between all team members on team join.
 */
object ResearchSynchronizationController {
	fun init() {
		Storage.teamManager.playerAddedEvent.register(::onTeamPlayerAdded)
	}

	private fun onTeamPlayerAdded(teamManager: TeamManager, team: Team, playerEntity: PlayerEntity) {
		// TODO: take into account the unlocked researches of offline team members
		if (playerEntity !is ServerPlayerEntity) return

		val teamPlayers = team.players.filterIsInstance<ServerPlayerEntity>()

		for (researchAdvancementId in ProjectGlitch.ResearchAdvancement.ALL_IDS) {
			if (playerEntity.hasAdvancement(researchAdvancementId)) {
				for (teamPlayer in teamPlayers) {
					teamPlayer.grantAdvancement(researchAdvancementId)
				}
			} else if (teamPlayers.any { it.hasAdvancement(researchAdvancementId) }) {
				playerEntity.grantAdvancement(researchAdvancementId)
			}
		}
	}
}
