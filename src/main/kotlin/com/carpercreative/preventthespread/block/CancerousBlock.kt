package com.carpercreative.preventthespread.block

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.cancer.CancerLogic
import com.carpercreative.preventthespread.cancer.CancerLogic.isCancerous
import com.carpercreative.preventthespread.cancer.TreatmentType
import com.carpercreative.preventthespread.persistence.BlobMembershipPersistentState.Companion.getBlobMembershipPersistentState
import com.carpercreative.preventthespread.persistence.CancerBlobPersistentState.Companion.getCancerBlobOrNull
import java.util.function.BiConsumer
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.BlockState
import net.minecraft.block.MapColor
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import net.minecraft.world.World
import net.minecraft.world.explosion.Explosion

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
		if (world.isClient()) return
		world as ServerWorld

		if (!newState.isCancerous()) {
			val cancerBlob = world.getBlobMembershipPersistentState().removeMembership(pos)

			if (cancerBlob != null && cancerBlob.cancerousBlockCount == 0) {
				val itemPos = pos.toCenterPos()
				world.spawnEntity(ItemEntity(world, itemPos.x, itemPos.y, itemPos.z, PreventTheSpread.CANCEROUS_MATERIAL_ITEM.defaultStack))
			}
		}
	}

	fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity) {
		if (world.isClient) return
		world as ServerWorld

		val cancerBlob = world.getCancerBlobOrNull(pos) ?: return

		if (
			!(
				cancerBlob.type.isTreatmentValid(TreatmentType.SURGERY)
				&& player.mainHandStack.isIn(PreventTheSpread.SURGERY_TOOL_ITEM_TAG)
			)
		) {
			CancerLogic.hastenSpread(world, pos, world.random)
		}
	}

	fun onExploded(state: BlockState, world: World, pos: BlockPos, explosion: Explosion, stackMerger: BiConsumer<ItemStack, BlockPos>) {
		if (world.isClient) return
		world as ServerWorld

		val cancerBlob = world.getCancerBlobOrNull(pos) ?: return

		val explodingEntity = explosion.entity

		// Don't hasten spread when using entities like creepers.
		if (explodingEntity is LivingEntity) return

		if (
			!cancerBlob.type.isTreatmentValid(TreatmentType.CHEMOTHERAPY)
			|| explodingEntity.let { it == null || it.type != PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_ENTITY_TYPE }
		) {
			CancerLogic.hastenSpread(world, pos, world.random)
		}
	}

	fun defaultBlockSettings(): FabricBlockSettings {
		return FabricBlockSettings.create()
			.mapColor(MapColor.DARK_CRIMSON)
			.pistonBehavior(PistonBehavior.BLOCK)
			.strength(3.0f)
			.ticksRandomly()
	}
}
