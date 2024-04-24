package com.carpercreative.preventthespread.block

import com.carpercreative.preventthespread.entity.ChemotherapeuticDrugEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.explosion.Explosion

class ChemotherapeuticDrugBlock(
	settings: Settings,
) : Block(settings) {
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

	override fun onDestroyedByExplosion(world: World, pos: BlockPos, explosion: Explosion) {
		if (world.isClient) return
		world as ServerWorld

		prime(world, pos, explosion.causingEntity, removeBlock = false) { chemoEntity ->
			val defaultFuse = chemoEntity.fuse
			chemoEntity.fuse = world.random.nextInt(defaultFuse / 4) + defaultFuse / 8
		}
	}

	companion object {
		fun prime(world: ServerWorld, blockPos: BlockPos, igniter: LivingEntity?, removeBlock: Boolean = true, modifyCallback: ((entity: ChemotherapeuticDrugEntity) -> Unit)? = null): ChemotherapeuticDrugEntity {
			if (removeBlock) {
				world.removeBlock(blockPos, false)
			}

			return ChemotherapeuticDrugEntity.spawn(world, blockPos, igniter, modifyCallback)
		}
	}
}
