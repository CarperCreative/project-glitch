package com.carpercreative.preventthespread.controller

import java.util.LinkedList
import java.util.UUID
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text

object MessageController {
	class QueuedMessage(
		val text: Text,
		val playerUuid: UUID,
	)

	private val playerMessageQueue = mutableMapOf<UUID, LinkedList<QueuedMessage>>()

	private val cooldownPerPlayer = mutableMapOf<UUID, Long>()

	fun init() {
		ServerTickEvents.END_WORLD_TICK.register(::onWorldEndServerTick)
	}

	fun queueMessage(message: QueuedMessage) {
		playerMessageQueue
			.getOrPut(message.playerUuid) { LinkedList() }
				.add(message)
	}

	private fun onWorldEndServerTick(world: ServerWorld) {
		if (world.server.overworld != world) return

		val playerQueueIterator = playerMessageQueue.iterator()
		for ((playerUuid, messages) in playerQueueIterator) {
			if (messages.isEmpty()) {
				playerQueueIterator.remove()
				continue
			}

			val cooldown = cooldownPerPlayer[playerUuid]
			if (cooldown != null) {
				if (cooldown > world.time) {
					continue
				} else {
					cooldownPerPlayer.remove(playerUuid)
				}
			}

			val player = world.server.playerManager.getPlayer(playerUuid)
			val message = messages.pop()
			player?.sendMessage(message.text)
			cooldownPerPlayer[playerUuid] = world.time + 100L

			if (messages.isEmpty()) {
				playerQueueIterator.remove()
			}
		}
	}
}
