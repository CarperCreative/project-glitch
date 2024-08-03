package com.carpercreative.projectglitch.networking

import com.carpercreative.projectglitch.screen.ProcessingTableResearchScreenHandler
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

class PerformResearchPacket(
	val researchAdvancementId: Identifier,
) {
	constructor(buf: PacketByteBuf) : this(
		buf.readIdentifier(),
	)

	fun write(buf: PacketByteBuf) {
		buf.writeIdentifier(researchAdvancementId)
	}

	companion object {
		fun handle(server: MinecraftServer, player: ServerPlayerEntity, handler: ServerPlayNetworkHandler, buf: PacketByteBuf, responseSender: PacketSender) {
			val screenHandler = player.currentScreenHandler as? ProcessingTableResearchScreenHandler
				?: return

			val packet = PerformResearchPacket(buf)

			screenHandler.onResearchPerformRequest(packet.researchAdvancementId)
		}
	}
}
