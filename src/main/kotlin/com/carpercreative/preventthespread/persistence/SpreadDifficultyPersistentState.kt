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
	spawnedBlobs: Int,
	defeatedBlobs: Int,
	nextScheduledSpawnAt: Long,
	nextForcedSpawnAt: Long,
) : PersistentState() {
	var spawnedBlobs: Int = spawnedBlobs
		private set(value) {
			field = value.coerceAtLeast(0)
		}

	fun incrementSpawnedBlobs() {
		spawnedBlobs++
	}

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
		get() = when (spawnedBlobs) {
			0 -> 120
			else -> (60 - spawnedBlobs * 5).coerceAtLeast(30)
		} * 20

	/**
	 * Amount of ticks players have to defeat any blob before another one is unconditionally spawned in.
	 */
	val blobForcedSpawnDelayTicks: Int
		get() = when (maxActiveBlobs) {
			// This mechanism is supposed to prevent cheesing by not defeating any blobs at the start of the game until ready.
			1, 2, 3 -> {
				when {
					// A forced blob has already been spawned as punishment before - the more the players fall behind the more aggressively we punish them.
					spawnedBlobs > maxActiveBlobs -> (10 - (spawnedBlobs - defeatedBlobs) * 4).coerceAtLeast(2)
					// Players aren't yet getting punished, but have fallen behind.
					else -> when (spawnedBlobs) {
						// Larger grace period for the first blob to allow gathering resources.
						0 -> 15
						// Otherwise force another spawn with increasing difficulty as time progresses.
						else -> (11 - maxActiveBlobs * 2).coerceAtLeast(2)
					}
				}
			}
			// Players have played enough that the difficulty curve will take over applying pressure.
			else -> 15
		} * 60 * 20 // Minutes to ticks.

	val blobSpawnMinRadius: Float
		get() = 80f * spawnedBlobs.toFloat().pow(0.4f)

	val blobSpawnMaxRadius: Float
		get() = 50f + 80f * spawnedBlobs.toFloat().pow(0.6f)

	val maxBlobDepth: Int
		get() = ((100f * (spawnedBlobs - 1).coerceAtLeast(0)).pow(0.5f)).roundToInt()

	val blobStartingSize: Int
		get() = (6 + spawnedBlobs * 5).coerceAtMost(36)

	val metastaticChance: Float
		get() = ((spawnedBlobs - 3).coerceAtLeast(0) * 0.1f).coerceAtMost(0.8f)

	val metastaticMaxJumpDistance: Int
		get() = (spawnedBlobs - 3).coerceAtLeast(0) * 3

	fun resetForcedSpawnTime(overworld: ServerWorld) {
		nextForcedSpawnAt = overworld.time + blobForcedSpawnDelayTicks
	}

	override fun writeNbt(nbt: NbtCompound): NbtCompound {
		nbt.putInt(KEY_VERSION, 1)

		nbt.putInt(KEY_SPAWNED_BLOBS, spawnedBlobs)
		nbt.putInt(KEY_DEFEATED_BLOBS, defeatedBlobs)
		nbt.putLong(KEY_NEXT_SCHEDULED_SPAWN_AT, nextScheduledSpawnAt)
		nbt.putLong(KEY_NEXT_FORCED_SPAWN_AT, nextForcedSpawnAt)

		return nbt
	}

	companion object {
		private const val KEY_VERSION = "version"
		private const val KEY_SPAWNED_BLOBS = "spawnedBlobs"
		private const val KEY_DEFEATED_BLOBS = "defeatedBlobs"
		private const val KEY_NEXT_SCHEDULED_SPAWN_AT = "nextScheduledSpawnAt"
		private const val KEY_NEXT_FORCED_SPAWN_AT = "nextForcedSpawnAt"

		private val type = Type(
			{ SpreadDifficultyPersistentState(0, 0, -1, -1) },
			SpreadDifficultyPersistentState::createFromNbt,
			null,
		)

		fun createFromNbt(nbt: NbtCompound): SpreadDifficultyPersistentState {
			return SpreadDifficultyPersistentState(
				spawnedBlobs = nbt.getInt(KEY_SPAWNED_BLOBS),
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
