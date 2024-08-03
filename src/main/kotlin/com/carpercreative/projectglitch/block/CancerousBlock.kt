package com.carpercreative.projectglitch.block

import com.carpercreative.projectglitch.ProjectGlitch
import com.carpercreative.projectglitch.cancer.CancerLogic
import com.carpercreative.projectglitch.cancer.CancerLogic.isGlitched
import com.carpercreative.projectglitch.cancer.TreatmentType
import com.carpercreative.projectglitch.entity.ChemotherapeuticDrugEntity
import com.carpercreative.projectglitch.persistence.BlobMembershipPersistentState.Companion.getBlobMembershipPersistentState
import com.carpercreative.projectglitch.persistence.CancerBlobPersistentState.Companion.getCancerBlobOrNull
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
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import net.minecraft.world.World
import net.minecraft.world.explosion.Explosion

/**
 * Entry points to common logic of all cancerous blocks.
 */
object CancerousBlock {
	fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
		// NOOP
	}

	fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
		// Always attempt spread when the tick has been scheduled to ensure adequate incentives. :)
		CancerLogic.attemptSpread(world, pos, random, bypassThrottling = true)
	}

	private val CANCER_BLOB_DEFEATED_SOUND = SoundEvent.of(ProjectGlitch.identifier("cancer_blob.defeated"))

	fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
		if (world.isClient()) return
		world as ServerWorld

		if (!newState.isGlitched()) {
			val cancerBlob = world.getBlobMembershipPersistentState().removeMembership(pos)

			if (cancerBlob != null && cancerBlob.cancerousBlockCount == 0) {
				if (cancerBlob.isAnalyzed) {
					val itemPos = pos.toCenterPos()
					world.spawnEntity(ItemEntity(world, itemPos.x, itemPos.y, itemPos.z, ProjectGlitch.GLITCH_MATERIAL_ITEM.defaultStack))
				}

				world.playSound(null, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, CANCER_BLOB_DEFEATED_SOUND, SoundCategory.BLOCKS, 1f, 1f)
			}
		}
	}

	fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity) {
		if (world.isClient) return
		world as ServerWorld

		val cancerBlob = world.getCancerBlobOrNull(pos) ?: return

		if (
			!(
				cancerBlob.isTreatmentValid(TreatmentType.SURGERY)
				&& player.mainHandStack.isIn(ProjectGlitch.SURGERY_TOOL_ITEM_TAG)
			)
		) {
			CancerLogic.hastenSpread(world, pos, world.random)
		}

		CancerLogic.checkMissingAnalysis(world, pos, player, cancerBlob)
	}

	fun onExploded(state: BlockState, world: World, pos: BlockPos, explosion: Explosion, stackMerger: BiConsumer<ItemStack, BlockPos>) {
		if (world.isClient) return
		world as ServerWorld

		val cancerBlob = world.getCancerBlobOrNull(pos) ?: return

		val explodingEntity = explosion.entity

		// Don't hasten spread when using entities like creepers.
		if (explodingEntity is LivingEntity) return

		val explodedByChemotherapeuticDrug = explodingEntity?.type == ProjectGlitch.CHEMOTHERAPEUTIC_DRUG_ENTITY_TYPE

		if (
			!cancerBlob.isTreatmentValid(TreatmentType.CHEMOTHERAPY)
			|| !explodedByChemotherapeuticDrug
		) {
			CancerLogic.hastenSpread(world, pos, world.random)
		}

		if (explodedByChemotherapeuticDrug) {
			CancerLogic.checkMissingAnalysis(world, pos, (explodingEntity as? ChemotherapeuticDrugEntity)?.owner as? PlayerEntity, cancerBlob)
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
