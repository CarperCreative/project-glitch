package com.carpercreative.preventthespread.block

import net.minecraft.block.BlockState
import net.minecraft.block.StairsBlock
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import net.minecraft.world.World

class CancerStairsBlock(
	baseBlockState: BlockState,
	settings: Settings,
) : StairsBlock(baseBlockState, settings) {
	override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
		CancerousBlock.randomTick(state, world, pos, random)
	}

	override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
		CancerousBlock.scheduledTick(state, world, pos, random)
	}

	override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
		CancerousBlock.onStateReplaced(state,  world, pos, newState, moved)
	}
}
