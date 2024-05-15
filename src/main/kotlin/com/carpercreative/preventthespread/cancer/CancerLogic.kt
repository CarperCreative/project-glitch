package com.carpercreative.preventthespread.cancer

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.Storage
import com.carpercreative.preventthespread.block.TowerBlock
import com.carpercreative.preventthespread.persistence.BlobMembershipPersistentState.Companion.getBlobMembershipPersistentState
import com.carpercreative.preventthespread.persistence.SpreadDifficultyPersistentState
import com.carpercreative.preventthespread.util.contentsSequence
import com.carpercreative.preventthespread.util.nextOfArray
import com.carpercreative.preventthespread.util.nextOfList
import com.carpercreative.preventthespread.util.nextOfListOrNull
import java.util.LinkedList
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.SlabBlock
import net.minecraft.block.StairsBlock
import net.minecraft.registry.tag.BlockTags
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.Heightmap
import net.minecraft.world.poi.PointOfInterestStorage

object CancerLogic {
	private val WEIGHTED_DIRECTIONS = arrayOf(
		Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH,
		Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH,
		Direction.DOWN, Direction.UP,
	)

	fun BlockState.isCancerous(): Boolean {
		return isIn(PreventTheSpread.CANCEROUS_BLOCK_TAG)
	}

	private fun BlockState.isCancerModifiable(): Boolean {
		// No point spreading to already cancer infested blocks.
		return !isCancerous()
			// Spreading to block entities could have unintended consequences, like dropping the entire contents of a chest.
			&& !hasBlockEntity()
			// Prevent spreading to unbreakable blocks like bedrock.
			&& block.hardness >= 0f
	}

	/**
	 * @return `true` for blocks which are valid targets for cancer to spread to.
	 * This excludes all cancer blocks and block entities.
	 */
	fun BlockState.isCancerSpreadable(): Boolean {
		return isCancerModifiable()
			// Allow spreading only to explicitly whitelisted blocks.
			&& isIn(PreventTheSpread.CANCER_SPREADABLE_BLOCK_TAG)
	}

	/**
	 * @return `true` for blocks which are valid targets for a cancer blob to get spawned from.
	 * This excludes all cancer blocks and block entities.
	 */
	fun BlockState.isValidCancerSeed(): Boolean {
		return isCancerModifiable()
			// Never spawn a blob in air.
			&& !isAir
			// Allow spreading only to explicitly whitelisted blocks.
			&& isIn(PreventTheSpread.VALID_CANCER_SEED_BLOCK_TAG)
	}

	fun generateCancerSpawnPos(world: ServerWorld, maxRadius: Float, maxDepth: Int): BlockPos {
		val minimumY = world.dimension.minY + 8
		val random = world.random

		val cancerSpawnPos = BlockPos.Mutable()

		// Attempt to generate a valid position multiple times.
		// Returns the last position if none were deemed valid.
		var attempt = 1
		var invalidAttempts = 0
		nextAttempt@while (attempt <= 5) {
			attempt++

			val angle = random.nextDouble() * PI * 2
			val distance = random.nextDouble() * maxRadius

			cancerSpawnPos.set(world.spawnPos)
			cancerSpawnPos.x += (sin(angle) * distance).roundToInt()
			cancerSpawnPos.z += (cos(angle) * distance).roundToInt()

			cancerSpawnPos.y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, cancerSpawnPos.x, cancerSpawnPos.z) - 1

			if (invalidAttempts > 5) {
				// Fallback in case of worlds where no blocks are valid seed locations.
				// Go up until...
				while (
					// hitting air or a valid cancer seed block,
					!world.getBlockState(cancerSpawnPos).run { isAir || isValidCancerSeed() }
					// or reaching the height limit (disregards all validity checks).
					&& !world.isOutOfHeightLimit(cancerSpawnPos.y + 1)
				) {
					cancerSpawnPos.y++
				}
				return cancerSpawnPos
			}

			// Find surface position, ignoring leaves, trees, and fluids.
			var blocksDescendedToFindSurface = 0
			while (cancerSpawnPos.y > minimumY) {
				if (world.getBlockState(cancerSpawnPos).isValidCancerSeed()) break

				cancerSpawnPos.y--

				// Prevent descending too far below the surface to avoid compounding depth with the max depth from spread difficulty.
				if (blocksDescendedToFindSurface++ > 15) continue@nextAttempt
			}

			if (maxDepth > 0 && random.nextFloat() < 0.5f) {
				// Hide below the ground, up to the max depth.
				// While descending, check every block, and use the last one which is a valid spawn location.
				var maxDescent = (random.nextFloat() * maxDepth).roundToInt()
				var lastValidY = cancerSpawnPos.y
				while (maxDescent > 0) {
					maxDescent--
					cancerSpawnPos.y--

					if (world.getBlockState(cancerSpawnPos).isValidCancerSeed()) {
						lastValidY = cancerSpawnPos.y
					}
				}
				cancerSpawnPos.y = lastValidY
			}

			cancerSpawnPos.y = cancerSpawnPos.y.coerceAtLeast(minimumY)

			if (!world.getBlockState(cancerSpawnPos).isValidCancerSeed()) {
				attempt--
				invalidAttempts++
				continue@nextAttempt
			}

			// Try not to spawn cancer under fluids.
			// If a position under a fluid is reached every attempt, the last one will be returned.
			if (!world.getFluidState(cancerSpawnPos.offset(Direction.UP)).isEmpty) continue

			// Position passed all checks.
			break@nextAttempt
		}

