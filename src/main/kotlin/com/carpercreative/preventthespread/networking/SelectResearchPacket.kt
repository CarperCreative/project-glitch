package com.carpercreative.preventthespread.networking

import com.carpercreative.preventthespread.screen.ProcessingTableResearchScreenHandler
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

class SelectResearchPacket(
	val researchAdvancementId: Identifier?,
) {
	constructor(buf: PacketByteBuf) : this(
		when (val action = buf.readByte()) {
			ACTION_SET -> buf.readIdentifier()
			ACTION_UNSET -> null
			else -> throw IllegalArgumentException("Invalid action received: $action")
		}
	)

	fun write(buf: PacketByteBuf) {
		if (researchAdvancementId == null) {
			buf.writeByte(ACTION_UNSET.toInt())
		} else {
			buf.writeByte(ACTION_SET.toInt())
			buf.writeIdentifier(researchAdvancementId)
		}
	}

	companion object {
		private const val ACTION_SET: Byte = 0
		private const val ACTION_UNSET: Byte = 1

		fun handle(server: MinecraftServer, player: ServerPlayerEntity, handler: ServerPlayNetworkHandler, buf: PacketByteBuf, responseSender: PacketSender) {
			val screenHandler = player.currentScreenHandler as? ProcessingTableResearchScreenHandler
				?: return

			val packet = SelectResearchPacket(buf)

			screenHandler.onResearchSelected(packet.researchAdvancementId)
		}
	}
}
