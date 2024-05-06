package com.carpercreative.preventthespread.controller

import com.carpercreative.preventthespread.cancer.CancerLogic
import com.carpercreative.preventthespread.cancer.CancerLogic.isCancerSpreadable
import com.carpercreative.preventthespread.cancer.CancerType
import com.carpercreative.preventthespread.cancer.TreatmentType
import com.carpercreative.preventthespread.persistence.CancerBlobPersistentState.Companion.getCancerBlobPersistentState
import com.carpercreative.preventthespread.persistence.SpreadDifficultyPersistentState.Companion.getSpreadDifficultyPersistentState
import com.carpercreative.preventthespread.util.nextOfList
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.Heightmap

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
	}

	private fun tryCreateNewBlob(world: ServerWorld) {
		val spreadDifficulty = world.getSpreadDifficultyPersistentState()

		val cancerBlobPersistentState = world.getCancerBlobPersistentState()
		if (cancerBlobPersistentState.getActiveCancerBlobCount() >= spreadDifficulty.maxActiveBlobs) return

		val random = world.random

		val cancerSpawnPos = generateCancerSpawnPos(world, spreadDifficulty.blobSpawnRadius, spreadDifficulty.maxBlobDepth)

		// Generate cancer stats.
		val cancerType = when {
			spreadDifficulty.defeatedBlobs < 2 -> random.nextOfList(CancerType.entries.filter { it.treatments.contains(TreatmentType.SURGERY) })
			else -> random.nextOfList(CancerType.entries)
		}

		// Create the cancer blob.
		CancerLogic.createCancerBlob(world, cancerSpawnPos, spreadDifficulty.blobStartingSize, cancerType)
	}

	private fun generateCancerSpawnPos(world: ServerWorld, maxRadius: Float, maxDepth: Int): BlockPos {
		val random = world.random

		val cancerSpawnPos = BlockPos.Mutable()

		// Attempt to generate a valid position multiple times.
		// Returns the last position if none were deemed valid.
		var attempt = 1
		while (attempt <= 5) {
			attempt++

			val angle = random.nextDouble() * PI * 2
			val distance = random.nextDouble() * maxRadius

			cancerSpawnPos.set(world.spawnPos)
			cancerSpawnPos.x += (sin(angle) * distance).roundToInt()
			cancerSpawnPos.z += (cos(angle) * distance).roundToInt()

			cancerSpawnPos.y = world.getTopY(Heightmap.Type.OCEAN_FLOOR, cancerSpawnPos.x, cancerSpawnPos.z) - 1

			if (maxDepth > 0 && random.nextFloat() < 0.5f) {
				cancerSpawnPos.y = (cancerSpawnPos.y - (random.nextFloat() * maxDepth).roundToInt())
					// Avoid pockets surrounded by bedrock by starting above bedrock.
					.coerceAtLeast(world.dimension.minY + 8)
			}

			if (!world.getBlockState(cancerSpawnPos).isCancerSpreadable()) {
				// FIXME: this will cause an infinite loop on worlds with weird generators or blocks
				attempt--
				continue
			}

			// Try not to spawn cancer under fluids.
			if (!world.getFluidState(cancerSpawnPos.offset(Direction.UP)).isEmpty) continue

			// Position passed all checks.
			break
		}

		return cancerSpawnPos
	}
}
