package com.carpercreative.preventthespread

import com.carpercreative.preventthespread.client.gui.screen.ProcessingTableAnalyzerScreen
import com.carpercreative.preventthespread.item.DebugToolItem
import com.carpercreative.preventthespread.item.ProbeItem
import com.carpercreative.preventthespread.item.RadiationStaffItem
import com.carpercreative.preventthespread.item.ScannerItem
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.client.item.CompassAnglePredicateProvider
import net.minecraft.client.item.ModelPredicateProviderRegistry
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.entity.TntEntityRenderer
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

object PreventTheSpreadClient : ClientModInitializer {
	override fun onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlock(PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_BLOCK, RenderLayer.getCutout())
		BlockRenderLayerMap.INSTANCE.putBlock(PreventTheSpread.PROCESSING_TABLE_BLOCK, RenderLayer.getCutout())
		BlockRenderLayerMap.INSTANCE.putBlock(PreventTheSpread.TARGETED_DRUG_INJECTOR_BLOCK, RenderLayer.getCutout())

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
		ModelPredicateProviderRegistry.register(PreventTheSpread.SCANNER_ITEM, Identifier("angle"), CompassAnglePredicateProvider { world: ClientWorld?, stack: ItemStack, entity: Entity? ->
			ScannerItem.getTrackedGlobalPosition(stack)
		})

		EntityRendererRegistry.register(PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_ENTITY_TYPE) { context -> TntEntityRenderer(context) }

		HandledScreens.register(PreventTheSpread.PROCESSING_TABLE_ANALYZER_SCREEN_HANDLER, ::ProcessingTableAnalyzerScreen)
	}
}
