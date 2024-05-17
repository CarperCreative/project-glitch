package com.carpercreative.preventthespread.controller

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.Storage
import com.carpercreative.preventthespread.cancer.CancerLogic
import com.carpercreative.preventthespread.persistence.BlobMembershipPersistentState.Companion.getBlobMembershipPersistentState
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

object CancerSpreadController {
	fun init() {
		ServerTickEvents.END_WORLD_TICK.register { world ->
			tickWorld(world)
		}
	}

	private fun tickWorld(world: ServerWorld) {
		if (world.time % 20 != 12L) return

		// Cancer grows only in the main world.
		if (world != world.server.overworld) return

		tryCreateNewBlob(world)

		tickBlobs(world)
	}

	private fun tryCreateNewBlob(overworld: ServerWorld) {
		if (!overworld.gameRules.getBoolean(PreventTheSpread.DO_CANCER_SPAWNING_GAME_RULE)) return

		val spreadDifficulty = Storage.spreadDifficulty

		if (Storage.cancerBlob.getActiveCancerBlobCount() >= spreadDifficulty.maxActiveBlobs) return

		if (spreadDifficulty.nextSpawnAt == -1L) {
			// Schedule the next cancer blob spawn in the future.
			spreadDifficulty.nextSpawnAt = overworld.time + spreadDifficulty.blobSpawnDelayTicks
			return
		}

		// Do nothing until we hit the scheduled time.
		if (spreadDifficulty.nextSpawnAt.let { it >= 0 && overworld.time <= it }) return

		// Create the cancer blob.
		spreadDifficulty.nextSpawnAt = -1L
		CancerLogic.createCancerBlob(overworld)
	}

	private fun tickBlobs(world: ServerWorld) {
		if (!world.gameRules.getBoolean(PreventTheSpread.DO_CANCER_SPREAD_GAME_RULE)) return

		val blobMembership = world.getBlobMembershipPersistentState()
		val blobMemberships = blobMembership.getBlobMembershipsEntries()
		if (blobMemberships.isEmpty()) return

		val random = world.random

		// Prevent players from slowing down the spread to a crawl by leaving a single cancerous block on the map.
		val blocksToTick = (0.01f * blobMemberships.size.coerceAtLeast(Storage.spreadDifficulty.blobStartingSize))
			.let { if (random.nextFloat() > it % 1f) floor(it) else ceil(it) }
			.roundToInt()

		if (blocksToTick <= 0) return

		// `blobMemberships` is a set, meaning obtaining an element at a given index requires iterating over the entire set.
		// To make this cheaper without copying the whole set into an array, precompute all indexes to pick out the positions to tick in a single iteration.
		val memberIndices = IntArray(blocksToTick) {
			random.nextInt(blobMemberships.size)
		}
		memberIndices.sort()

		val blockPositionsToTick = Array<BlockPos?>(blocksToTick) { null }

		var memberIndicesIndex = 0
		memberIterator@for ((memberIndex, blobMember) in blobMemberships.withIndex()) {
			if (memberIndices[memberIndicesIndex] != memberIndex) continue

			blockPositionsToTick[memberIndicesIndex] = blobMember.key

			// Advance to next member index.
			// In case of RNG generates a duplicate index, iterate until we reach the next index.
			while (memberIndices[memberIndicesIndex] == memberIndex) {
				memberIndicesIndex++
				if (memberIndicesIndex >= memberIndices.size) break@memberIterator
			}
		}

		// Tick after iterating over blob memberships to prevent concurrent modification.
		for (blockPos in blockPositionsToTick) {
			if (blockPos == null) continue

			CancerLogic.attemptSpread(world, blockPos, world.random)
		}
	}
}
