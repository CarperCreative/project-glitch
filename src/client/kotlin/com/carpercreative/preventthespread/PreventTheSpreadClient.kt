package com.carpercreative.preventthespread

import com.carpercreative.preventthespread.item.DebugToolItem
import com.carpercreative.preventthespread.item.ProbeItem
import com.carpercreative.preventthespread.item.RadiationStaffItem
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

		ModelPredicateProviderRegistry.register(PreventTheSpread.RADIATION_STAFF_ITEM, Identifier("heat")) { stack, clientWorld, livingEntity, seed ->
			stack.damage.toFloat() / stack.maxDamage
		}
		ModelPredicateProviderRegistry.register(PreventTheSpread.RADIATION_STAFF_ITEM, Identifier("overheated")) { stack, clientWorld, livingEntity, seed ->
			if (RadiationStaffItem.isOverheated(stack)) 1f else 0f
		}
	}
}
