package com.carpercreative.preventthespread.persistence

import com.carpercreative.preventthespread.PreventTheSpread
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.roundToInt
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.PersistentState

class SpreadDifficultyPersistentState(
	defeatedBlobs: Int,
	nextScheduledSpawnAt: Long,
	nextForcedSpawnAt: Long,
) : PersistentState() {
	var defeatedBlobs: Int = defeatedBlobs
		private set(value) {
			field = value.coerceAtLeast(0)
		}

	fun incrementDefeatedBlobs() {
		defeatedBlobs++
	}

	/**
	 * Time at which the next blob will spawn if the player is still ahead of the difficulty.
	 */
	var nextScheduledSpawnAt: Long = nextScheduledSpawnAt
		set(value) {
			field = value.coerceAtLeast(-1)
			markDirty()
		}

	/**
	 * Time at which the next blob will be spawned unconditionally.
	 */
	var nextForcedSpawnAt: Long = nextForcedSpawnAt
		set(value) {
			field = value.coerceAtLeast(-1)
			markDirty()
		}

	val maxActiveBlobs: Int
		get() = (1 + (ceil((defeatedBlobs - 2).coerceAtLeast(0) / 4f).roundToInt())).coerceAtMost(6)

	val blobSpawnDelayTicks: Int
		get() = when (defeatedBlobs) {
			0 -> 120
			else -> (60 - defeatedBlobs * 5).coerceAtLeast(30)
		} * 20

	/**
	 * Amount of ticks players have to defeat any blob before another one is unconditionally spawned in.
	 */
	val blobForcedSpawnDelayTicks: Int
		get() = when (defeatedBlobs) {
			0 -> 15
			else -> (11 - defeatedBlobs).coerceAtLeast(2)
		} * 60 * 20

	val blobSpawnRadius: Float
		get() = 50f + 100f * defeatedBlobs.toFloat().pow(0.6f)

	val maxBlobDepth: Int
		get() = (20f * (defeatedBlobs - 1).coerceAtLeast(0).toFloat().pow(0.5f)).roundToInt()

	val blobStartingSize: Int
		get() = (6 + defeatedBlobs * 8).coerceAtMost(126)

	val metastaticChance: Float
		get() = ((defeatedBlobs - 3).coerceAtLeast(0) * 0.1f).coerceAtMost(0.8f)

	val metastaticMaxJumpDistance: Int
		get() = (defeatedBlobs - 3).coerceAtLeast(0) * 3

	fun resetForcedSpawnTime(overworld: ServerWorld) {
		nextForcedSpawnAt = overworld.time + blobForcedSpawnDelayTicks
	}

	override fun writeNbt(nbt: NbtCompound): NbtCompound {
		nbt.putInt(KEY_VERSION, 1)

		nbt.putInt(KEY_DEFEATED_BLOBS, defeatedBlobs)
		nbt.putLong(KEY_NEXT_SCHEDULED_SPAWN_AT, nextScheduledSpawnAt)
		nbt.putLong(KEY_NEXT_FORCED_SPAWN_AT, nextForcedSpawnAt)

		return nbt
	}

	companion object {
		private const val KEY_VERSION = "version"
		private const val KEY_DEFEATED_BLOBS = "defeatedBlobs"
		private const val KEY_NEXT_SCHEDULED_SPAWN_AT = "nextScheduledSpawnAt"
		private const val KEY_NEXT_FORCED_SPAWN_AT = "nextForcedSpawnAt"

		private val type = Type(
			{ SpreadDifficultyPersistentState(0, -1, -1) },
			SpreadDifficultyPersistentState::createFromNbt,
			null,
		)

		fun createFromNbt(nbt: NbtCompound): SpreadDifficultyPersistentState {
			return SpreadDifficultyPersistentState(
				nbt.getInt(KEY_DEFEATED_BLOBS),
				nextScheduledSpawnAt = nbt.getLong(KEY_NEXT_SCHEDULED_SPAWN_AT),
				nextForcedSpawnAt = nbt.getLong(KEY_NEXT_FORCED_SPAWN_AT),
			)
		}

		fun MinecraftServer.getSpreadDifficultyPersistentState(): SpreadDifficultyPersistentState {
			return overworld.persistentStateManager.getOrCreate(type, "${PreventTheSpread.MOD_ID}_spreadDifficulty")
		}
	}
}
