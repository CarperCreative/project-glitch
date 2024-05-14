package com.carpercreative.preventthespread.block

import java.util.function.BiConsumer
import net.minecraft.block.BlockState
import net.minecraft.block.SlabBlock
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import net.minecraft.world.World
import net.minecraft.world.explosion.Explosion

class CancerSlabBlock(
	settings: Settings,
) : SlabBlock(settings) {
	override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
		CancerousBlock.randomTick(state, world, pos, random)
	}

	override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
		CancerousBlock.scheduledTick(state, world, pos, random)
	}

	override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
		CancerousBlock.onStateReplaced(state,  world, pos, newState, moved)
	}

	override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity): BlockState {
		CancerousBlock.onBreak(world, pos, state, player)
		return super.onBreak(world, pos, state, player)
	}

	override fun onExploded(state: BlockState, world: World, pos: BlockPos, explosion: Explosion, stackMerger: BiConsumer<ItemStack, BlockPos>) {
		CancerousBlock.onExploded(state, world, pos, explosion, stackMerger)
		super.onExploded(state, world, pos, explosion, stackMerger)
	}
}
