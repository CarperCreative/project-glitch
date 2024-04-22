package com.carpercreative.preventthespread.block

import com.carpercreative.preventthespread.cancer.CancerLogic
import com.carpercreative.preventthespread.persistence.BlobMembershipPersistentState.Companion.getBlobMembershipPersistentState
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.BlockState
import net.minecraft.block.MapColor
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import net.minecraft.world.World

/**
 * Entry points to common logic of all cancerous blocks.
 */
object CancerousBlock {
	fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
		// Prevent spreading on every random tick to keep it manageable.
		if (random.nextFloat() <= 0.5f) return

		CancerLogic.attemptSpread(world, pos, random)
	}

	fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
		// Always attempt spread when the tick has been scheduled to ensure adequate incentives. :)
		CancerLogic.attemptSpread(world, pos, random, bypassThrottling = true)
	}

	fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
		if (!world.isClient()) {
			(world as ServerWorld).getBlobMembershipPersistentState().removeMembership(pos)
		}
	}

	fun defaultBlockSettings(): FabricBlockSettings {
		return FabricBlockSettings.create()
			.mapColor(MapColor.DARK_CRIMSON)
			.pistonBehavior(PistonBehavior.BLOCK)
			.strength(4.0f)
			.ticksRandomly()
	}
}
