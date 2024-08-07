package com.carpercreative.projectglitch.cancer

import com.carpercreative.projectglitch.ProjectGlitch
import com.carpercreative.projectglitch.Storage
import com.carpercreative.projectglitch.block.TowerBlock
import com.carpercreative.projectglitch.cancer.CancerLogic.CancerSpreadEvent
import com.carpercreative.projectglitch.persistence.BlobMembershipPersistentState.Companion.getBlobMembershipPersistentState
import com.carpercreative.projectglitch.persistence.SpreadDifficultyPersistentState
import com.carpercreative.projectglitch.util.contentsSequence
import com.carpercreative.projectglitch.util.nextOfArray
import com.carpercreative.projectglitch.util.nextOfList
import com.carpercreative.projectglitch.util.nextOfListOrNull
import java.util.LinkedList
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.SlabBlock
import net.minecraft.block.StairsBlock
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.tag.BlockTags
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Formatting
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

	fun interface CancerSpreadEvent {
		fun onCancerSpread(world: ServerWorld, fromPos: BlockPos?, toPos: BlockPos)
	}

	val cancerSpreadEvent: Event<CancerSpreadEvent> = EventFactory.createArrayBacked(CancerSpreadEvent::class.java) { callbacks ->
		CancerSpreadEvent { world, fromPos, toPos ->
			callbacks.forEach { it.onCancerSpread(world, fromPos, toPos) }
		}
	}

	fun BlockState.isGlitched(): Boolean {
		return isIn(ProjectGlitch.GLITCHED_BLOCK_TAG)
	}

	private fun BlockState.isGlitchModifiable(): Boolean {
		// No point spreading to already cancer infested blocks.
		return !isGlitched()
			// Spreading to block entities could have unintended consequences, like dropping the entire contents of a chest.
			&& !hasBlockEntity()
			// Prevent spreading to unbreakable blocks like bedrock.
			&& block.hardness >= 0f
	}

	/**
	 * @return `true` for blocks which are valid targets for glitch to spread to.
	 * This excludes all glitch blocks and block entities.
	 */
	fun BlockState.isGlitchable(): Boolean {
		return isGlitchModifiable()
			// Allow spreading only to explicitly whitelisted blocks.
			&& isIn(ProjectGlitch.GLITCHABLE_BLOCK_TAG)
	}

	/**
	 * @return `true` for blocks which are valid targets for a glitch cluster to get spawned from.
	 * This excludes all glitch blocks and block entities.
	 */
	fun BlockState.isValidGlitchSeed(): Boolean {
		return isGlitchModifiable()
			// Never spawn a cluster in air.
			&& !isAir
			// Allow spreading only to explicitly whitelisted blocks.
			&& isIn(ProjectGlitch.VALID_GLITCH_SEED_BLOCK_TAG)
	}

	fun generateCancerSpawnPos(world: ServerWorld, minRadius: Float, maxRadius: Float, maxDepth: Int): BlockPos {
		val minimumY = world.dimension.minY + 8
		val random = world.random

		val bestCancerSpawnPos = BlockPos.Mutable()
		var bestPenalty = Int.MAX_VALUE

		// Attempt to generate a valid position multiple times.
		// Returns the last position if none were deemed valid.
		var attempt = 0
		var invalidAttempts = 0
		nextAttempt@while (attempt <= 10) {
			attempt++

			val angle = random.nextDouble() * PI * 2
			val distance = minRadius + (random.nextDouble() * (maxRadius - minRadius))

			var penalty = 0

			val cancerSpawnPos = BlockPos.Mutable()
			cancerSpawnPos.set(world.spawnPos)
			cancerSpawnPos.x += (sin(angle) * distance).roundToInt()
			cancerSpawnPos.z += (cos(angle) * distance).roundToInt()

			// Force chunk to generate.
			world.getBlockState(cancerSpawnPos)

			cancerSpawnPos.y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, cancerSpawnPos.x, cancerSpawnPos.z) - 1

			if (invalidAttempts > 20) {
				// Fallback in case of worlds where no blocks are valid seed locations.
				// Go up until...
				while (
					// hitting air or a valid cancer seed block,
					!world.getBlockState(cancerSpawnPos).run { isAir || isValidGlitchSeed() }
					// or reaching the height limit (disregards all validity checks).
					&& !world.isOutOfHeightLimit(cancerSpawnPos.y + 1)
				) {
					cancerSpawnPos.y++
				}
				return cancerSpawnPos
			}

			// Ignore positions with an invalid heightmap.
			if (cancerSpawnPos.y <= world.dimension.minY) {
				invalidAttempts++
				attempt--
				continue@nextAttempt
			}

			// Find surface position, ignoring leaves, trees, and fluids.
			var blocksDescendedToFindSurface = 0
			while (cancerSpawnPos.y > minimumY) {
				if (world.getBlockState(cancerSpawnPos).isValidGlitchSeed()) break

				cancerSpawnPos.y--

				// Prevent descending too far below the surface to avoid compounding depth with the max depth from spread difficulty.
				if (blocksDescendedToFindSurface++ > 15) {
					invalidAttempts++
					attempt--
					continue@nextAttempt
				}
			}

			// Penalty for surface being significantly below sea level. [0..128]
			val surfaceLevel = world.seaLevel - 8
			if (cancerSpawnPos.y < surfaceLevel) {
				penalty += (surfaceLevel - cancerSpawnPos.y) * 2
			}

			// Penalty for surface being covered in a fluid.
			for (yOffset in 1..32 step 2) {
				if (world.getFluidState(cancerSpawnPos.offset(Direction.UP, yOffset)).isEmpty) {
					break
				}

				// Checks are twice as dense as fluid checks above the candidate position - penalty should be half of it.
				// 50 -> 40 to make fluids above the candidate spawn position have a higher penalty.
				penalty += 40
			}

			if (maxDepth > 0 && random.nextFloat() < 0.5f) {
				// Hide below the ground, up to the max depth.
				// While descending, check every block, and use the last one which is a valid spawn location.
				var maxDescent = (random.nextFloat() * maxDepth).roundToInt()
				var lastValidY = cancerSpawnPos.y
				while (maxDescent > 0) {
					maxDescent--
					cancerSpawnPos.y--

					if (world.getBlockState(cancerSpawnPos).isValidGlitchSeed()) {
						lastValidY = cancerSpawnPos.y
					}
				}
				cancerSpawnPos.y = lastValidY
			}

			cancerSpawnPos.y = cancerSpawnPos.y.coerceAtLeast(minimumY)

			if (!world.getBlockState(cancerSpawnPos).isValidGlitchSeed()) {
				attempt--
				invalidAttempts++
				continue@nextAttempt
			}

			// Try not to spawn cancer under fluids.
			// If a position under a fluid is reached every attempt, the one with the least fluid above it will be returned.
			if (!world.getFluidState(cancerSpawnPos.offset(Direction.UP)).isEmpty) {

				for (yOffset in 1 .. 30 step 4) {
					if (world.getFluidState(cancerSpawnPos.offset(Direction.UP, yOffset)).isEmpty) break

					penalty += 100
				}
			}

			// Try to spawn away from any players.
			val minimumDistanceToPlayer = 64.0
			val minimumSquaredDistanceToPlayer = minimumDistanceToPlayer.pow(2)
			val playerDistanceModifier = world.players
				.asSequence()
				.filter { it.interactionManager.isSurvivalLike }
				// Distance on the XZ plane.
				.map { (it.x - cancerSpawnPos.x).pow(2) + (it.z - cancerSpawnPos.z).pow(2) }
				.filter { it < minimumSquaredDistanceToPlayer }
				.sumOf { minimumDistanceToPlayer - sqrt(it) }
			if (playerDistanceModifier > 0) {
				penalty += playerDistanceModifier.roundToInt()
			}


			if (penalty < bestPenalty) {
				bestPenalty = penalty
				bestCancerSpawnPos.set(cancerSpawnPos)
			}

			if (penalty <= 0) {
				// Position passed all checks.
				break@nextAttempt
			}
		}

		return bestCancerSpawnPos
	}

	/**
	 * Creates a new [CancerBlob] according to the current [spread difficulty][SpreadDifficultyPersistentState] values, using the given [world]'s randomness.
	 */
	fun createCancerBlob(world: ServerWorld, incrementDifficulty: Boolean = true): CancerBlob? {
		val spreadDifficulty = Storage.spreadDifficulty
		val cancerSpawnPos = generateCancerSpawnPos(world, spreadDifficulty.blobSpawnMinRadius, spreadDifficulty.blobSpawnMaxRadius, spreadDifficulty.maxBlobDepth)

		return createCancerBlob(world, cancerSpawnPos, incrementDifficulty = incrementDifficulty)
	}

	private fun generateTreatments(random: Random, spreadDifficulty: SpreadDifficultyPersistentState): Array<TreatmentType> {
		val reusableTreatment = when {
			spreadDifficulty.defeatedBlobs < 5 -> TreatmentType.SURGERY
			else -> arrayOf(TreatmentType.SURGERY, TreatmentType.RADIATION_THERAPY).let(random::nextOfArray)
		}

		val otherTreatment = random.nextOfList(TreatmentType.entries.filter { it != reusableTreatment })

		return arrayOf(reusableTreatment, otherTreatment)
	}

	/**
	 * Creates a new [CancerBlob] according to the current [spread difficulty][SpreadDifficultyPersistentState] values, using the given [world]'s randomness.
	 */
	fun createCancerBlob(world: ServerWorld, cancerSpawnPos: BlockPos, incrementDifficulty: Boolean = true): CancerBlob? {
		val random = world.random
		val spreadDifficulty = Storage.spreadDifficulty

		// Generate cancer stats.
		val cancerType = when (spreadDifficulty.spawnedBlobs) {
			0 -> CancerType.PRE_CANCEROUS
			1 -> CancerType.EARLY
			in 2..8 -> random.nextOfList(CancerType.entries)
			in 9..15 -> random.nextOfArray(arrayOf(CancerType.EARLY, CancerType.ADVANCED))
			else -> CancerType.ADVANCED
		}

		val treatments = generateTreatments(random, spreadDifficulty)

		val metastaticMaxJumpDistance = when {
			random.nextFloat() < spreadDifficulty.metastaticChance -> spreadDifficulty.metastaticMaxJumpDistance
			else -> 0
		}

		// Create the cancer blob.
		val cancerBlob = createCancerBlob(world, cancerSpawnPos, spreadDifficulty.blobStartingSize, cancerType, treatments, metastaticMaxJumpDistance)

		if (cancerBlob != null && incrementDifficulty) {
			spreadDifficulty.incrementSpawnedBlobs()
		}

		return cancerBlob
	}

	fun createCancerBlob(world: ServerWorld, cancerSpawnPos: BlockPos, maxSize: Int, cancerType: CancerType, treatments: Array<TreatmentType>, maxMetastaticJumpDistance: Int): CancerBlob? {
		val blobMembershipPersistentState = world.getBlobMembershipPersistentState()

		if (world.getBlockState(cancerSpawnPos).isGlitched() || blobMembershipPersistentState.getMembershipOrNull(cancerSpawnPos) != null) return null

		// Always reset the forced spawn time when a new blob is spawned to prevent unplanned spawns.
		Storage.spreadDifficulty.resetForcedSpawnTime(world.server.overworld)

		val cancerBlob = Storage.cancerBlob.createCancerBlob { CancerBlob(it, cancerType, treatments, maxMetastaticJumpDistance) }

		for (blockPos in getBlocksForBlobCreation(world, cancerSpawnPos, maxSize)) {
			convertToCancer(world, blockPos)
			blobMembershipPersistentState.setMembership(blockPos, cancerBlob)

			cancerSpreadEvent.invoker().onCancerSpread(world, null, blockPos)
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
				|| !blockState.isGlitchable()
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
		if (random.nextFloat() < (1f / 500)) {
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

					if (world.getBlockState(candidatePos).isValidGlitchSeed()) {
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
		if (!world.gameRules.getBoolean(ProjectGlitch.DO_GLITCH_SPREAD_GAME_RULE)) return

		val spreadPosition = getSpreadTargetPosition(world, pos, random)
		val targetCurrentBlockState = world.getBlockState(spreadPosition)

		if (!targetCurrentBlockState.isGlitchable()) return

		// Prefer spreading to existing blocks over growing out into empty space.
		// Exception for spreading into air below us, to make cheesing a little more difficult.
		if (!bypassThrottling && targetCurrentBlockState.isAir && random.nextFloat() <= 0.8f && pos.down() != spreadPosition) return

		// 50% chance for a chilling tower to prevent the spread.
		if (random.nextInt(2) == 0) {
			val chillingTowerInRange = world.pointOfInterestStorage
				.getInSquare(
					{ type -> type.matchesKey(ProjectGlitch.CHILLING_TOWER_POI_TYPE) },
					spreadPosition,
					TowerBlock.AREA_OF_EFFECT_HORIZONTAL,
					PointOfInterestStorage.OccupationStatus.ANY,
				)
				.anyMatch { (it.pos.y - spreadPosition.y).absoluteValue <= TowerBlock.AREA_OF_EFFECT_VERTICAL }

			if (chillingTowerInRange) return
		}

		if (!bypassThrottling) {
			val cancerBlobType = world.getBlobMembershipPersistentState()
				.getMembershipOrNull(pos)
				?.let { Storage.cancerBlob.getCancerBlobById(it) }
				?.type
			val baseSpreadChance = cancerBlobType?.baseSpreadChance ?: 0.5f
			val surroundedSpreadChance = cancerBlobType?.surroundedSpreadChance ?: 1f

			// Spread to blocks which already have a bunch of cancerous neighbors with a higher likelihood.
			val cancerousNeighborCountOfTarget = Direction.entries
				.asSequence()
				.map { spreadPosition.offset(it) }
				.count { world.getBlockState(it).isGlitched() }

			val spreadChance = baseSpreadChance + (cancerousNeighborCountOfTarget / 6f * (surroundedSpreadChance - baseSpreadChance))

			if (random.nextFloat() <= 1f - spreadChance) return
		}

		spreadCancer(world, pos, spreadPosition)
	}

	fun spreadCancer(world: ServerWorld, fromPos: BlockPos, toPos: BlockPos) {
		convertToCancer(world, toPos)

		val blobMembershipPersistentState = world.getBlobMembershipPersistentState()
		val blobId = blobMembershipPersistentState.getMembershipOrNull(fromPos)
		if (blobId != null) {
			blobMembershipPersistentState.setMembership(toPos, blobId)
		}

		cancerSpreadEvent.invoker().onCancerSpread(world, fromPos, toPos)
	}

	fun checkMissingAnalysis(world: ServerWorld, pos: BlockPos, player: PlayerEntity?, cancerBlob: CancerBlob) {
		if (cancerBlob.isAnalyzed) return

		// Always warn player if the blob hasn't been analyzed yet.
		if (player != null && player.inventory.containsAny { it.item == ProjectGlitch.SCANNER_ITEM }) {
			player.sendMessage(Text.translatable("${ProjectGlitch.MOD_ID}.analysis_missing_warning").formatted(Formatting.RED), true)
		}

		// Punish skipping analysis, even if the applied treatment is correct.
		if (world.random.nextFloat() < 0.2f) {
			hastenSpread(world, pos, world.random)
		}
	}

	fun hastenSpread(world: ServerWorld, pos: BlockPos, random: Random, distance: Int = 3) {
		val box = BlockBox(pos).expand(distance)

		val cancerBlockPositions = box.contentsSequence()
			.filter { world.getBlockState(it).isGlitched() }
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
		return ProjectGlitch.GLITCH_STONE_BLOCK.defaultState
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
		if (state.isIn(ProjectGlitch.GLITCHED_BLOCK_TAG)) return state

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
				ProjectGlitch.GLITCH_DIRT_BLOCK,
				ProjectGlitch.GLITCH_DIRT_SLAB_BLOCK,
				ProjectGlitch.GLITCH_DIRT_STAIRS_BLOCK,
			)
			StateMaterial.LEAVES -> ProjectGlitch.GLITCH_LEAVES_BLOCK.getStateWithProperties(state)
			StateMaterial.LOG -> ProjectGlitch.GLITCH_LOG_BLOCK.getStateWithProperties(state)
			StateMaterial.PLANKS -> convertBlockStateToCancer(
				state,
				ProjectGlitch.GLITCH_PLANKS_BLOCK,
				ProjectGlitch.GLITCH_PLANKS_SLAB_BLOCK,
				ProjectGlitch.GLITCH_PLANKS_STAIRS_BLOCK,
			)
			StateMaterial.STONE -> convertBlockStateToCancer(
				state,
				ProjectGlitch.GLITCH_STONE_BLOCK,
				ProjectGlitch.GLITCH_STONE_SLAB_BLOCK,
				ProjectGlitch.GLITCH_STONE_STAIRS_BLOCK,
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