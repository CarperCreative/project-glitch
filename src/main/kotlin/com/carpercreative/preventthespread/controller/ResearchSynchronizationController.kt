package com.carpercreative.preventthespread.controller

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.Storage
import com.carpercreative.preventthespread.team.Team
import com.carpercreative.preventthespread.team.TeamManager
import com.carpercreative.preventthespread.util.grantAdvancement
import com.carpercreative.preventthespread.util.hasAdvancement
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

		for (researchAdvancementId in PreventTheSpread.ResearchAdvancement.ALL_IDS) {
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
