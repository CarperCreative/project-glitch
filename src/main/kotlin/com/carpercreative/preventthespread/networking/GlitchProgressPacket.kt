package com.carpercreative.preventthespread.networking

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.client.ClientStorage
import kotlin.math.roundToInt
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.network.PacketByteBuf
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent

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
		private val GLITCH_SPREADS_SOUND_EVENT = SoundEvent.of(PreventTheSpread.identifier("glitch_spreads"))

		fun handle(client: MinecraftClient, handler: ClientPlayNetworkHandler, buf: PacketByteBuf, responseSender: PacketSender) {
			val previousProgress = ClientStorage.glitchProgress
			val packet = GlitchProgressPacket(buf)

			client.submit {
				ClientStorage.glitchProgress = packet.progress

				if (previousProgress > 0f && (packet.progress * 100f).roundToInt() > (previousProgress * 100f).roundToInt()) {
					client.player?.playSound(GLITCH_SPREADS_SOUND_EVENT, SoundCategory.AMBIENT, 1f, 0.3f)
				}
			}
		}
	}
}