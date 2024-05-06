package com.carpercreative.preventthespread.cancer

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.persistence.BlobMembershipPersistentState.Companion.getBlobMembershipPersistentState
import com.carpercreative.preventthespread.persistence.CancerBlobPersistentState.Companion.getCancerBlobPersistentState
import com.carpercreative.preventthespread.util.contentsSequence
import com.carpercreative.preventthespread.util.nextOfArray
import com.carpercreative.preventthespread.util.nextOfListOrNull
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

	fun createCancerBlob(world: ServerWorld, pos: BlockPos, cancerType: CancerType): CancerBlob? {
		val blobMembershipPersistentState = world.getBlobMembershipPersistentState()

		if (world.getBlockState(pos).isCancerous() || blobMembershipPersistentState.getMembershipOrNull(pos) != null) return null

		val cancerBlobPersistentState = world.getCancerBlobPersistentState()
		val cancerBlob = cancerBlobPersistentState.createCancerBlob { CancerBlob(it, cancerType) }
		convertToCancer(world, pos)
		blobMembershipPersistentState.setMembership(cancerBlobPersistentState, pos, cancerBlob)

		return cancerBlob
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
			blobMembershipPersistentState.setMembership(world.getCancerBlobPersistentState(), toPos, blobId)
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