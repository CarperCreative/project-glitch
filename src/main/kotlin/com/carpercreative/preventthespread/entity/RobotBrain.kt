package com.carpercreative.preventthespread.entity

import com.carpercreative.preventthespread.entity.ai.brain.task.AvoidGroundTask
import com.carpercreative.preventthespread.entity.ai.brain.task.DiscardEntityTask
import com.carpercreative.preventthespread.entity.ai.brain.task.HoverNearGroundTask
import com.carpercreative.preventthespread.entity.ai.brain.task.LookAtLikedPlayerTask
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import java.util.Optional
import kotlin.jvm.optionals.getOrNull
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.brain.Activity
import net.minecraft.entity.ai.brain.Brain
import net.minecraft.entity.ai.brain.EntityLookTarget
import net.minecraft.entity.ai.brain.LookTarget
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.entity.ai.brain.task.LookAroundTask
import net.minecraft.entity.ai.brain.task.StayAboveWaterTask
import net.minecraft.entity.ai.brain.task.WalkTowardsLookTargetTask
import net.minecraft.entity.ai.brain.task.WanderAroundTask
import net.minecraft.server.world.ServerWorld

object RobotBrain {
	fun create(brain: Brain<RobotEntity>): Brain<RobotEntity> {
		addCoreActivities(brain)
		addIdleActivities(brain)
		brain.setCoreActivities(ImmutableSet.of(Activity.CORE))
		brain.setDefaultActivity(Activity.IDLE)
		brain.resetPossibleActivities()
		return brain
	}

	private fun addCoreActivities(brain: Brain<RobotEntity>) {
		brain.setTaskList(
			Activity.CORE,
			0,
			ImmutableList.of(
				StayAboveWaterTask(1f),
				AvoidGroundTask(0.75f, 0.8),
				HoverNearGroundTask(0.75f, 1.5),
				LookAroundTask(45, 90),
				WanderAroundTask(),
				DiscardEntityTask(15 * 20),
			),
		)
	}

	private fun addIdleActivities(brain: Brain<RobotEntity>) {
		brain.setTaskList(
			Activity.IDLE,
			0,
			ImmutableList.of(
				LookAtLikedPlayerTask.create(),
				WalkTowardsLookTargetTask.create(
					::getLookTarget,
					{ true },
					8,
					5,
					1.0f,
				),
			),
		)
	}

	fun updateActivities(robot: RobotEntity) {
		robot.brain.resetPossibleActivities(ImmutableList.of(Activity.IDLE))
	}

	private fun getLookTarget(entity: LivingEntity): Optional<LookTarget> {
		val likedPlayer = entity.brain.getOptionalRegisteredMemory(MemoryModuleType.LIKED_PLAYER).getOrNull()
			?.let { (entity.world as ServerWorld).getPlayerByUuid(it) }
			?.takeIf { it.isPartOfGame }
			?: return Optional.empty()

		return Optional.of(EntityLookTarget(likedPlayer, true))
	}
}
