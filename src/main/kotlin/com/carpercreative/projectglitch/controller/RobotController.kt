package com.carpercreative.projectglitch.controller

import com.carpercreative.projectglitch.ProjectGlitch
import com.carpercreative.projectglitch.ProjectGlitch.ResearchAdvancement
import com.carpercreative.projectglitch.ProjectGlitch.StoryAdvancement
import com.carpercreative.projectglitch.entity.RobotEntity
import com.carpercreative.projectglitch.entity.RobotEntity.Companion.resetDiscardTimer
import com.carpercreative.projectglitch.entity.RobotEntity.Companion.setLikedPlayer
import com.mojang.logging.LogUtils
import kotlin.math.cos
import kotlin.math.sin
import net.minecraft.advancement.AdvancementEntry
import net.minecraft.entity.SpawnReason
import net.minecraft.entity.ai.NoPenaltySolidTargeting
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Box

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

	private fun spawnRobot(player: ServerPlayerEntity): RobotEntity? {
		val world = player.world as ServerWorld

		val robot = world
			// Try to find an existing robot nearby instead of spawning more.
			.getEntitiesByClass(RobotEntity::class.java, Box.of(player.pos, 32.0, 32.0, 32.0)) { true }
			.minByOrNull { it.squaredDistanceTo(player) }
			?.also { robot ->
				robot.resetDiscardTimer()
			}
			// No robot entity found nearby - spawn a new one.
			?: ProjectGlitch.ROBOT_ENTITY_TYPE.spawn(world, null, { robot ->
			// Try to teleport the robot to a nearby location in front of the player to prevent putting it inside the player.
				NoPenaltySolidTargeting.find(robot, 10, 2, 0, cos(player.yaw).toDouble(), sin(player.yaw).toDouble(), Math.PI * 0.4)?.also { spawnPos ->
					// Prevent robot getting lerped out of the player.
					robot.refreshPositionAndAngles(spawnPos.x, spawnPos.y, spawnPos.z, 0f, 0f)
					robot.lookAtEntity(player, Float.MAX_VALUE, Float.MAX_VALUE)
					robot.refreshPositionAndAngles(spawnPos.x, spawnPos.y + 1.0, spawnPos.z, robot.yaw, robot.pitch)
				}
			}, player.blockPos, SpawnReason.COMMAND, false, false)

		if (robot == null) {
			logger.warn("Spawning robot returned null")
			return null
		}

		robot.setLikedPlayer(player.uuid)

		return robot
	}

	private fun createRobotMessage(message: Text): Text {
		return  Text.translatable(
			"chat.type.text",
			Text.translatable(ProjectGlitch.ROBOT_ENTITY_TYPE.translationKey),
			message,
		)
	}

	private fun robotMessage(player: ServerPlayerEntity, advancementIdentifier: Identifier, messageCount: Int): RobotEntity? {
		require(messageCount > 0) { "messageCount has to be greater than 0, is $messageCount" }

		val translationKey = "${ProjectGlitch.MOD_ID}.tutorial.${advancementIdentifier.namespace}.${advancementIdentifier.path.replace('/', '.')}"

		for (index in 0 until messageCount) {
			val text = createRobotMessage(Text.translatable("$translationKey.$index"))
			MessageController.queueMessage(MessageController.QueuedMessage(text, player.uuid))
		}

		return spawnRobot(player)
	}

	private val actionableAdvancements: Map<Identifier, (ServerPlayerEntity) -> Unit> = mapOf(
		StoryAdvancement.ROOT_ID to ({ player ->
			val robot = robotMessage(player, StoryAdvancement.ROOT_ID, 4)

			// Drop kelp to avoid sending the player on a long journey to find it.
			(robot ?: player).dropItem(Items.KELP)
		}),
		StoryAdvancement.OBTAIN_PROBE_ID to ({ player -> robotMessage(player, StoryAdvancement.OBTAIN_PROBE_ID, 3) }),
		StoryAdvancement.OBTAIN_SCANNER_ID to ({ player -> robotMessage(player, StoryAdvancement.OBTAIN_SCANNER_ID, 3) }),
		StoryAdvancement.GET_SAMPLE_ID to ({ player -> robotMessage(player, StoryAdvancement.GET_SAMPLE_ID, 1) }),
		StoryAdvancement.CRAFT_PROCESSING_TABLE_ID to ({ player -> robotMessage(player, StoryAdvancement.CRAFT_PROCESSING_TABLE_ID, 2) }),
		StoryAdvancement.ANALYZE_SAMPLE_ID to ({ player -> robotMessage(player, StoryAdvancement.ANALYZE_SAMPLE_ID, 5) }),
		StoryAdvancement.DEFEAT_BLOB_ID to ({ player -> robotMessage(player, StoryAdvancement.DEFEAT_BLOB_ID, 1) }),
		StoryAdvancement.PROCESS_GLITCH_MATERIAL_ID to ({ player -> robotMessage(player, StoryAdvancement.PROCESS_GLITCH_MATERIAL_ID, 1) }),
		StoryAdvancement.UNLOCK_TREATMENT_ID to ({ player -> robotMessage(player, StoryAdvancement.UNLOCK_TREATMENT_ID, 1) }),
		StoryAdvancement.GAME_OVER_ID to ({ player -> robotMessage(player, StoryAdvancement.GAME_OVER_ID, 2) }),
		ResearchAdvancement.CHEMOTHERAPEUTIC_DRUG_ID to ({ player -> robotMessage(player, ResearchAdvancement.CHEMOTHERAPEUTIC_DRUG_ID, 1) }),
		ResearchAdvancement.CHILLING_TOWER_ID to ({ player -> robotMessage(player, ResearchAdvancement.CHILLING_TOWER_ID, 1) }),
		ResearchAdvancement.RADIATION_STAFF_ID to ({ player -> robotMessage(player, ResearchAdvancement.RADIATION_STAFF_ID, 1) }),
		ResearchAdvancement.SURGERY_EFFICIENCY_1_ID to ({ player -> robotMessage(player, ResearchAdvancement.SURGERY_EFFICIENCY_1_ID, 1) }),
		ResearchAdvancement.TARGETED_DRUG_ID to ({ player -> robotMessage(player, ResearchAdvancement.TARGETED_DRUG_ID, 1) }),
	)
}
