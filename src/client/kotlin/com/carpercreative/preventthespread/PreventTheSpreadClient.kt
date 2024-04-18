package com.carpercreative.preventthespread

import com.carpercreative.preventthespread.item.DebugToolItem
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.item.ModelPredicateProviderRegistry
import net.minecraft.util.Identifier

object PreventTheSpreadClient : ClientModInitializer {
	override fun onInitializeClient() {
		ModelPredicateProviderRegistry.register(PreventTheSpread.DEBUG_TOOL_ITEM, Identifier("mode")) { itemStack, clientWorld, livingEntity, seed ->
			DebugToolItem.getDebugMode(itemStack).ordinal.toFloat()
		}
	}
}
