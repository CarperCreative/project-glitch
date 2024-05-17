package com.carpercreative.preventthespread.block

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.Waterloggable
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.EnumProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.random.Random
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess

abstract class TowerBlock(
	settings: Settings,
) : Block(settings), Waterloggable {
	init {
		defaultState = stateManager.defaultState
			.with(PART, TowerPart.BOTTOM)
			.with(WATERLOGGED, false)
	}

	override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
		builder.add(
			PART,
			WATERLOGGED,
		)
	}

	override fun getStateForNeighborUpdate(state: BlockState, direction: Direction, neighborState: BlockState, world: WorldAccess, pos: BlockPos, neighborPos: BlockPos): BlockState {
		val part = state.get(PART)
		if (
			(
				(part == TowerPart.BOTTOM && direction == Direction.UP)
				|| (part == TowerPart.MIDDLE && direction.axis == Direction.Axis.Y)
				|| (part == TowerPart.TOP && direction == Direction.DOWN)
			)
			&& (!neighborState.isOf(this) || neighborPos.y - neighborState.get(PART).y != pos.y - part.y)
		) {
			return Blocks.AIR.defaultState
		}

		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
	}

	override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
		val blockPos = ctx.blockPos
		val world = ctx.world
		if (blockPos.y >= world.topY - 1) return null
		if (!world.getBlockState(blockPos.up(1)).canReplace(ctx)) return null
		if (!world.getBlockState(blockPos.up(2)).canReplace(ctx)) return null

		return defaultState
			.with(WATERLOGGED, world.getFluidState(blockPos).fluid == Fluids.WATER)
	}

	override fun onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
		val part = state.get(PART)
		if (part == TowerPart.BOTTOM) {
			for (counterpart in part.counterparts) {
				val counterpartPos = pos.up(counterpart.y)
				val counterpartState = state
					.with(PART, counterpart)
					.with(WATERLOGGED, world.getFluidState(counterpartPos).fluid == Fluids.WATER)
				world.setBlockState(counterpartPos, counterpartState, NOTIFY_ALL)
			}
		}
		super.onPlaced(world, pos, state, placer, itemStack)
	}

	override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity): BlockState {
		if (!world.isClient) {
			val part = state.get(PART)
			if (player.isCreative && part != TowerPart.BOTTOM) {
				// Break the bottom to prevent it from dropping items.
				// The other parts get broken due to neighbor updates.
				val counterpart = TowerPart.BOTTOM
				val counterpartPos = pos.down(part.y - counterpart.y)
				val counterpartState = world.getBlockState(counterpartPos)
				if (counterpartState.isOf(this) && counterpartState.get(PART) == counterpart) {
					world.setBlockState(counterpartPos, if (counterpartState.fluidState.fluid == Fluids.WATER) Blocks.WATER.defaultState else Blocks.AIR.defaultState, NOTIFY_ALL or SKIP_DROPS)
				}
			}
		}

		return super.onBreak(world, pos, state, player)
	}

	override fun getRenderingSeed(state: BlockState, pos: BlockPos): Long {
		return MathHelper.hashCode(pos.x, pos.y - state.get(PART).y, pos.z)
	}

	override fun getAmbientOcclusionLightLevel(state: BlockState, world: BlockView, pos: BlockPos): Float {
		return 0.8f
	}

	override fun randomDisplayTick(state: BlockState, world: World, pos: BlockPos, random: Random) {
		if (state.get(PART) == TowerPart.MIDDLE) {
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

	enum class TowerPart(
		private val partName: String,
		val y: Int,
	) : StringIdentifiable {
		TOP("top", 2),
		MIDDLE("middle", 1),
		BOTTOM("bottom", 0),
		;

		override fun toString(): String {
			return partName
		}

		override fun asString(): String {
			return partName
		}

		val counterparts: Array<TowerPart>
			get() = when (this) {
				TOP -> arrayOf(MIDDLE, BOTTOM)
				MIDDLE -> arrayOf(TOP, BOTTOM)
				BOTTOM -> arrayOf(TOP, MIDDLE)
			}
	}

	companion object {
		const val AREA_OF_EFFECT_HORIZONTAL = 8
		const val AREA_OF_EFFECT_VERTICAL = 6

		init {
			assert(AREA_OF_EFFECT_HORIZONTAL >= AREA_OF_EFFECT_VERTICAL) { "The area of effect of a tower must not be larger vertically than it is horizontally." }
		}

		val PART: EnumProperty<TowerPart>
			get() = EnumProperty.of("part", TowerPart::class.java)

		val WATERLOGGED: BooleanProperty
			get() = Properties.WATERLOGGED
	}
}
