package com.carpercreative.preventthespread.cancer

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.persistence.BlobMembershipPersistentState.Companion.getBlobMembershipPersistentState
import com.carpercreative.preventthespread.persistence.CancerBlobPersistentState.Companion.getCancerBlobPersistentState
import com.carpercreative.preventthespread.util.contentsSequence
import com.carpercreative.preventthespread.util.nextOfArray
import com.carpercreative.preventthespread.util.nextOfList
import net.minecraft.block.BlockState
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
			&& (isSolid || isAir)
			// Prevent spreading to unbreakable blocks like bedrock.
			&& block.hardness >= 0f
	}

	fun createCancerBlob(world: ServerWorld, pos: BlockPos, cancerType: CancerType): CancerBlob? {
		val blobMembershipPersistentState = world.getBlobMembershipPersistentState()

		if (world.getBlockState(pos).isCancerous() || blobMembershipPersistentState.getMembershipOrNull(pos) != null) return null

		val cancerBlob = world.getCancerBlobPersistentState().createCancerBlob { CancerBlob(it, cancerType) }
		world.setBlockState(pos, PreventTheSpread.CANCER_STONE_BLOCK.defaultState)
		blobMembershipPersistentState.setMembership(pos, cancerBlob)

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
		val blobMembershipPersistentState = world.getBlobMembershipPersistentState()

		world.setBlockState(toPos, PreventTheSpread.CANCER_STONE_BLOCK.defaultState)

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
			val blockPos = random.nextOfList(cancerBlockPositions) ?: break
			world.scheduleBlockTick(blockPos, world.getBlockState(blockPos).block, index)
		}
		for (index in 0..8) {
			val blockPos = cancerBlockPositions.randomOrNull(kotlin.random.Random) ?: break
			world.scheduleBlockTick(blockPos, world.getBlockState(blockPos).block, 20 + index * 20)
		}
	}
}