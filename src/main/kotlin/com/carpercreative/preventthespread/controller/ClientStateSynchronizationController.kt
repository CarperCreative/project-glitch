package com.carpercreative.preventthespread.controller

import com.carpercreative.preventthespread.ChallengeConstants
import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.Storage
import com.carpercreative.preventthespread.networking.GlitchProgressPacket
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.MinecraftServer

object ClientStateSynchronizationController {
	fun init() {
		ServerTickEvents.END_SERVER_TICK.register { server ->
			tick(server)
		}
	}

	private fun tick(server: MinecraftServer) {
		if (server.ticks % 20 == 6) {
			val dangerLevel = Storage.cancerBlob.getTotalCancerousBlockCount() / ChallengeConstants.CANCEROUS_BLOCK_LIMIT.toFloat()
			val packetBuf = PacketByteBufs.create().also { GlitchProgressPacket(dangerLevel).write(it) }
			for (player in server.playerManager.playerList) {
				ServerPlayNetworking.send(player, PreventTheSpread.GLITCH_PROGRESS_PACKET_ID, packetBuf)
			}
		}
	}
}
