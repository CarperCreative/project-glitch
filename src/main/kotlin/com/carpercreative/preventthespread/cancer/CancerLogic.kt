package com.carpercreative.preventthespread.cancer

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.Storage
import com.carpercreative.preventthespread.persistence.BlobMembershipPersistentState.Companion.getBlobMembershipPersistentState
import com.carpercreative.preventthespread.persistence.SpreadDifficultyPersistentState
import com.carpercreative.preventthespread.util.contentsSequence
import com.carpercreative.preventthespread.util.nextOfArray
import com.carpercreative.preventthespread.util.nextOfList
import com.carpercreative.preventthespread.util.nextOfListOrNull
import java.util.LinkedList
import kotlin.math.PI
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

object CancerLogic {
	private val WEIGHTED_DIRECTIONS = arrayOf(
		Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH,
		Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH,
		Direction.DOWN, Direction.UP,
	)

	fun BlockState.isCancerous(): Boolean {
		return isIn(PreventTheSpread.CANCEROUS_BLOCK_TAG)
	}

	/**
	 * @return `true` for blocks which are valid targets for cancer to spread to.
	 * This excludes all cancer blocks and block entities.
	 */
	fun BlockState.isCancerSpreadable(): Boolean {
		// No point spreading to already cancer infested blocks.
		return !isCancerous()
			// Spreading to block entities could have unintended consequences, like dropping the entire contents of a chest.
			&& !hasBlockEntity()
			// Allow spreading only to explicitly whitelisted blocks.
			&& isIn(PreventTheSpread.CANCER_SPREADABLE_BLOCK_TAG)
			// Prevent spreading to unbreakable blocks like bedrock.
			&& block.hardness >= 0f
	}

	fun generateCancerSpawnPos(world: ServerWorld, maxRadius: Float, maxDepth: Int): BlockPos {
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

		// Create the cancer blob.
		return createCancerBlob(world, cancerSpawnPos, spreadDifficulty.blobStartingSize, cancerType)
	}

	fun createCancerBlob(world: ServerWorld, cancerSpawnPos: BlockPos, maxSize: Int, cancerType: CancerType): CancerBlob? {
		val blobMembershipPersistentState = world.getBlobMembershipPersistentState()

		if (world.getBlockState(cancerSpawnPos).isCancerous() || blobMembershipPersistentState.getMembershipOrNull(cancerSpawnPos) != null) return null

		val cancerBlob = Storage.cancerBlob.createCancerBlob { CancerBlob(it, cancerType) }

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

	fun attemptSpread(world: ServerWorld, pos: BlockPos, random: Random, bypassThrottling: Boolean = false) {
		// Favor horizontal spread.
		val spreadDirection = random.nextOfArray(WEIGHTED_DIRECTIONS)
		val spreadPosition = pos.offset(spreadDirection)
		val targetCurrentBlockState = world.getBlockState(spreadPosition)

		if (!targetCurrentBlockState.isCancerSpreadable()) return

		// Prefer spreading to existing blocks over growing out into empty space.
		if (!bypassThrottling && targetCurrentBlockState.isAir && random.nextFloat() <= 0.8f) return

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