package com.carpercreative.preventthespread

import com.carpercreative.preventthespread.item.DebugToolItem
import com.carpercreative.preventthespread.item.ProbeItem
import com.carpercreative.preventthespread.item.RadiationStaffItem
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.item.ModelPredicateProviderRegistry
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.entity.TntEntityRenderer
import net.minecraft.util.Identifier

object PreventTheSpreadClient : ClientModInitializer {
	override fun onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlock(PreventTheSpread.PROCESSING_TABLE_BLOCK, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(PreventTheSpread.TARGETED_DRUG_INJECTOR_BLOCK, RenderLayer.getCutout());

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

		EntityRendererRegistry.register(PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_ENTITY_TYPE) { context -> TntEntityRenderer(context) }
	}
}
