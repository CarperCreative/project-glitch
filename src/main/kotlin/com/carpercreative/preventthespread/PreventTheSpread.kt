package com.carpercreative.preventthespread

import com.carpercreative.preventthespread.block.CancerBlock
import com.carpercreative.preventthespread.item.DebugToolItem
import com.carpercreative.preventthespread.item.ProbeItem
import com.carpercreative.preventthespread.item.RadiationStaffItem
import com.carpercreative.preventthespread.item.SurgeryAxeItem
import com.carpercreative.preventthespread.item.SurgeryHoeItem
import com.carpercreative.preventthespread.item.SurgeryPickaxeItem
import com.carpercreative.preventthespread.item.SurgeryShovelItem
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.MapColor
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.ToolMaterials
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.Rarity
import org.slf4j.LoggerFactory

@Suppress("MemberVisibilityCanBePrivate")
object PreventTheSpread : ModInitializer {
	const val MOD_ID = "preventthespread"

	fun identifier(path: String) = Identifier(MOD_ID, path)

	private val logger = LoggerFactory.getLogger(MOD_ID)

	val CANCER_BLOCK = CancerBlock(FabricBlockSettings.create().strength(4.0f).ticksRandomly().mapColor(MapColor.DARK_CRIMSON).pistonBehavior(PistonBehavior.BLOCK))
	val CANCER_BLOCK_ITEM = BlockItem(CANCER_BLOCK, FabricItemSettings())

	val DEBUG_TOOL_ITEM = DebugToolItem(FabricItemSettings().maxCount(1).rarity(Rarity.EPIC))
	val PROBE_ITEM = ProbeItem(FabricItemSettings().maxCount(1))
	val RADIATION_STAFF_ITEM = RadiationStaffItem(FabricItemSettings().maxCount(1).maxDamage(60).customDamage(RadiationStaffItem.RadiationBeamGunDamageHandler))
	val SURGERY_AXE_ITEM = SurgeryAxeItem(ToolMaterials.IRON, 6.0f, -3.1f, FabricItemSettings().maxDamage(800))
	val SURGERY_HOE_ITEM = SurgeryHoeItem(ToolMaterials.IRON, -2, -1.0f, FabricItemSettings().maxDamage(800))
	val SURGERY_PICKAXE_ITEM = SurgeryPickaxeItem(ToolMaterials.IRON, 1, -2.8f, FabricItemSettings().maxDamage(800))
	val SURGERY_SHOVEL_ITEM = SurgeryShovelItem(ToolMaterials.IRON, 1.5f, -3.0f, FabricItemSettings().maxDamage(800))

	val CANCEROUS_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, identifier("cancerous"))
	val CANCER_SPREADABLE_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, identifier("cancer_spreadable"))
	val SURGERY_AXE_MINEABLE_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, identifier("mineable/surgery_axe"))
	val SURGERY_HOE_MINEABLE_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, identifier("mineable/surgery_hoe"))
	val SURGERY_PICKAXE_MINEABLE_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, identifier("mineable/surgery_pickaxe"))
	val SURGERY_SHOVEL_MINEABLE_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, identifier("mineable/surgery_shovel"))

	val SURGERY_TOOL_ITEM_TAG = TagKey.of(RegistryKeys.ITEM, identifier("surgery_tool"))

	private val ITEM_GROUP = FabricItemGroup.builder()
		.icon { ItemStack(CANCER_BLOCK_ITEM) }
		.displayName(Text.translatable("itemGroup.$MOD_ID.default"))
		.entries { context, entries ->
			entries.add(CANCER_BLOCK_ITEM)
			entries.add(PROBE_ITEM)
			entries.add(RADIATION_STAFF_ITEM)
			entries.add(SURGERY_AXE_ITEM)
			entries.add(SURGERY_HOE_ITEM)
			entries.add(SURGERY_PICKAXE_ITEM)
			entries.add(SURGERY_SHOVEL_ITEM)
			entries.add(DEBUG_TOOL_ITEM)
		}
		.build()

	override fun onInitialize() {
		Registry.register(Registries.BLOCK, identifier("cancer_block"), CANCER_BLOCK)
		Registry.register(Registries.ITEM, identifier("cancer_block"), CANCER_BLOCK_ITEM)

		Registry.register(Registries.ITEM, identifier("debug_tool"), DEBUG_TOOL_ITEM)
		Registry.register(Registries.ITEM, identifier("probe"), PROBE_ITEM)
		Registry.register(Registries.ITEM, identifier("radiation_staff"), RADIATION_STAFF_ITEM)
		Registry.register(Registries.ITEM, identifier("surgery_axe"), SURGERY_AXE_ITEM)
		Registry.register(Registries.ITEM, identifier("surgery_hoe"), SURGERY_HOE_ITEM)
		Registry.register(Registries.ITEM, identifier("surgery_pickaxe"), SURGERY_PICKAXE_ITEM)
		Registry.register(Registries.ITEM, identifier("surgery_shovel"), SURGERY_SHOVEL_ITEM)

		Registry.register(Registries.ITEM_GROUP, identifier("default"), ITEM_GROUP)

		ServerTickEvents.END_WORLD_TICK.register { world ->
			RadiationStaffItem.doCooldown(world)
		}

		// TODO: add surgery tool crafting recipe
		// TODO: make cancer blocks mine-able only using appropriate tools
		// TODO: implement chemotherapy (uhhh, TNT?)
		// TODO: implement targeted drug therapy (syringe item/block)
		// TODO: create research table block
		// TODO: create research GUI
		// TODO: create research state store (per player?)
		// TODO: implement different types of cancer blocks (logs, dirt, stone)?
		// TODO: implement towers/beacons
		// TODO: implement radiation staff recharge rate and/or heat capacity research
		// TODO: fix model transformations of surgery tools (on-ground is MASSIVE) .-.
	}
}
