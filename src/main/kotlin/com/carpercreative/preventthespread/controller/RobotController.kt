package com.carpercreative.preventthespread.controller

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.entity.RobotEntity.Companion.setLikedPlayer
import com.mojang.logging.LogUtils
import kotlin.math.cos
import kotlin.math.sin
import net.minecraft.advancement.AdvancementEntry
import net.minecraft.entity.SpawnReason
import net.minecraft.entity.ai.NoPenaltySolidTargeting
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Identifier

object RobotController {
	private val logger = LogUtils.getLogger()

	fun onAdvancementMade(player: ServerPlayerEntity, advancementEntry: AdvancementEntry) {
		val advancementAction = actionableAdvancements[advancementEntry.id]
			?: return

		try {
			advancementAction.invoke(player)
		} catch (ex: Exception) {
			logger.error("Failed to perform advancement done action for advancement ${advancementEntry.id}", ex)
		}
	}

	private fun spawnRobot(player: ServerPlayerEntity) {
		val world = player.world as ServerWorld
		// TODO: use existing robot if one exists nearby and is focused on our player

		val robot = PreventTheSpread.ROBOT_ENTITY_TYPE.spawn(world, null, { robot ->
			// Try to teleport the robot to a nearby location in front of the player to prevent putting it inside the player.
			NoPenaltySolidTargeting.find(robot, 10, 2, 0, cos(player.yaw).toDouble(), sin(player.yaw).toDouble(), Math.PI * 0.4)?.also { spawnPos ->
				// Prevent robot getting lerped out of the player.
				robot.refreshPositionAndAngles(spawnPos.x, spawnPos.y, spawnPos.z, 0f, 0f)
				robot.lookAtEntity(player, Float.MAX_VALUE, Float.MAX_VALUE)
				robot.refreshPositionAndAngles(spawnPos.x, spawnPos.y + 1.0, spawnPos.z, robot.yaw, robot.pitch)
			}
		}, player.blockPos, SpawnReason.COMMAND, false, false)
			?: return logger.warn("Spawning robot returned null")

		robot.setLikedPlayer(player.uuid)
	}

	private fun sendRobotMessage(player: ServerPlayerEntity, message: Text) {
		val prefixed = Text.translatable(
			"chat.type.text",
			Text.translatable(PreventTheSpread.ROBOT_ENTITY_TYPE.translationKey),
			message,
		)
		player.sendMessageToClient(prefixed, false)
	}

	private fun robotMessage(player: ServerPlayerEntity, message: Text) {
		spawnRobot(player)
		sendRobotMessage(player, message)
	}

	private val actionableAdvancements: Map<Identifier, (ServerPlayerEntity) -> Unit> = mapOf(
		PreventTheSpread.StoryAdvancement.ROOT_ID to ({ player ->
			robotMessage(player, Text.translatable("preventthespread.tutorial.root"))
		})
	)
}
