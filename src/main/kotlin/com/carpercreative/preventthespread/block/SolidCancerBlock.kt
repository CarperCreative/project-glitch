package com.carpercreative.preventthespread.block

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import net.minecraft.world.World

class SolidCancerBlock(
	settings: Settings,
) : Block(settings) {
	override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
		CancerBlock.cancerousRandomTick(state, world, pos, random)
	}

	override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
		CancerBlock.cancerousScheduledTick(state, world, pos, random)
	}

	override fun onStateReplaced(state: BlockState?, world: World, pos: BlockPos, newState: BlockState?, moved: Boolean) {
		CancerBlock.onCancerousStateReplaced(state,  world, pos, newState, moved)
	}
}
