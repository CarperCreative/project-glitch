package com.carpercreative.preventthespread.controller

import com.carpercreative.preventthespread.cancer.CancerLogic.isCancerous
import com.carpercreative.preventthespread.util.contentsSequence
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos

object CancerTouchEffectController {
	fun init() {
		ServerTickEvents.END_WORLD_TICK.register(::onEndWorldTick)
	}

	private fun onEndWorldTick(world: ServerWorld) {
		for (player in world.players) {
			checkCancerousBlockCollisions(world, player)
		}
	}

	private const val EPSILON = 1.0e-5

	private fun checkCancerousBlockCollisions(world: ServerWorld, entity: LivingEntity) {
		if (!entity.isAlive) return
		if (entity.world != world) return

		val bbox = entity.boundingBox
		val fromPos = BlockPos.ofFloored(bbox.minX - EPSILON, bbox.minY - EPSILON, bbox.minZ - EPSILON)
		val toPos = BlockPos.ofFloored(bbox.maxX + EPSILON, bbox.maxY + EPSILON, bbox.maxZ + EPSILON)
		if (world.isRegionLoaded(fromPos, toPos)) {
			for (mutable in BlockBox.create(fromPos, toPos).contentsSequence()) {
				val blockState = world.getBlockState(mutable)

				if (blockState.isCancerous()) {
					onCancerousBlockTouched(world, mutable.toImmutable(), entity)
				}
			}
		}
	}

	private fun onCancerousBlockTouched(world: ServerWorld, blockPos: BlockPos, entity: LivingEntity) {
		if (entity !is ServerPlayerEntity) return
		if (!entity.interactionManager.gameMode.isSurvivalLike) return

		val random = world.random

		when (random.nextInt(1024)) {
			0 -> entity.addStatusEffect(StatusEffectInstance(StatusEffects.HUNGER, random.nextBetween(10, 40)))
			1 -> entity.addStatusEffect(StatusEffectInstance(StatusEffects.POISON, random.nextBetween(10, 60)))
			2 -> entity.addStatusEffect(StatusEffectInstance(StatusEffects.WITHER, random.nextBetween(10, 25)))
		}
	}
}
