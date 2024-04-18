package com.carpercreative.preventthespread.block

import com.carpercreative.preventthespread.PreventTheSpread
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random

class CancerBlock(
	settings: Settings,
) : Block(settings) {
	override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
		// Prevent spreading on every random tick to keep it manageable.
		if (random.nextFloat() <= 0.5f) return

		// Favor horizontal spread.
		val spreadDirection = WEIGHTED_DIRECTIONS[random.nextBetweenExclusive(0, WEIGHTED_DIRECTIONS.size)]
		val spreadPosition = pos.offset(spreadDirection)
		val targetCurrentBlockState = world.getBlockState(spreadPosition)

		if (!targetCurrentBlockState.isCancerSpreadable()) return

		// Prefer spreading to existing blocks over growing out into empty space.
		if (targetCurrentBlockState.isAir && random.nextFloat() <= 0.8f) return

		// Spread to blocks which already have a bunch of cancerous neighbors with a higher likelihood.
		val cancerousNeighborCountOfTarget = DIRECTIONS
			.asSequence()
			.map { spreadPosition.offset(it) }
			.count { world.getBlockState(it).isCancerous() }
		if (random.nextFloat() <= 0.5f - (cancerousNeighborCountOfTarget / 6f * 0.5f)) return

		world.setBlockState(spreadPosition, PreventTheSpread.CANCER_BLOCK.defaultState)
	}

	private fun BlockState.isCancerous(): Boolean {
		return isOf(PreventTheSpread.CANCER_BLOCK)
	}

	private fun BlockState.isCancerSpreadable(): Boolean {
		return !isCancerous() && !hasBlockEntity() && (isSolid || isAir)
	}

	companion object {
		private val WEIGHTED_DIRECTIONS = arrayOf(
			Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH,
			Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH,
			Direction.DOWN, Direction.UP,
		)
	}
}
