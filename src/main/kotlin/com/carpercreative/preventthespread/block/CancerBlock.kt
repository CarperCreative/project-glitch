package com.carpercreative.preventthespread.block

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.cancer.CancerBlob
import com.carpercreative.preventthespread.cancer.CancerType
import com.carpercreative.preventthespread.persistence.BlobMembershipPersistentState.Companion.getBlobMembershipPersistentState
import com.carpercreative.preventthespread.persistence.CancerBlobPersistentState.Companion.getCancerBlobPersistentState
import com.carpercreative.preventthespread.util.nextOfArray
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.World

class CancerBlock(
	settings: Settings,
) : Block(settings) {
	override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
		// Prevent spreading on every random tick to keep it manageable.
		if (random.nextFloat() <= 0.5f) return

		attemptSpread(world, pos, random)
	}

	fun attemptSpread(world: ServerWorld, pos: BlockPos, random: Random) {
		// Favor horizontal spread.
		val spreadDirection = random.nextOfArray(WEIGHTED_DIRECTIONS)
		val spreadPosition = pos.offset(spreadDirection)
		val targetCurrentBlockState = world.getBlockState(spreadPosition)

		if (!targetCurrentBlockState.isCancerSpreadable() || targetCurrentBlockState.isCancerous()) return

		// Prefer spreading to existing blocks over growing out into empty space.
		if (targetCurrentBlockState.isAir && random.nextFloat() <= 0.8f) return

		// Spread to blocks which already have a bunch of cancerous neighbors with a higher likelihood.
		val cancerousNeighborCountOfTarget = DIRECTIONS
			.asSequence()
			.map { spreadPosition.offset(it) }
			.count { world.getBlockState(it).isCancerous() }
		if (random.nextFloat() <= 0.5f - (cancerousNeighborCountOfTarget / 6f * 0.5f)) return

		spreadCancer(world, pos, spreadPosition)
	}

	override fun onStateReplaced(state: BlockState?, world: World, pos: BlockPos, newState: BlockState?, moved: Boolean) {
		if (!world.isClient()) {
			(world as ServerWorld).getBlobMembershipPersistentState().removeMembership(pos)
		}

		super.onStateReplaced(state, world, pos, newState, moved)
	}

	companion object {
		private val WEIGHTED_DIRECTIONS = arrayOf(
			Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH,
			Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH,
			Direction.DOWN, Direction.UP,
		)

		fun BlockState.isCancerous(): Boolean {
			return isOf(PreventTheSpread.CANCER_BLOCK)
		}

		fun BlockState.isCancerSpreadable(): Boolean {
			return !isCancerous() && !hasBlockEntity() && (isSolid || isAir)
		}

		fun createCancerBlob(world: ServerWorld, pos: BlockPos, cancerType: CancerType): CancerBlob? {
			val blobMembershipPersistentState = world.getBlobMembershipPersistentState()

			if (world.getBlockState(pos).isCancerous() || blobMembershipPersistentState.getMembershipOrNull(pos) != null) return null

			val cancerBlob = world.getCancerBlobPersistentState().createCancerBlob { CancerBlob(it, cancerType) }
			world.setBlockState(pos, PreventTheSpread.CANCER_BLOCK.defaultState)
			blobMembershipPersistentState.setMembership(pos, cancerBlob)

			return cancerBlob
		}

		fun spreadCancer(world: ServerWorld, fromPos: BlockPos, toPos: BlockPos) {
			val blobMembershipPersistentState = world.getBlobMembershipPersistentState()

			world.setBlockState(toPos, PreventTheSpread.CANCER_BLOCK.defaultState)

			val blobId = blobMembershipPersistentState.getMembershipOrNull(fromPos)
			if (blobId != null) {
				blobMembershipPersistentState.setMembership(toPos, blobId)
			}
		}
	}
}
