package com.carpercreative.projectglitch.block

import com.carpercreative.projectglitch.ProjectGlitch
import com.carpercreative.projectglitch.entity.ChemotherapeuticDrugEntity
import com.carpercreative.projectglitch.util.getChemotherapeuticDrugStrength
import java.util.function.BiConsumer
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.IntProperty
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.explosion.Explosion

class ChemotherapeuticDrugBlock(
	settings: Settings,
) : Block(settings) {
	override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
		builder.add(STRENGTH)
	}

	override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
		if (player.isSneaking) return ActionResult.PASS
		// Never prime while player is holding the block.
		if (player.isHolding { (it.item as? BlockItem)?.block is ChemotherapeuticDrugBlock }) return ActionResult.PASS

		if (!world.isClient) {
			world as ServerWorld

			prime(world, pos, player)
		}

		return ActionResult.SUCCESS
	}

	override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
		return defaultState
			.with(STRENGTH, ctx.player?.getChemotherapeuticDrugStrength() ?: 0)
	}

	override fun onBlockAdded(state: BlockState, world: World, pos: BlockPos, oldState: BlockState, notify: Boolean) {
		if (world.isClient) return
		world as ServerWorld
		if (oldState.isOf(state.block)) return

		if (world.isReceivingRedstonePower(pos)) {
			prime(world, pos, null)
		}
	}

	override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos, sourceBlock: Block, sourcePos: BlockPos, notify: Boolean) {
		if (world.isClient) return
		world as ServerWorld

		if (world.isReceivingRedstonePower(pos)) {
			prime(world, pos, null)
		}
	}

	override fun shouldDropItemsOnExplosion(explosion: Explosion?): Boolean {
		return false
	}

	/**
	 * We're overriding this function because the original removes the block from the world before we can read its strength from the [BlockState].
 	 */
	override fun onExploded(state: BlockState, world: World, pos: BlockPos, explosion: Explosion, stackMerger: BiConsumer<ItemStack, BlockPos>) {
		if (
			!world.isClient
			&& state.isOf(this)
			&& explosion.destructionType != Explosion.DestructionType.TRIGGER_BLOCK
		) {
			world as ServerWorld

			prime(world, pos, explosion.causingEntity, removeBlock = false) { chemoEntity ->
				val defaultFuse = chemoEntity.fuse
				chemoEntity.fuse = world.random.nextInt(defaultFuse / 4) + defaultFuse / 8
			}
		}

		super.onExploded(state, world, pos, explosion, stackMerger)
	}

	companion object {
		val STRENGTH = IntProperty.of("strength", 0, 2)

		fun prime(world: ServerWorld, blockPos: BlockPos, igniter: LivingEntity?, removeBlock: Boolean = true, modifyCallback: ((entity: ChemotherapeuticDrugEntity) -> Unit)? = null): ChemotherapeuticDrugEntity {
			val state = world.getBlockState(blockPos)
			val strength = when {
				state.isOf(ProjectGlitch.CHEMOTHERAPEUTIC_DRUG_BLOCK) -> state.get(STRENGTH)
				else -> 0
			}

			if (removeBlock) {
				world.removeBlock(blockPos, false)
			}

			return ChemotherapeuticDrugEntity.spawn(world, blockPos, igniter, strength, modifyCallback)
		}
	}
}
