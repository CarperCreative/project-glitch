package com.carpercreative.preventthespread.block

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.blockEntity.ProcessingTableAnalyzerBlockEntity
import com.carpercreative.preventthespread.blockEntity.ProcessingTableResearchBlockEntity
import com.carpercreative.preventthespread.util.grantAdvancement
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
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
import net.minecraft.util.math.MathHelper
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldView

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

	override fun <T : BlockEntity> getTicker(world: World, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? {
		if (world.isClient) return null

		if (type != PreventTheSpread.PROCESSING_TABLE_BLOCK_ENTITY) return null

		@Suppress("UNCHECKED_CAST")
		return when (state.get(PROCESSING_TABLE_PART)) {
			ProcessingTablePart.LEFT -> ProcessingTableAnalyzerBlockEntity.Ticker as BlockEntityTicker<T>
			else -> null
		}
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

		// Let the counterpart read state from the item stack.
		world.getBlockEntity(counterpartPos)?.readNbt(BlockItem.getBlockEntityNbt(itemStack))
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
				return state.with(PROCESSING, counterpartProcessing)
			}
		}

		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
	}

	private fun getAnalyzerBlockEntity(world: WorldView, pos: BlockPos, state: BlockState): ProcessingTableAnalyzerBlockEntity? {
		val analyzerPos = state.get(PROCESSING_TABLE_PART).getPartPosition(state.get(FACING), pos, ProcessingTablePart.LEFT)
		return world.getBlockEntity(analyzerPos) as? ProcessingTableAnalyzerBlockEntity
	}

	private fun getResearchBlockEntity(world: WorldView, pos: BlockPos, state: BlockState): ProcessingTableResearchBlockEntity? {
		val researchPos = state.get(PROCESSING_TABLE_PART).getPartPosition(state.get(FACING), pos, ProcessingTablePart.RIGHT)
		return world.getBlockEntity(researchPos) as? ProcessingTableResearchBlockEntity
	}

	private fun createBlockEntityItemTag(world: WorldView, pos: BlockPos, state: BlockState): NbtCompound? {
		val blockEntityTag = NbtCompound()
		getAnalyzerBlockEntity(world, pos, state)
			?.takeIf { !it.isEmpty }
			?.also { blockEntityTag.copyFrom(it.createNbt()) }
		getResearchBlockEntity(world, pos, state)
			?.takeIf { !it.isEmpty }
			?.also { blockEntityTag.copyFrom(it.createNbt()) }

		if (blockEntityTag.isEmpty) {
			return null
		}

		BlockEntity.writeIdToNbt(blockEntityTag, PreventTheSpread.PROCESSING_TABLE_BLOCK_ENTITY)
		return blockEntityTag
	}

	/**
	 * @return `true` if a tag has been put on the [itemStack].
	 */
	private fun saveBlockEntityItemTag(world: WorldView, pos: BlockPos, state: BlockState, itemStack: ItemStack): Boolean {
		val blockEntityTag = createBlockEntityItemTag(world, pos, state)
			?: return false

		itemStack.setSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY, blockEntityTag)
		return true
	}

	override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity): BlockState {
		if (!world.isClient) {
			world as ServerWorld

			// Drop item without using loot tables due to Minecraft not being made to handle blocks this complex.
			val itemStack = PreventTheSpread.PROCESSING_TABLE_BLOCK_ITEM.defaultStack

			val tableNotEmpty = saveBlockEntityItemTag(world, pos, state, itemStack)

			// Do not drop an item if the player is in creative and the table is empty.
			if (!player.isCreative || tableNotEmpty) {
				val itemEntity = ItemEntity(
					world,
					pos.x.toDouble() + 0.5,
					pos.y.toDouble() + 0.5,
					pos.z.toDouble() + 0.5,
					itemStack,
				)
				itemEntity.setToDefaultPickupDelay()
				world.spawnEntity(itemEntity)
			}
		}

		if (!world.isClient && player.isCreative) {
			val counterpartPos = state.get(PROCESSING_TABLE_PART).getCounterpartBlockPos(state.get(FACING), pos)
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

		when (val blockEntity = world.getBlockEntity(pos)) {
			is ProcessingTableAnalyzerBlockEntity -> {
				player.openHandledScreen(blockEntity as NamedScreenHandlerFactory)
				// TODO: increment use stat
			}
			is ProcessingTableResearchBlockEntity -> {
				// Unlock the root research advancement.
				(player as ServerPlayerEntity).grantAdvancement(PreventTheSpread.ResearchAdvancement.ROOT_ID)

				player.openHandledScreen(blockEntity as NamedScreenHandlerFactory)
				// TODO: increment use stat
			}
		}

		return ActionResult.CONSUME
	}

	override fun createBlockEntity(pos: BlockPos, state: BlockState): LockableContainerBlockEntity? {
		return when (state.get(PROCESSING_TABLE_PART)) {
			ProcessingTablePart.LEFT -> ProcessingTableAnalyzerBlockEntity(pos, state)
			ProcessingTablePart.RIGHT -> ProcessingTableResearchBlockEntity(pos, state)
			else -> null
		}
	}

	override fun getPickStack(world: WorldView, pos: BlockPos, state: BlockState): ItemStack {
		val itemStack = super.getPickStack(world, pos, state)
		saveBlockEntityItemTag(world, pos, state, itemStack)
		return itemStack
	}

	override fun getAmbientOcclusionLightLevel(state: BlockState?, world: BlockView?, pos: BlockPos?): Float {
		return 1f
	}

	override fun getRenderingSeed(state: BlockState, pos: BlockPos): Long {
		return when (val part = state.get(PROCESSING_TABLE_PART)) {
			ProcessingTablePart.LEFT -> MathHelper.hashCode(pos.x, pos.y, pos.z)
			ProcessingTablePart.RIGHT -> {
				val counterpartPos = part.getCounterpartBlockPos(state.get(FACING), pos)
				MathHelper.hashCode(counterpartPos)
			}
		}
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

		/**
		 * @param facing Facing direction of the block.
		 * @param pos Position of the block.
		 * @param targetPart Part the position of which is desired.
		 * @return Position of the desired part.
		 */
		fun getPartPosition(facing: Direction, pos: BlockPos, targetPart: ProcessingTablePart): BlockPos {
			if (targetPart == this) return pos

			return getCounterpartBlockPos(facing, pos)
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
