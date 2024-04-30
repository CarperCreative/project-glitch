package com.carpercreative.preventthespread.block

import com.carpercreative.preventthespread.blockEntity.ProcessingTableAnalyzerBlockEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.EnumProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess

class ProcessingTableBlock(
	settings: Settings,
) : Block(settings), BlockEntityProvider {
	init {
		defaultState = defaultState
			.with(PROCESSING, false)
	}

	override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
		builder.add(
			FACING,
			PROCESSING,
			PROCESSING_TABLE_PART,
		)
	}

	override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
		val facing = ctx.horizontalPlayerFacing.opposite
		val mainPos = ctx.blockPos

		for (part in ProcessingTablePart.PLACEMENT_ORDER) {
			val counterpartPos = part.getCounterpartBlockPos(facing, mainPos)
			if (!ctx.world.getBlockState(counterpartPos).canReplace(ctx)) continue

			return defaultState
				.with(FACING, facing)
				.with(PROCESSING_TABLE_PART, part)
		}

		// Blocks to the left and right are occupied - can't place the processing table here.
		return null
	}

	override fun onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
		val facing = state.get(FACING)
		val currentPart = state.get(PROCESSING_TABLE_PART)
		val counterpartPos = currentPart.getCounterpartBlockPos(facing, pos)

		// Place the counterpart.
		val counterpartState = defaultState
			.with(FACING, facing)
			.with(PROCESSING_TABLE_PART, currentPart.counterpart)
		world.setBlockState(counterpartPos, counterpartState)
	}

	override fun getStateForNeighborUpdate(state: BlockState, direction: Direction, neighborState: BlockState, world: WorldAccess, pos: BlockPos, neighborPos: BlockPos): BlockState {
		val facing = state.get(FACING)
		val currentPart = state.get(PROCESSING_TABLE_PART)
		if (direction == currentPart.getCounterpartDirection(facing)) {
			if (!neighborState.isOf(this)) {
				return Blocks.AIR.defaultState
			}

			val counterpartProcessing = neighborState.get(PROCESSING)
			if (counterpartProcessing != state.get(PROCESSING)) {
				state.with(PROCESSING, counterpartProcessing)
			}
		}

		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
	}

	override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity): BlockState {
		if (!world.isClient && player.isCreative) {
			val counterpart = state.get(PROCESSING_TABLE_PART).counterpart
			val counterpartPos = counterpart.getCounterpartBlockPos(state.get(FACING), pos)
			val counterpartState = world.getBlockState(counterpartPos)

			if (counterpartState.isOf(this)) {
				world.removeBlock(counterpartPos, false)
			}
		}

		return super.onBreak(world, pos, state, player)
	}

	override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
		if (world.isClient) {
			return ActionResult.SUCCESS
		}

		val blockEntity = world.getBlockEntity(pos)
		if (blockEntity is ProcessingTableAnalyzerBlockEntity) {
			player.openHandledScreen(blockEntity as NamedScreenHandlerFactory)
			// TODO: increment use stat
		}

		return ActionResult.CONSUME
	}

	override fun createBlockEntity(pos: BlockPos, state: BlockState): ProcessingTableAnalyzerBlockEntity? {
		return when (state.get(PROCESSING_TABLE_PART)) {
			ProcessingTablePart.LEFT -> ProcessingTableAnalyzerBlockEntity(pos, state)
			else -> null
		}
	}

	override fun getAmbientOcclusionLightLevel(state: BlockState?, world: BlockView?, pos: BlockPos?): Float {
		return 1f
	}

	enum class ProcessingTablePart(
		private val partName: String,
	) : StringIdentifiable {
		LEFT("left"),
		RIGHT("right"),
		;

		val counterpart
			get() = when (this) {
				LEFT -> RIGHT
				RIGHT -> LEFT
			}

		/**
		 * @param facing Facing direction of the block.
		 * @return Direction towards the other half.
		 */
		fun getCounterpartDirection(facing: Direction): Direction {
			return when (this) {
				LEFT -> facing.rotateYCounterclockwise()
				RIGHT -> facing.rotateYClockwise()
			}
		}

		/**
		 * @param facing Facing direction of the block.
		 * @param pos Position of the block.
		 * @return Position of the other half.
		 */
		fun getCounterpartBlockPos(facing: Direction, pos: BlockPos): BlockPos {
			return pos.offset(getCounterpartDirection(facing))
		}

		override fun toString(): String {
			return partName
		}

		override fun asString(): String {
			return partName
		}

		companion object {
			val PLACEMENT_ORDER = arrayOf(
				LEFT,
				RIGHT,
			)
		}
	}

	companion object {
		val PROCESSING_TABLE_PART: EnumProperty<ProcessingTablePart> = EnumProperty.of("part", ProcessingTablePart::class.java)
		val PROCESSING: BooleanProperty = BooleanProperty.of("processing")
		val FACING: DirectionProperty = Properties.HORIZONTAL_FACING
	}
}
