package com.carpercreative.preventthespread.entity.ai.brain.task

import com.carpercreative.preventthespread.entity.RobotEntity
import net.minecraft.entity.ai.brain.EntityLookTarget
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.entity.ai.brain.task.Task
import net.minecraft.entity.ai.brain.task.TaskRunnable
import net.minecraft.entity.ai.brain.task.TaskTriggerer

object LookAtLikedPlayerTask {
	fun create(): Task<RobotEntity> {
		return TaskTriggerer.task { context ->
			context
				.group(
					context.queryMemoryValue(MemoryModuleType.LIKED_PLAYER),
					context.queryMemoryAbsent(MemoryModuleType.LOOK_TARGET),
					context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET),
				)
				.apply(context) { likedPlayerUuidResult, lookTarget, _ ->
					TaskRunnable { world, entity, time ->
						val likedPlayerUuid = context.getValue(likedPlayerUuidResult)
						val likedPlayer = world.getEntity(likedPlayerUuid)
							?: return@TaskRunnable false

						lookTarget.remember(EntityLookTarget(likedPlayer, true))

						return@TaskRunnable true
					}
				}
		}
	}
}
