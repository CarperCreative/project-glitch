package com.carpercreative.preventthespread

import com.carpercreative.preventthespread.block.CancerBlock
import com.carpercreative.preventthespread.item.DebugToolItem
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
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.Rarity
import org.slf4j.LoggerFactory

object PreventTheSpread : ModInitializer {
	const val MOD_ID = "preventthespread"

	fun identifier(path: String) = Identifier(MOD_ID, path)

	private val logger = LoggerFactory.getLogger(MOD_ID)

	val CANCER_BLOCK = CancerBlock(FabricBlockSettings.create().strength(4.0f).ticksRandomly().mapColor(MapColor.DARK_CRIMSON).pistonBehavior(PistonBehavior.BLOCK))
	val CANCER_BLOCK_ITEM = BlockItem(CANCER_BLOCK, FabricItemSettings())

	val DEBUG_TOOL_ITEM = DebugToolItem(FabricItemSettings().maxCount(1).rarity(Rarity.EPIC))

	val CANCER_SPREADABLE_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, identifier("cancer_spreadable"))

	private val ITEM_GROUP = FabricItemGroup.builder()
		.icon { ItemStack(CANCER_BLOCK_ITEM) }
		.displayName(Text.translatable("itemGroup.$MOD_ID.default"))
		.entries { context, entries ->
			entries.add(CANCER_BLOCK_ITEM)
			entries.add(DEBUG_TOOL_ITEM)
		}
		.build()

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		logger.info("Hello Fabric world!")

		Registry.register(Registries.BLOCK, identifier("cancer_block"), CANCER_BLOCK)
		Registry.register(Registries.ITEM, identifier("cancer_block"), CANCER_BLOCK_ITEM)

		Registry.register(Registries.ITEM, identifier("debug_tool"), DEBUG_TOOL_ITEM)

		Registry.register(Registries.ITEM_GROUP, identifier("default"), ITEM_GROUP)

		// TODO: store cancer core associated with cancer block at a given position (important: pistons!)
		// TODO: implement cancer spread (random tick)
		// TODO: implement sampling tool
		// TODO: implement surgery tool
		// TODO: add surgery tool crafting recipe
		// TODO: make cancer blocks mine-able only using appropriate tools
		// TODO: implement chemotherapy (uhhh, TNT?)
		// TODO: implement targeted drug therapy (syringe item/block)
		// TODO: implement radiation therapy gun (laser gun, pew pew)
		// TODO: create research table block
		// TODO: create research GUI
		// TODO: create research state store (per player?)
		// TODO: implement different types of cancer blocks (logs, dirt, stone)?
		// TODO: implement towers/beacons
	}
}
