package com.carpercreative.projectglitch.controller

import com.carpercreative.projectglitch.ProjectGlitch
import com.carpercreative.projectglitch.Storage
import com.carpercreative.projectglitch.team.Team
import com.carpercreative.projectglitch.team.TeamManager
import com.carpercreative.projectglitch.util.grantAdvancement
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

object StoryRootUnlockController {
	fun init() {
		Storage.teamManager.playerAddedEvent.register(::onPlayerJoinedTeam)
	}

	private fun onPlayerJoinedTeam(teamManager: TeamManager, team: Team, playerEntity: PlayerEntity) {
		(playerEntity as? ServerPlayerEntity)?.grantAdvancement(ProjectGlitch.StoryAdvancement.ROOT_ID)
	}
}
