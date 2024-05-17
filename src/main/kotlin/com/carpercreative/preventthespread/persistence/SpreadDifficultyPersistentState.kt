package com.carpercreative.preventthespread.persistence

import com.carpercreative.preventthespread.PreventTheSpread
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.roundToInt
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.world.PersistentState

class SpreadDifficultyPersistentState(
	defeatedBlobs: Int,
	nextSpawnAt: Long,
) : PersistentState() {
	var defeatedBlobs: Int = defeatedBlobs
		private set(value) {
			field = value.coerceAtLeast(0)
		}

	fun incrementDefeatedBlobs() {
		defeatedBlobs++
	}

	var nextSpawnAt: Long = nextSpawnAt
		set(value) {
			field = value.coerceAtLeast(-1)
			markDirty()
		}

	val maxActiveBlobs: Int
		get() = (1 + (ceil((defeatedBlobs - 2).coerceAtLeast(0) / 4f).roundToInt())).coerceAtMost(6)

	val blobSpawnDelayTicks: Int
		get() = (120 - defeatedBlobs * 10).coerceAtLeast(60) * 20

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

	override fun writeNbt(nbt: NbtCompound): NbtCompound {
		nbt.putInt(KEY_VERSION, 1)

		nbt.putInt(KEY_DEFEATED_BLOBS, defeatedBlobs)
		nbt.putLong(KEY_NEXT_SPAWN_AT, nextSpawnAt)

		return nbt
	}

	companion object {
		private const val KEY_VERSION = "version"
		private const val KEY_DEFEATED_BLOBS = "defeatedBlobs"
		private const val KEY_NEXT_SPAWN_AT = "nextSpawnAt"

		private val type = Type(
			{ SpreadDifficultyPersistentState(0, -1) },
			SpreadDifficultyPersistentState::createFromNbt,
			null,
		)

		fun createFromNbt(nbt: NbtCompound): SpreadDifficultyPersistentState {
			return SpreadDifficultyPersistentState(
				nbt.getInt(KEY_DEFEATED_BLOBS),
				nextSpawnAt = nbt.getLong(KEY_NEXT_SPAWN_AT),
			)
		}

		fun MinecraftServer.getSpreadDifficultyPersistentState(): SpreadDifficultyPersistentState {
			return overworld.persistentStateManager.getOrCreate(type, "${PreventTheSpread.MOD_ID}_spreadDifficulty")
		}
	}
}
