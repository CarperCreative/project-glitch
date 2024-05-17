package com.carpercreative.preventthespread.challenge.controller

import com.carpercreative.preventthespread.ChallengeConstants
import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.Storage
import com.carpercreative.preventthespread.cancer.CancerLogic
import com.carpercreative.preventthespread.challenge.persistence.ChallengePersistentState.ChallengeStatus
import com.carpercreative.preventthespread.challenge.persistence.ChallengePersistentState.Companion.getChallengePersistentState
import com.carpercreative.preventthespread.team.Team
import com.carpercreative.preventthespread.team.TeamManager
import com.mojang.logging.LogUtils
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos

object ChallengeStatusController {
	private val LOGGER = LogUtils.getLogger()

	private var hadSpreadLastTick = false

	fun init() {
		ServerTickEvents.END_WORLD_TICK.register(::onEndWorldTick)
		CancerLogic.cancerSpreadEvent.register(::onCancerSpread)
		Storage.teamManager.playerAddedEvent.register(::onTeamPlayerAdded)
	}

	private fun onTeamPlayerAdded(teamManager: TeamManager, team: Team, playerEntity: PlayerEntity) {
		if (playerEntity !is ServerPlayerEntity) return
		if (!playerEntity.server.getChallengePersistentState().isInProgress) return

		playerEntity.sendMessage(Text.translatable("preventthespread.challenge.is_in_progress"))
	}

	private fun onEndWorldTick(world: ServerWorld) {
		val server = world.server
		if (server.overworld != world) return

		val challengePersistentState = server.getChallengePersistentState()

		if (challengePersistentState.status == ChallengeStatus.UNKNOWN) {
			tryStartChallenge(server)
		}

		if (!challengePersistentState.isInProgress) return

		challengePersistentState.playTime++

		if (hadSpreadLastTick) {
			hadSpreadLastTick = false
			checkEndCondition(server)
		}
	}

	private fun tryStartChallenge(server: MinecraftServer) {
		val challengePersistentState = server.getChallengePersistentState()

		if (challengePersistentState.status != ChallengeStatus.UNKNOWN) return

		if (server.dataPackManager.enabledProfiles.none { it.name == ChallengeConstants.DATA_PACK_ID.toString() }) {
			challengePersistentState.status = ChallengeStatus.DISABLED
			return
		}

		server.overworld.gameRules.apply {
			get(PreventTheSpread.DO_CANCER_SPAWNING_GAME_RULE).set(true, server)
			get(PreventTheSpread.DO_CANCER_SPREAD_GAME_RULE).set(true, server)
		}

		if (!ChallengeValidityController.isValid(server)) {
			challengePersistentState.status = ChallengeStatus.DISABLED

			LOGGER.info("World is ineligible to start the Prevent the Spread Challenge.")
			return
		}

		challengePersistentState.status = ChallengeStatus.IN_PROGRESS

		server.setDifficultyLocked(true)

		LOGGER.info("Prevent the Spread Challenge started.")
	}

	private fun onCancerSpread(world: ServerWorld, fromPos: BlockPos?, toPos: BlockPos) {
		hadSpreadLastTick = true
	}

	private fun checkEndCondition(server: MinecraftServer) {
		if (!server.getChallengePersistentState().isInProgress) return

		val cancerousBlockCount = Storage.cancerBlob.getTotalCancerousBlockCount()

		if (cancerousBlockCount >= ChallengeConstants.CANCEROUS_BLOCK_LIMIT) {
			endChallenge(server)
		}
	}

	private fun Int.formatTime(): String {
		val time = this / 20
		val hours = time / 3600
		val minutes = ((time / 60) % 60).toString().run { if (hours > 0) padStart(2, '0') else this }
		val seconds = (time % 60).toString().padStart(2, '0')
		val fractions = (this % 20) / 10f

		return "${ hours.takeIf { it > 0 }?.let { "$it:" } ?: "" }$minutes:$seconds.$fractions"
	}

	private fun endChallenge(server: MinecraftServer) {
		val challengePersistentState = server.getChallengePersistentState()
		if (!challengePersistentState.isInProgress) return

		challengePersistentState.status = ChallengeStatus.ENDED

		server.playerManager.broadcast(
			Text.translatable(
				"${PreventTheSpread.MOD_ID}.challenge.ended",
				Storage.spreadDifficulty.defeatedBlobs,
				challengePersistentState.playTime.formatTime(),
			),
			true,
		)

		if (challengePersistentState.cheated) {
			server.playerManager.broadcast(Text.literal("This run was not valid.").formatted(Formatting.RED), false)
		}

		for (world in server.worlds) {
			val gameRules = world.gameRules
			gameRules.get(PreventTheSpread.DO_CANCER_SPAWNING_GAME_RULE).set(false, server)
			gameRules.get(PreventTheSpread.DO_CANCER_SPREAD_GAME_RULE).set(false, server)
		}
	}
}