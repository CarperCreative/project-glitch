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

	private fun tryCreateNewBlob(world: ServerWorld) {
		if (!world.gameRules.getBoolean(PreventTheSpread.DO_CANCER_SPAWNING_GAME_RULE)) return

		val spreadDifficulty = Storage.spreadDifficulty

		if (Storage.cancerBlob.getActiveCancerBlobCount() >= spreadDifficulty.maxActiveBlobs) return

		// Create the cancer blob.
		CancerLogic.createCancerBlob(world)
	}

	private fun tickBlobs(world: ServerWorld) {
		if (!world.gameRules.getBoolean(PreventTheSpread.DO_CANCER_SPREAD_GAME_RULE)) return

		val blobMembership = world.getBlobMembershipPersistentState()
		val blobMemberships = blobMembership.getBlobMembershipsEntries()
		if (blobMemberships.isEmpty()) return

		val random = world.random

		val blocksToTick = (0.01f * blobMemberships.size)
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
