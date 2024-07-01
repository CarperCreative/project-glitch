package com.carpercreative.preventthespread.entity.ai.brain.task

import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.brain.task.MultiTickTask
import net.minecraft.server.world.ServerWorld

class DiscardEntityTask(
	delay: Int,
) : MultiTickTask<LivingEntity>(
	emptyMap(),
	delay,
) {
	override fun shouldKeepRunning(serverWorld: ServerWorld?, entity: LivingEntity, l: Long): Boolean {
		return entity.removalReason == null
	}

	override fun finishRunning(world: ServerWorld?, entity: LivingEntity, time: Long) {
		if (entity.removalReason == null) {
			entity.remove(Entity.RemovalReason.DISCARDED)
		}
	}
}
