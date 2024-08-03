package com.carpercreative.projectglitch.challenge.persistence

import com.carpercreative.projectglitch.ProjectGlitch
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import net.minecraft.world.PersistentState

class ChallengePersistentState(
	status: ChallengeStatus,
	playTime: Int,
	cheated: Boolean,
) : PersistentState() {
	enum class ChallengeStatus {
		UNKNOWN,
		IN_PROGRESS,
		ENDED,
		DISABLED,
	}

	var status: ChallengeStatus = status
		set(value) {
			field = value
			markDirty()
		}

	val isInProgress
		get() = status == ChallengeStatus.IN_PROGRESS

	var cheated: Boolean = cheated
		set(value) {
			field = value
			markDirty()
		}

	var playTime: Int = playTime
		set(value) {
			field = value.coerceAtLeast(0)
			markDirty()
		}

	fun invalidateChallenge(server: MinecraftServer) {
		if (cheated) return
		cheated = true

		server.playerManager.broadcast(Text.literal("Prevent the Spread challenge invalidated."), false)
	}

	override fun writeNbt(nbt: NbtCompound): NbtCompound {
		nbt.putInt(KEY_VERSION, 1)

		nbt.putString(KEY_CHALLENGE_STATUS, status.name)
		nbt.putInt(KEY_PLAY_TIME, playTime)

		return nbt
	}

	companion object {
		private const val KEY_VERSION = "version"
		private const val KEY_CHALLENGE_STATUS = "status"
		private const val KEY_PLAY_TIME = "playTime"
		private const val KEY_CHEATED = "cheated"

		private val type = Type(
			{ ChallengePersistentState(
				status = ChallengeStatus.UNKNOWN,
				playTime = 0,
				cheated = false,
			) },
			ChallengePersistentState::createFromNbt,
			null,
		)

		fun createFromNbt(nbt: NbtCompound): ChallengePersistentState {
			return ChallengePersistentState(
				status = ChallengeStatus.valueOf(nbt.getString(KEY_CHALLENGE_STATUS)),
				playTime = nbt.getInt(KEY_PLAY_TIME),
				cheated = nbt.getBoolean(KEY_CHEATED),
			)
		}

		fun MinecraftServer.getChallengePersistentState(): ChallengePersistentState {
			return overworld.persistentStateManager.getOrCreate(type, "${ProjectGlitch.MOD_ID}_challenge")
		}
	}
}
