package com.carpercreative.preventthespread.entity.ai.brain.task

import com.google.common.collect.ImmutableMap
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.entity.ai.brain.task.MultiTickTask
import net.minecraft.entity.mob.MobEntity
import net.minecraft.server.world.ServerWorld

class HoverNearGroundTask(
	private val chance: Float,
	private val maxDistance: Double,
) : MultiTickTask<MobEntity>(
	ImmutableMap.of(),
) {
	override fun shouldRun(serverWorld: ServerWorld, entity: MobEntity): Boolean {
		return !entity.isOnGround && serverWorld.getCollisions(entity, entity.boundingBox.offset(0.0, -maxDistance, 0.0)).none()
	}

	override fun shouldKeepRunning(serverWorld: ServerWorld, entity: MobEntity, time: Long): Boolean {
		return shouldRun(serverWorld, entity)
	}

	override fun keepRunning(serverWorld: ServerWorld, entity: MobEntity, time: Long) {
		if (entity.random.nextFloat() < this.chance) {
			// Do nothing during navigation.
			if (entity.brain.getOptionalMemory(MemoryModuleType.WALK_TARGET)?.isPresent == true)
				return

			// Probably a bad practice to interact with move control without changing controls or through navigation, but whatever.
			entity.moveControl.moveTo(entity.x, entity.y - 0.2, entity.z, 1.0)
		}
	}
}
