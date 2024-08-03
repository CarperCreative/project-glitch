package com.carpercreative.projectglitch.util

import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

fun PlayerEntity.hasAdvancement(identifier: Identifier): Boolean {
	when (this) {
		is ServerPlayerEntity -> {
			val advancement = server.advancementLoader.get(identifier)
				?: return false
			return advancementTracker.getProgress(advancement).isDone
		}
		is ClientPlayerEntity -> {
			val advancementHandler = networkHandler.advancementHandler
			val advancement = advancementHandler.get(identifier)
				?: return false
			val advancementProgress = advancementHandler.advancementProgresses[advancement]
				?: return false
			return advancementProgress.isDone
		}
		else -> throw IllegalArgumentException("Received unknown player type: $this")
	}
}
