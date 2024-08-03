package com.carpercreative.projectglitch.block

import java.util.function.BiConsumer
import net.minecraft.block.BlockState
import net.minecraft.block.LeavesBlock
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import net.minecraft.world.World
import net.minecraft.world.explosion.Explosion

class CancerLeavesBlock(
	settings: Settings,
) : LeavesBlock(settings) {
	override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
		CancerousBlock.randomTick(state, world, pos, random)

		// Do not call super.randomTick - all it does is check for decay, and we don't want any side effects from other mods.
	}

	override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
		// Update the leaves' distance property.
		super.scheduledTick(state, world, pos, random)

		// FIXME: distance property updates of leaves cause rapid cancer growth
		// CancerousBlock.scheduledTick(world.getBlockState(pos), world, pos, random)
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

	override fun shouldDecay(state: BlockState): Boolean {
		// Cancer leaves never decay.
		return false
	}
}
