package com.carpercreative.preventthespread.block

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.TallPlantBlock
import net.minecraft.block.Waterloggable
import net.minecraft.block.enums.DoubleBlockHalf
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.EnumProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import net.minecraft.world.WorldAccess

abstract class TowerBlock(
	settings: Settings,
) : Block(settings), Waterloggable {
	init {
		defaultState = stateManager.defaultState
			.with(HALF, DoubleBlockHalf.LOWER)
			.with(WATERLOGGED, false)
	}

	override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
		builder.add(
			HALF,
			WATERLOGGED,
		)
	}

	override fun getStateForNeighborUpdate(state: BlockState, direction: Direction, neighborState: BlockState, world: WorldAccess, pos: BlockPos, neighborPos: BlockPos): BlockState {
		val half = state.get(HALF)
			if (
				half.oppositeDirection == direction
				&& (!neighborState.isOf(this) || neighborState.get(HALF) != half.otherHalf)
			) {
				return Blocks.AIR.defaultState
			}

		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
	}

	override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
		val blockPos = ctx.blockPos
		val world = ctx.world
		if (blockPos.y >= world.topY || !world.getBlockState(blockPos.up()).canReplace(ctx)) return null

		return defaultState
			.with(WATERLOGGED, world.getFluidState(blockPos).fluid == Fluids.WATER)
	}

	override fun onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
		if (state.get(HALF) == DoubleBlockHalf.LOWER) {
			val neighborPos = pos.up()
			val neighborState = state
				.with(HALF, DoubleBlockHalf.UPPER)
				.with(WATERLOGGED, world.getFluidState(neighborPos).fluid == Fluids.WATER)
			world.setBlockState(neighborPos, neighborState, NOTIFY_ALL)
		}
		super.onPlaced(world, pos, state, placer, itemStack)
	}

	override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity): BlockState {
		if (!world.isClient) {
			if (player.isCreative && state.get(HALF) == DoubleBlockHalf.UPPER) {
				val counterpartPos = pos.down()
				val counterpartState = world.getBlockState(counterpartPos)
				if (counterpartState.isOf(this) && counterpartState.get(HALF) == DoubleBlockHalf.LOWER) {
					world.setBlockState(counterpartPos, if (counterpartState.fluidState.fluid == Fluids.WATER) Blocks.WATER.defaultState else Blocks.AIR.defaultState, NOTIFY_ALL or SKIP_DROPS)
				}
			}
		}

		return super.onBreak(world, pos, state, player)
	}

	override fun getRenderingSeed(state: BlockState, pos: BlockPos): Long {
		return MathHelper.hashCode(pos.x, pos.down(if (state.get(TallPlantBlock.HALF) == DoubleBlockHalf.LOWER) 0 else 1).y, pos.z)
	}

	companion object {
		val HALF: EnumProperty<DoubleBlockHalf>
			get() = Properties.DOUBLE_BLOCK_HALF

		val WATERLOGGED: BooleanProperty
			get() = Properties.WATERLOGGED
	}
}
