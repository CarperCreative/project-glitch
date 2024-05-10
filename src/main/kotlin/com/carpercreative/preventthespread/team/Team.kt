package com.carpercreative.preventthespread.team

import net.minecraft.entity.player.PlayerEntity

interface Team {
	val players: List<PlayerEntity>

	fun addPlayer(player: PlayerEntity): Boolean

	fun removePlayer(player: PlayerEntity): Boolean
}
