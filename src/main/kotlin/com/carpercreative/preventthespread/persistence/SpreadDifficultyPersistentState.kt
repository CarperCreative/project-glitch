package com.carpercreative.preventthespread.persistence

import com.carpercreative.preventthespread.PreventTheSpread
import kotlin.math.pow
import kotlin.math.roundToInt
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.world.PersistentState

class SpreadDifficultyPersistentState(
	defeatedBlobs: Int,
) : PersistentState() {
	var defeatedBlobs: Int = defeatedBlobs
		private set(value) {
			field = value.coerceAtLeast(0)
		}

	fun incrementDefeatedBlobs() {
		defeatedBlobs++
		markDirty()
	}

	val maxActiveBlobs: Int
		get() = 1 + ((defeatedBlobs - 4).coerceAtLeast(0) / 7)

	val blobSpawnRadius: Float
		get() = 50f + 100f * defeatedBlobs.toFloat().pow(0.4f)

	val maxBlobDepth: Int
		get() = (20f * (defeatedBlobs - 5).coerceAtLeast(0).toFloat().pow(0.4f)).roundToInt()

	val blobStartingSize: Int
		get() = (6 + defeatedBlobs * 2).coerceAtMost(64)

	val metastaticChance: Float
		get() = ((defeatedBlobs - 5).coerceAtLeast(0) * 0.1f).coerceAtMost(0.8f)

	val metastaticMaxJumpDistance: Int
		get() = ((defeatedBlobs - 5).coerceAtLeast(0) / 4) * 5

	override fun writeNbt(nbt: NbtCompound): NbtCompound {
		nbt.putInt(KEY_VERSION, 1)

		nbt.putInt(KEY_DEFEATED_BLOBS, defeatedBlobs)

		return nbt
	}

	companion object {
		private const val KEY_VERSION = "version"
		private const val KEY_DEFEATED_BLOBS = "defeatedBlobs"

		private val type = Type(
			{ SpreadDifficultyPersistentState(0) },
			SpreadDifficultyPersistentState::createFromNbt,
			null,
		)

		fun createFromNbt(nbt: NbtCompound): SpreadDifficultyPersistentState {
			return SpreadDifficultyPersistentState(
				nbt.getInt(KEY_DEFEATED_BLOBS),
			)
		}

		fun MinecraftServer.getSpreadDifficultyPersistentState(): SpreadDifficultyPersistentState {
			return overworld.persistentStateManager.getOrCreate(type, "${PreventTheSpread.MOD_ID}_spreadDifficulty")
		}
	}
}
