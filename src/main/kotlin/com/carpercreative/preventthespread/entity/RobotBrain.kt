package com.carpercreative.preventthespread.entity

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.mojang.datafixers.util.Pair
import net.minecraft.entity.ai.brain.Activity
import net.minecraft.entity.ai.brain.Brain
import net.minecraft.entity.ai.brain.MemoryModuleState
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.entity.ai.brain.task.GoTowardsLookTargetTask
import net.minecraft.entity.ai.brain.task.LookAroundTask
import net.minecraft.entity.ai.brain.task.RandomTask
import net.minecraft.entity.ai.brain.task.StayAboveWaterTask
import net.minecraft.entity.ai.brain.task.StrollTask
import net.minecraft.entity.ai.brain.task.WaitTask
import net.minecraft.entity.ai.brain.task.WanderAroundTask

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
				LookAroundTask(45, 90),
				WanderAroundTask(),
			),
		)
	}

	private fun addIdleActivities(brain: Brain<RobotEntity>) {
		brain.setTaskList(
			Activity.IDLE,
			ImmutableList.of(
				Pair.of(0, RandomTask(ImmutableList.of(
					Pair.of(StrollTask.createSolidTargeting(1.0f), 2),
					Pair.of(GoTowardsLookTargetTask.create(1.0f, 3), 2),
					Pair.of(WaitTask(30, 60), 1)
				))),
			),
			ImmutableSet.of<Pair<MemoryModuleType<*>, MemoryModuleState>>(),
		)
	}

	fun updateActivities(robot: RobotEntity) {
		robot.brain.resetPossibleActivities(ImmutableList.of(Activity.IDLE))
	}
}
