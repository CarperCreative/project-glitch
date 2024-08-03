package com.carpercreative.projectglitch.client

import com.carpercreative.projectglitch.ProjectGlitch
import com.carpercreative.projectglitch.client.gui.screen.ProcessingTableAnalyzerScreen
import com.carpercreative.projectglitch.client.gui.screen.ProcessingTableResearchScreen
import com.carpercreative.projectglitch.client.render.entity.RobotEntityRenderer
import com.carpercreative.projectglitch.client.render.entity.model.RobotEntityModel
import com.carpercreative.projectglitch.item.DebugToolItem
import com.carpercreative.projectglitch.item.ProbeItem
import com.carpercreative.projectglitch.item.RadiationStaffItem
import com.carpercreative.projectglitch.item.ScannerItem
import com.carpercreative.projectglitch.networking.GlitchProgressPacket
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.client.item.CompassAnglePredicateProvider
import net.minecraft.client.item.ModelPredicateProviderRegistry
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.entity.TntEntityRenderer
import net.minecraft.client.render.entity.model.EntityModelLayer
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

object ProjectGlitchClient : ClientModInitializer {
	val ROBOT_ENTITY_MODEL_LAYER = EntityModelLayer(ProjectGlitch.ROBOT_ENTITY_ID, "main")

	override fun onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlock(ProjectGlitch.CHEMOTHERAPEUTIC_DRUG_BLOCK, RenderLayer.getCutout())
		BlockRenderLayerMap.INSTANCE.putBlock(ProjectGlitch.PROCESSING_TABLE_BLOCK, RenderLayer.getCutout())
		BlockRenderLayerMap.INSTANCE.putBlock(ProjectGlitch.TARGETED_DRUG_INJECTOR_BLOCK, RenderLayer.getCutout())

		ModelPredicateProviderRegistry.register(ProjectGlitch.DEBUG_TOOL_ITEM, Identifier("mode")) { stack, clientWorld, livingEntity, seed ->
			DebugToolItem.getDebugMode(stack).ordinal.toFloat()
		}

		ModelPredicateProviderRegistry.register(ProjectGlitch.PROBE_ITEM, Identifier("sample")) { stack, clientWorld, livingEntity, seed ->
			if (ProbeItem.containsSample(stack)) 1f else 0f
		}

		ModelPredicateProviderRegistry.register(ProjectGlitch.RADIATION_STAFF_ITEM, Identifier("heat")) { stack, clientWorld, livingEntity, seed ->
			stack.damage.toFloat() / stack.maxDamage
		}
		ModelPredicateProviderRegistry.register(ProjectGlitch.RADIATION_STAFF_ITEM, Identifier("overheated")) { stack, clientWorld, livingEntity, seed ->
			if (RadiationStaffItem.isOverheated(stack)) 1f else 0f
		}
		ModelPredicateProviderRegistry.register(ProjectGlitch.SCANNER_ITEM, Identifier("angle"), CompassAnglePredicateProvider { world: ClientWorld?, stack: ItemStack, entity: Entity? ->
			ScannerItem.getTrackedGlobalPosition(stack)
		})

		EntityRendererRegistry.register(ProjectGlitch.CHEMOTHERAPEUTIC_DRUG_ENTITY_TYPE) { context -> TntEntityRenderer(context) }
		EntityRendererRegistry.register(ProjectGlitch.ROBOT_ENTITY_TYPE) { context -> RobotEntityRenderer(context) }

		EntityModelLayerRegistry.registerModelLayer(ROBOT_ENTITY_MODEL_LAYER, RobotEntityModel::getTexturedModelData)

		HandledScreens.register(ProjectGlitch.PROCESSING_TABLE_ANALYZER_SCREEN_HANDLER, ::ProcessingTableAnalyzerScreen)
		HandledScreens.register(ProjectGlitch.PROCESSING_TABLE_RESEARCH_SCREEN_HANDLER, ::ProcessingTableResearchScreen)

		ClientPlayNetworking.registerGlobalReceiver(ProjectGlitch.GLITCH_PROGRESS_PACKET_ID, GlitchProgressPacket::handle)
	}
}