		return cancerSpawnPos
	}

	/**
	 * Creates a new [CancerBlob] according to the current [spread difficulty][SpreadDifficultyPersistentState] values, using the given [world]'s randomness.
	 */
	fun createCancerBlob(world: ServerWorld): CancerBlob? {
		val spreadDifficulty = Storage.spreadDifficulty
		val cancerSpawnPos = generateCancerSpawnPos(world, spreadDifficulty.blobSpawnRadius, spreadDifficulty.maxBlobDepth)

		return createCancerBlob(world, cancerSpawnPos)
	}

	/**
	 * Creates a new [CancerBlob] according to the current [spread difficulty][SpreadDifficultyPersistentState] values, using the given [world]'s randomness.
	 */
	fun createCancerBlob(world: ServerWorld, cancerSpawnPos: BlockPos): CancerBlob? {
		val random = world.random
		val spreadDifficulty = Storage.spreadDifficulty

		// Generate cancer stats.
		val cancerType = when {
			spreadDifficulty.defeatedBlobs < 2 -> random.nextOfList(CancerType.entries.filter { it.treatments.contains(TreatmentType.SURGERY) })
			else -> random.nextOfList(CancerType.entries)
		}

		val metastaticMaxJumpDistance = when {
			random.nextFloat() < spreadDifficulty.metastaticChance -> spreadDifficulty.metastaticMaxJumpDistance
			else -> 0
		}

		// Create the cancer blob.
		return createCancerBlob(world, cancerSpawnPos, spreadDifficulty.blobStartingSize, cancerType, metastaticMaxJumpDistance)
	}

	fun createCancerBlob(world: ServerWorld, cancerSpawnPos: BlockPos, maxSize: Int, cancerType: CancerType, maxMetastaticJumpDistance: Int): CancerBlob? {
		val blobMembershipPersistentState = world.getBlobMembershipPersistentState()

		if (world.getBlockState(cancerSpawnPos).isCancerous() || blobMembershipPersistentState.getMembershipOrNull(cancerSpawnPos) != null) return null

		val cancerBlob = Storage.cancerBlob.createCancerBlob { CancerBlob(it, cancerType, maxMetastaticJumpDistance) }

		for (blockPos in getBlocksForBlobCreation(world, cancerSpawnPos, maxSize)) {
			convertToCancer(world, blockPos)
			blobMembershipPersistentState.setMembership(blockPos, cancerBlob)
		}

		return cancerBlob
	}

	private fun getBlocksForBlobCreation(world: ServerWorld, startPos: BlockPos, maxBlocks: Int): List<BlockPos> {
		val out = mutableListOf<BlockPos>()
		val visited = hashSetOf<BlockPos>()
		val queue = LinkedList<BlockPos>()

		val random = world.random

		queue.add(startPos.toImmutable())
		visited.add(queue.peek())

		while (queue.isNotEmpty() && visited.size < maxBlocks * 3) {
			val pos = queue.pop()

			// Skip some blocks before marking them as visited to create slightly unpredictable shapes.
			if (random.nextInt(10) == 0) continue

			val blockState = world.getBlockState(pos)
			if (
				(blockState.isAir && random.nextFloat() < 0.8f)
				|| !blockState.isCancerSpreadable()
			) continue

			out.add(pos)
			if (out.size >= maxBlocks) break

			for (direction in Direction.entries) {
				val nextPos = pos.offset(direction)
				if (!visited.contains(nextPos)) {
					queue.add(nextPos)
					visited.add(nextPos)
				}
			}
		}

		return out
	}

	private fun getSpreadTargetPosition(world: ServerWorld, fromPos: BlockPos, random: Random): BlockPos {
		// Chance for a metastatic jump.
		if (random.nextFloat() < (1f / 1000)) {
			val cancerBlob = world.getBlobMembershipPersistentState()
				.getMembershipOrNull(fromPos)
				?.let(Storage.cancerBlob::getCancerBlobById)

			if (cancerBlob != null && cancerBlob.isMetastatic) {
				val maxJumpDistance = cancerBlob.maxMetastaticJumpDistance
				val candidatePos = BlockPos.Mutable()

				for (attempt in 1..8) {
					candidatePos.x = fromPos.x + random.nextBetweenExclusive(-maxJumpDistance, maxJumpDistance)
					// Prefer jumping sideways.
					candidatePos.y = fromPos.y + random.nextBetweenExclusive(-maxJumpDistance / 2, maxJumpDistance / 3)
					candidatePos.z = fromPos.z + random.nextBetweenExclusive(-maxJumpDistance, maxJumpDistance)

					if (world.getBlockState(candidatePos).isCancerSpreadable()) {
						return candidatePos.toImmutable()
					}
				}
			}
		}

		// Favor horizontal spread.
		val spreadDirection = random.nextOfArray(WEIGHTED_DIRECTIONS)
		return fromPos.offset(spreadDirection)
	}

	fun attemptSpread(world: ServerWorld, pos: BlockPos, random: Random, bypassThrottling: Boolean = false) {
		if (!world.gameRules.getBoolean(PreventTheSpread.DO_CANCER_SPREAD_GAME_RULE)) return

		val spreadPosition = getSpreadTargetPosition(world, pos, random)
		val targetCurrentBlockState = world.getBlockState(spreadPosition)

		if (!targetCurrentBlockState.isCancerSpreadable()) return

		// Prefer spreading to existing blocks over growing out into empty space.
		if (!bypassThrottling && targetCurrentBlockState.isAir && random.nextFloat() <= 0.8f) return

		// 50% chance for a chilling tower to prevent the spread.
		if (random.nextInt(2) == 0) {
			val chillingTowerInRange = world.pointOfInterestStorage
				.getInSquare(
					{ type -> type.matchesKey(PreventTheSpread.CHILLING_TOWER_POI_TYPE) },
					spreadPosition,
					TowerBlock.AREA_OF_EFFECT_HORIZONTAL,
					PointOfInterestStorage.OccupationStatus.ANY,
				)
				.anyMatch { (it.pos.y - spreadPosition.y).absoluteValue <= TowerBlock.AREA_OF_EFFECT_VERTICAL }

			if (chillingTowerInRange) return
		}

		// Spread to blocks which already have a bunch of cancerous neighbors with a higher likelihood.
		val cancerousNeighborCountOfTarget = Direction.entries
			.asSequence()
			.map { spreadPosition.offset(it) }
			.count { world.getBlockState(it).isCancerous() }
		if (!bypassThrottling && random.nextFloat() <= 0.5f - (cancerousNeighborCountOfTarget / 6f * 0.5f)) return

		spreadCancer(world, pos, spreadPosition)
	}

	fun spreadCancer(world: ServerWorld, fromPos: BlockPos, toPos: BlockPos) {
		convertToCancer(world, toPos)

		val blobMembershipPersistentState = world.getBlobMembershipPersistentState()
		val blobId = blobMembershipPersistentState.getMembershipOrNull(fromPos)
		if (blobId != null) {
			blobMembershipPersistentState.setMembership(toPos, blobId)
		}
	}

	fun hastenSpread(world: ServerWorld, pos: BlockPos, random: Random, distance: Int = 3) {
		val box = BlockBox(pos).expand(distance)

		val cancerBlockPositions = box.contentsSequence()
			.filter { world.getBlockState(it).isCancerous() }
			.map { it.toImmutable() }
			.toList()

		for (index in 0..10) {
			val blockPos = random.nextOfListOrNull(cancerBlockPositions) ?: break
			world.scheduleBlockTick(blockPos, world.getBlockState(blockPos).block, index)
		}
		for (index in 0..8) {
			val blockPos = cancerBlockPositions.randomOrNull(kotlin.random.Random) ?: break
			world.scheduleBlockTick(blockPos, world.getBlockState(blockPos).block, 20 + index * 20)
		}
	}

	fun getDefaultCancerBlockState(): BlockState {
		return PreventTheSpread.CANCER_STONE_BLOCK.defaultState
	}

	fun convertToCancer(world: ServerWorld, pos: BlockPos, variantForAir: BlockState = getDefaultCancerBlockState()) {
		val cancerBlockState = convertBlockStateToCancer(world.getBlockState(pos), variantForAir)
		world.setBlockState(pos, cancerBlockState)
	}

	private enum class StateMaterial {
		DIRT,
		LEAVES,
		LOG,
		PLANKS,
		STONE,
	}

	fun convertBlockStateToCancer(state: BlockState, variantForAir: BlockState = getDefaultCancerBlockState()): BlockState {
		// Bail fast for air.
		if (state.isAir) return variantForAir

		// Do not convert block states which are already cancerous.
		if (state.isIn(PreventTheSpread.CANCEROUS_BLOCK_TAG)) return state

		val material = when {
			state.isIn(BlockTags.LEAVES) -> StateMaterial.LEAVES
			state.isIn(BlockTags.LOGS) -> StateMaterial.LOG
			state.isIn(BlockTags.PLANKS)
				|| state.isIn(BlockTags.WOODEN_SLABS)
				|| state.isIn(BlockTags.WOODEN_STAIRS) -> StateMaterial.PLANKS
			// Fallbacks.
			state.isIn(BlockTags.AXE_MINEABLE) -> StateMaterial.PLANKS
			state.isIn(BlockTags.SHOVEL_MINEABLE) -> StateMaterial.DIRT
			else -> StateMaterial.STONE
		}

		return when (material) {
			StateMaterial.DIRT -> convertBlockStateToCancer(
				state,
				PreventTheSpread.CANCER_DIRT_BLOCK,
				PreventTheSpread.CANCER_DIRT_SLAB_BLOCK,
				PreventTheSpread.CANCER_DIRT_STAIRS_BLOCK,
			)
			StateMaterial.LEAVES -> PreventTheSpread.CANCER_LEAVES_BLOCK.getStateWithProperties(state)
			StateMaterial.LOG -> PreventTheSpread.CANCER_LOG_BLOCK.getStateWithProperties(state)
			StateMaterial.PLANKS -> convertBlockStateToCancer(
				state,
				PreventTheSpread.CANCER_PLANKS_BLOCK,
				PreventTheSpread.CANCER_PLANKS_SLAB_BLOCK,
				PreventTheSpread.CANCER_PLANKS_STAIRS_BLOCK,
			)
			StateMaterial.STONE -> convertBlockStateToCancer(
				state,
				PreventTheSpread.CANCER_STONE_BLOCK,
				PreventTheSpread.CANCER_STONE_SLAB_BLOCK,
				PreventTheSpread.CANCER_STONE_STAIRS_BLOCK,
			)
		}
	}

	private fun convertBlockStateToCancer(state: BlockState, solid: Block, slabs: Block, stairs: Block): BlockState {
		return when (state.block) {
			is SlabBlock -> slabs
			is StairsBlock -> stairs
			else -> solid
		}.getStateWithProperties(state)
	}
}