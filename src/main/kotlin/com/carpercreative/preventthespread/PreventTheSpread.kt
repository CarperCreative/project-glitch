package com.carpercreative.preventthespread

import com.carpercreative.preventthespread.block.CancerBlock
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.MapColor
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

object PreventTheSpread : ModInitializer {
	val MOD_ID = "preventthespread"

	private val logger = LoggerFactory.getLogger(MOD_ID)

	val CANCER_BLOCK = CancerBlock(FabricBlockSettings.create().strength(4.0f).ticksRandomly().mapColor(MapColor.DARK_CRIMSON).pistonBehavior(PistonBehavior.BLOCK))
	val CANCER_BLOCK_ITEM = BlockItem(CANCER_BLOCK, FabricItemSettings())

	private val ITEM_GROUP = FabricItemGroup.builder()
		.icon { ItemStack(CANCER_BLOCK_ITEM) }
		.displayName(Text.translatable("itemGroup.$MOD_ID.default"))
		.entries { context, entries ->
			entries.add(CANCER_BLOCK_ITEM)
		}
		.build()

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		logger.info("Hello Fabric world!")

		Registry.register(Registries.BLOCK, Identifier(MOD_ID, "cancer_block"), CANCER_BLOCK)
		Registry.register(Registries.ITEM, Identifier(MOD_ID, "cancer_block"), CANCER_BLOCK_ITEM)

		Registry.register(Registries.ITEM_GROUP, Identifier(MOD_ID, "default"), ITEM_GROUP)
	}
}
