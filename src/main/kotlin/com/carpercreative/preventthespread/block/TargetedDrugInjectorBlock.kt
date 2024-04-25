package com.carpercreative.preventthespread.block

import com.carpercreative.preventthespread.cancer.CancerLogic.isCancerous
import com.mojang.serialization.MapCodec
import java.util.LinkedList
import kotlin.jvm.optionals.getOrNull
import kotlin.math.roundToInt
import net.minecraft.block.BarrierBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.RodBlock
import net.minecraft.block.Waterloggable
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.pathing.NavigationType
import net.minecraft.fluid.FluidState
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.IntProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldView

class TargetedDrugInjectorBlock(
	settings: Settings,
) : RodBlock(settings), Waterloggable {
	init {
		defaultState = stateManager.defaultState.with(FACING, Direction.UP)
	}

	override fun getCodec(): MapCodec<out TargetedDrugInjectorBlock> = CODEC

	override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
		builder.add(Properties.FACING)
		builder.add(PROGRESS)
		builder.add(Properties.WATERLOGGED)
	}

	private fun scheduleTick(world: ServerWorld, pos: BlockPos) {
		val progressInterval = 10
		world.scheduleBlockTick(pos, this, progressInterval)
	}

	override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
		var injectionProgress = state.get(PROGRESS)

		// Injection hasn't started - do nothing.
		if (injectionProgress == 0) return

		injectionProgress++
		if (injectionProgress < MAX_PROGRESS) {
			world.setBlockState(pos, state.with(PROGRESS, injectionProgress))
			scheduleTick(world, pos)
			return
		}

		// Destroy self to prevent the injector from ever being possible to reuse.
		world.removeBlock(pos, false)

		// Perform the injection.

		val targetPos = pos.offset(state.get(FACING).opposite)
		val cancerousBlockPositions = BlockSearch.findBlocks(world, targetPos, INJECTED_BLOCK_COUNT)

		for (cancerousBlockPos in cancerousBlockPositions) {
			world.removeBlock(cancerousBlockPos, false)
		}
	}

	override fun randomDisplayTick(state: BlockState, world: World, pos: BlockPos, random: Random) {
		val progress = state.get(PROGRESS)
		val targetPos = pos.offset(state.get(FACING).opposite)
		val cancerousBlockPositions = BlockSearch.findBlocks(world, targetPos, (progress / 8f * INJECTED_BLOCK_COUNT).roundToInt())

		for (cancerousBlockPos in cancerousBlockPositions) {
			world.addBlockBreakParticles(cancerousBlockPos, world.getBlockState(cancerousBlockPos))
		}
	}

	override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
		val fluidState = ctx.world.getFluidState(ctx.blockPos)
		return defaultState
			.with(FACING, ctx.side)
			.with(Properties.WATERLOGGED, fluidState.fluid === Fluids.WATER)
	}

	override fun canPlaceAt(state: BlockState, world: WorldView, pos: BlockPos): Boolean {
		val direction = state.get(FACING)
		val blockPos = pos.offset(direction.opposite)
		val blockState = world.getBlockState(blockPos)
		return blockState.isSideSolidFullSquare(world, blockPos, direction)
	}

	override fun onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
		val updatedState = checkInjectionTarget(state, world, pos)
		if (state != updatedState) {
			world.setBlockState(pos, updatedState, NOTIFY_LISTENERS)
		}

		if (world.isClient) return
		world as ServerWorld

		scheduleTick(world, pos)
	}

	override fun getStateForNeighborUpdate(state: BlockState, direction: Direction, neighborState: BlockState?, world: WorldAccess, pos: BlockPos, neighborPos: BlockPos?): BlockState {
		if (state.get(Properties.WATERLOGGED)) {
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world))
		}

		// Bail quickly if the block update came from a block other than the one this injector is attached to.
		if (state.get(FACING).opposite != direction) return state

		// Drop if we can't attach to the block.
		if (!state.canPlaceAt(world, pos)) {
			return Blocks.AIR.defaultState
		}

		return checkInjectionTarget(state, world, pos)
	}

	/**
	 * Returns a block state with [PROGRESS] appropriate for the block type the injector is facing.
	 *
	 * If attached to a non-cancerous block, the injection progress is reset to 0.
	 * Otherwise, the injection progress is kept at its current value, or set to 1 if currently 0.
	 */
	private fun checkInjectionTarget(state: BlockState, world: WorldAccess, pos: BlockPos): BlockState {
		val attachedToPosition = pos.offset(state.get(FACING).opposite)
		val attachedToCancerous = world.getBlockState(attachedToPosition).isCancerous()

		return when (attachedToCancerous) {
			true -> when (state.get(PROGRESS) == 0) {
				true -> state.with(PROGRESS, 1)
				false -> state
			}
			false -> state.with(PROGRESS, 0)
		}
	}

	override fun getFluidState(state: BlockState): FluidState {
		if (state.get(BarrierBlock.WATERLOGGED)) {
			return Fluids.WATER.getStill(false)
		}
		return super.getFluidState(state)
	}

	override fun getAmbientOcclusionLightLevel(state: BlockState?, world: BlockView?, pos: BlockPos?): Float {
		return 1f
	}

	override fun isTransparent(state: BlockState?, world: BlockView?, pos: BlockPos?): Boolean {
		return true
	}

	override fun canPathfindThrough(state: BlockState?, world: BlockView?, pos: BlockPos?, type: NavigationType?): Boolean {
		return false
	}

	companion object {
		val CODEC = createCodec(::TargetedDrugInjectorBlock)

		/**
		 * Maximum value of [PROGRESS].
		 */
		const val MAX_PROGRESS = 8

		/**
		 * Injection progress. 0 when not facing a valid block, [1..MAX_PROGRESS] while injecting.
		 */
		val PROGRESS = IntProperty.of("progress", 0, MAX_PROGRESS)

		const val INJECTED_BLOCK_COUNT = 24

		fun isInjecting(blockState: BlockState): Boolean {
			return (blockState.getOrEmpty(PROGRESS).getOrNull() ?: 0) != 0
		}
	}

	object BlockSearch {
		fun findBlocks(world: WorldAccess, startPos: BlockPos, limit: Int): List<BlockPos> {
			val visited = hashSetOf<BlockPos>()
			val cancerous = mutableListOf<BlockPos>()
			val candidates = LinkedList<BlockPos>()

			candidates.add(startPos)
			visited.add(startPos)

			while (candidates.isNotEmpty()) {
				val pos = candidates.pop()

				// All found cancerous blocks must be connected.
				if (!world.getBlockState(pos).isCancerous()) continue

				cancerous.add(pos)
				if (cancerous.size >= limit) break

				for (direction in DIRECTIONS) {
					val offsetPos = pos.offset(direction)

					if (!visited.contains(offsetPos)) {
						visited.add(offsetPos)
						candidates.add(offsetPos)
					}
				}
			}

			return cancerous
		}
	}
}