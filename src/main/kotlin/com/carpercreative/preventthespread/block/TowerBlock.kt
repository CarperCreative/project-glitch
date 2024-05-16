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
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.random.Random
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

	override fun randomDisplayTick(state: BlockState, world: World, pos: BlockPos, random: Random) {
		if (state.get(HALF) == DoubleBlockHalf.LOWER) {
			for (index in 1..3) {
				// TODO: use gaussian to bias towards outer edges
				val particlePos = pos.toCenterPos()
					.add(
						random.nextDouble() * 2 * AREA_OF_EFFECT_HORIZONTAL - AREA_OF_EFFECT_HORIZONTAL,
						random.nextDouble() * 2 * AREA_OF_EFFECT_VERTICAL - AREA_OF_EFFECT_VERTICAL,
						random.nextDouble() * 2 * AREA_OF_EFFECT_HORIZONTAL - AREA_OF_EFFECT_HORIZONTAL,
					)

				spawnParticle(state, world, particlePos, random)
			}
		}

		super.randomDisplayTick(state, world, pos, random)
	}

	protected abstract fun spawnParticle(state: BlockState, world: World, particlePos: Vec3d, random: Random)

	companion object {
		const val AREA_OF_EFFECT_HORIZONTAL = 8
		const val AREA_OF_EFFECT_VERTICAL = 6

		init {
			assert(AREA_OF_EFFECT_HORIZONTAL >= AREA_OF_EFFECT_VERTICAL) { "The area of effect of a tower must not be larger vertically than it is horizontally." }
		}

		val HALF: EnumProperty<DoubleBlockHalf>
			get() = Properties.DOUBLE_BLOCK_HALF

		val WATERLOGGED: BooleanProperty
			get() = Properties.WATERLOGGED
	}
}
