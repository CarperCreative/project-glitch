package com.carpercreative.preventthespread.controller

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.Storage
import com.carpercreative.preventthespread.team.Team
import com.carpercreative.preventthespread.team.TeamManager
import com.carpercreative.preventthespread.util.grantAdvancement
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

object StoryRootUnlockController {
	fun init() {
		Storage.teamManager.playerAddedEvent.register(::onPlayerJoinedTeam)
	}

	private fun onPlayerJoinedTeam(teamManager: TeamManager, team: Team, playerEntity: PlayerEntity) {
		(playerEntity as? ServerPlayerEntity)?.grantAdvancement(PreventTheSpread.StoryAdvancement.ROOT_ID)
	}
}
