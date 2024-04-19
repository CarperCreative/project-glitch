package com.carpercreative.preventthespread

import com.carpercreative.preventthespread.item.DebugToolItem
import com.carpercreative.preventthespread.item.ProbeItem
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.item.ModelPredicateProviderRegistry
import net.minecraft.util.Identifier

object PreventTheSpreadClient : ClientModInitializer {
	override fun onInitializeClient() {
		ModelPredicateProviderRegistry.register(PreventTheSpread.DEBUG_TOOL_ITEM, Identifier("mode")) { stack, clientWorld, livingEntity, seed ->
			DebugToolItem.getDebugMode(stack).ordinal.toFloat()
		}

		ModelPredicateProviderRegistry.register(PreventTheSpread.PROBE_ITEM, Identifier("sample")) { stack, clientWorld, livingEntity, seed ->
			if (ProbeItem.containsSample(stack)) 1f else 0f
		}
	}
}
