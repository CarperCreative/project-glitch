package com.carpercreative.preventthespread.networking

import com.carpercreative.preventthespread.client.ClientStorage
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.network.PacketByteBuf

class GlitchProgressPacket(
	progress: Float,
) {
	constructor(buf: PacketByteBuf) : this(
		buf.readFloat(),
	)

	val progress: Float = progress.coerceIn(0f, 1f)

	fun write(buf: PacketByteBuf) {
		buf.writeFloat(progress)
	}

	companion object {
		fun handle(client: MinecraftClient, handler: ClientPlayNetworkHandler, buf: PacketByteBuf, responseSender: PacketSender) {
			val packet = GlitchProgressPacket(buf)

			ClientStorage.glitchProgress = packet.progress
		}
	}
}