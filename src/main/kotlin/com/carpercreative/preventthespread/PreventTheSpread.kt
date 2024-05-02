package com.carpercreative.preventthespread

import com.carpercreative.preventthespread.block.CancerLeavesBlock
import com.carpercreative.preventthespread.block.CancerPillarBlock
import com.carpercreative.preventthespread.block.CancerSlabBlock
import com.carpercreative.preventthespread.block.CancerStairsBlock
import com.carpercreative.preventthespread.block.CancerousBlock
import com.carpercreative.preventthespread.block.ChemotherapeuticDrugBlock
import com.carpercreative.preventthespread.block.ProcessingTableBlock
import com.carpercreative.preventthespread.block.SolidCancerBlock
import com.carpercreative.preventthespread.block.TargetedDrugInjectorBlock
import com.carpercreative.preventthespread.blockEntity.ProcessingTableAnalyzerBlockEntity
import com.carpercreative.preventthespread.entity.ChemotherapeuticDrugEntity
import com.carpercreative.preventthespread.item.DebugToolItem
import com.carpercreative.preventthespread.item.ProbeItem
import com.carpercreative.preventthespread.item.RadiationStaffItem
import com.carpercreative.preventthespread.item.SurgeryAxeItem
import com.carpercreative.preventthespread.item.SurgeryHoeItem
import com.carpercreative.preventthespread.item.SurgeryPickaxeItem
import com.carpercreative.preventthespread.item.SurgeryShovelItem
import com.carpercreative.preventthespread.screen.ProcessingTableAnalyzerScreenHandler
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.MapColor
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ToolMaterials
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.resource.featuretoggle.FeatureFlags
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.Rarity
import org.slf4j.LoggerFactory

@Suppress("MemberVisibilityCanBePrivate")
object PreventTheSpread : ModInitializer {
	const val MOD_ID = "preventthespread"

	fun identifier(path: String) = Identifier(MOD_ID, path)

	private val logger = LoggerFactory.getLogger(MOD_ID)

	val CANCER_DIRT_BLOCK = SolidCancerBlock(CancerousBlock.defaultBlockSettings())
	val CANCER_DIRT_BLOCK_ITEM = BlockItem(CANCER_DIRT_BLOCK, FabricItemSettings())
	val CANCER_LOG_BLOCK = CancerPillarBlock(CancerousBlock.defaultBlockSettings())
	val CANCER_LOG_BLOCK_ITEM = BlockItem(CANCER_LOG_BLOCK, FabricItemSettings())
	val CANCER_PLANKS_BLOCK = SolidCancerBlock(CancerousBlock.defaultBlockSettings())
	val CANCER_PLANKS_BLOCK_ITEM = BlockItem(CANCER_PLANKS_BLOCK, FabricItemSettings())
	val CANCER_STONE_BLOCK = SolidCancerBlock(CancerousBlock.defaultBlockSettings().requiresTool())
	val CANCER_STONE_BLOCK_ITEM = BlockItem(CANCER_STONE_BLOCK, FabricItemSettings())

	val CANCER_DIRT_SLAB_BLOCK = CancerSlabBlock(CancerousBlock.defaultBlockSettings())
	val CANCER_DIRT_SLAB_BLOCK_ITEM = BlockItem(CANCER_DIRT_SLAB_BLOCK, FabricItemSettings())
	val CANCER_DIRT_STAIRS_BLOCK = CancerStairsBlock(CANCER_DIRT_BLOCK.defaultState, FabricBlockSettings.copy(CANCER_DIRT_BLOCK))
	val CANCER_DIRT_STAIRS_BLOCK_ITEM = BlockItem(CANCER_DIRT_STAIRS_BLOCK, FabricItemSettings())
	val CANCER_PLANKS_SLAB_BLOCK = CancerSlabBlock(CancerousBlock.defaultBlockSettings())
	val CANCER_PLANKS_SLAB_BLOCK_ITEM = BlockItem(CANCER_PLANKS_SLAB_BLOCK, FabricItemSettings())
	val CANCER_PLANKS_STAIRS_BLOCK = CancerStairsBlock(CANCER_PLANKS_BLOCK.defaultState, FabricBlockSettings.copy(CANCER_PLANKS_BLOCK))
	val CANCER_PLANKS_STAIRS_BLOCK_ITEM = BlockItem(CANCER_PLANKS_STAIRS_BLOCK, FabricItemSettings())
	val CANCER_STONE_SLAB_BLOCK = CancerSlabBlock(CancerousBlock.defaultBlockSettings().requiresTool())
	val CANCER_STONE_SLAB_BLOCK_ITEM = BlockItem(CANCER_STONE_SLAB_BLOCK, FabricItemSettings())
	val CANCER_STONE_STAIRS_BLOCK = CancerStairsBlock(CANCER_STONE_BLOCK.defaultState, FabricBlockSettings.copy(CANCER_STONE_BLOCK).requiresTool())
	val CANCER_STONE_STAIRS_BLOCK_ITEM = BlockItem(CANCER_STONE_STAIRS_BLOCK, FabricItemSettings())

	val CANCER_LEAVES_BLOCK = CancerLeavesBlock(
		CancerousBlock.defaultBlockSettings()
			.allowsSpawning(Blocks::canSpawnOnLeaves)
			.blockVision(Blocks::never)
			.nonOpaque()
			.pistonBehavior(PistonBehavior.DESTROY)
			.solidBlock(Blocks::never)
			.strength(0.2f)
			.suffocates(Blocks::never)
	)
	val CANCER_LEAVES_BLOCK_ITEM = BlockItem(CANCER_LEAVES_BLOCK, FabricItemSettings())

	val CHEMOTHERAPEUTIC_DRUG_BLOCK = ChemotherapeuticDrugBlock(FabricBlockSettings.create().mapColor(MapColor.LIGHT_BLUE).nonOpaque().breakInstantly().sounds(BlockSoundGroup.GRASS).solidBlock(Blocks::never))
	val CHEMOTHERAPEUTIC_DRUG_BLOCK_ITEM = BlockItem(CHEMOTHERAPEUTIC_DRUG_BLOCK, FabricItemSettings())
	val PROCESSING_TABLE_BLOCK = ProcessingTableBlock(
		FabricBlockSettings.create()
			.hardness(3.5f)
			.mapColor(MapColor.DARK_GREEN)
			.nonOpaque()
			.pistonBehavior(PistonBehavior.BLOCK)
			.sounds(BlockSoundGroup.WOOD)
	)
	val PROCESSING_TABLE_BLOCK_ITEM = BlockItem(PROCESSING_TABLE_BLOCK, FabricItemSettings())
	val TARGETED_DRUG_INJECTOR_BLOCK = TargetedDrugInjectorBlock(
		FabricBlockSettings.create()
			.blockVision(Blocks::never)
			.breakInstantly()
			.nonOpaque()
			.pistonBehavior(PistonBehavior.DESTROY)
			.solidBlock(Blocks::never)
			.suffocates(Blocks::never)
			.ticksRandomly()
	)
	val TARGETED_DRUG_INJECTOR_BLOCK_ITEM = BlockItem(TARGETED_DRUG_INJECTOR_BLOCK, FabricItemSettings())

	val PROCESSING_TABLE_BLOCK_ENTITY: BlockEntityType<ProcessingTableAnalyzerBlockEntity> = BlockEntityType.Builder.create(::ProcessingTableAnalyzerBlockEntity, PROCESSING_TABLE_BLOCK).build()

	val DEBUG_TOOL_ITEM = DebugToolItem(FabricItemSettings().maxCount(1).rarity(Rarity.EPIC))
	val PROBE_ITEM = ProbeItem(FabricItemSettings().maxCount(1))
	val RADIATION_STAFF_ITEM = RadiationStaffItem(FabricItemSettings().maxCount(1).maxDamage(60).customDamage(RadiationStaffItem.RadiationBeamGunDamageHandler))
	val SURGERY_AXE_ITEM = SurgeryAxeItem(ToolMaterials.IRON, 6.0f, -3.1f, FabricItemSettings().maxDamage(800))
	val SURGERY_HOE_ITEM = SurgeryHoeItem(ToolMaterials.IRON, -2, -1.0f, FabricItemSettings().maxDamage(800))
	val SURGERY_PICKAXE_ITEM = SurgeryPickaxeItem(ToolMaterials.IRON, 1, -2.8f, FabricItemSettings().maxDamage(800))
	val SURGERY_SHOVEL_ITEM = SurgeryShovelItem(ToolMaterials.IRON, 1.5f, -3.0f, FabricItemSettings().maxDamage(800))

	val CANCEROUS_BLOCK_TAG: TagKey<Block> = TagKey.of(RegistryKeys.BLOCK, identifier("cancerous"))
	val CANCER_SPREADABLE_BLOCK_TAG: TagKey<Block> = TagKey.of(RegistryKeys.BLOCK, identifier("cancer_spreadable"))
	val SURGERY_AXE_MINEABLE_BLOCK_TAG: TagKey<Block> = TagKey.of(RegistryKeys.BLOCK, identifier("mineable/surgery_axe"))
	val SURGERY_HOE_MINEABLE_BLOCK_TAG: TagKey<Block> = TagKey.of(RegistryKeys.BLOCK, identifier("mineable/surgery_hoe"))
	val SURGERY_PICKAXE_MINEABLE_BLOCK_TAG: TagKey<Block> = TagKey.of(RegistryKeys.BLOCK, identifier("mineable/surgery_pickaxe"))
	val SURGERY_SHOVEL_MINEABLE_BLOCK_TAG: TagKey<Block> = TagKey.of(RegistryKeys.BLOCK, identifier("mineable/surgery_shovel"))

	val SURGERY_TOOL_ITEM_TAG: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, identifier("surgery_tool"))

	val CHEMOTHERAPEUTIC_DRUG_ENTITY_TYPE: EntityType<ChemotherapeuticDrugEntity> = EntityType.Builder.create({ entityType, world -> ChemotherapeuticDrugEntity(entityType, world) }, SpawnGroup.MISC).makeFireImmune().setDimensions(0.98f, 0.98f).maxTrackingRange(10).trackingTickInterval(10).build()

	val PROCESSING_TABLE_ANALYZER_SCREEN_HANDLER = ScreenHandlerType(::ProcessingTableAnalyzerScreenHandler, FeatureFlags.VANILLA_FEATURES)

	private val ITEM_GROUP = FabricItemGroup.builder()
		.icon { ItemStack(CANCER_DIRT_BLOCK_ITEM) }
		.displayName(Text.translatable("itemGroup.$MOD_ID.default"))
		.entries { context, entries ->
			entries.add(PROBE_ITEM)
			entries.add(PROCESSING_TABLE_BLOCK_ITEM)
			entries.add(CHEMOTHERAPEUTIC_DRUG_BLOCK_ITEM)
			entries.add(RADIATION_STAFF_ITEM)
			entries.add(SURGERY_AXE_ITEM)
			entries.add(SURGERY_HOE_ITEM)
			entries.add(SURGERY_PICKAXE_ITEM)
			entries.add(SURGERY_SHOVEL_ITEM)
			entries.add(TARGETED_DRUG_INJECTOR_BLOCK_ITEM)
			entries.add(CANCER_DIRT_BLOCK_ITEM)
			entries.add(CANCER_LOG_BLOCK_ITEM)
			entries.add(CANCER_PLANKS_BLOCK_ITEM)
			entries.add(CANCER_STONE_BLOCK_ITEM)
			entries.add(CANCER_DIRT_SLAB_BLOCK_ITEM)
			entries.add(CANCER_DIRT_STAIRS_BLOCK_ITEM)
			entries.add(CANCER_PLANKS_SLAB_BLOCK_ITEM)
			entries.add(CANCER_PLANKS_STAIRS_BLOCK_ITEM)
			entries.add(CANCER_STONE_SLAB_BLOCK_ITEM)
			entries.add(CANCER_STONE_STAIRS_BLOCK_ITEM)
			entries.add(CANCER_LEAVES_BLOCK_ITEM)
			entries.add(DEBUG_TOOL_ITEM)
		}
		.build()

	override fun onInitialize() {
		Registry.register(Registries.BLOCK, identifier("cancer_dirt"), CANCER_DIRT_BLOCK)
		Registry.register(Registries.ITEM, identifier("cancer_dirt"), CANCER_DIRT_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, identifier("cancer_log"), CANCER_LOG_BLOCK)
		Registry.register(Registries.ITEM, identifier("cancer_log"), CANCER_LOG_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, identifier("cancer_planks"), CANCER_PLANKS_BLOCK)
		Registry.register(Registries.ITEM, identifier("cancer_planks"), CANCER_PLANKS_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, identifier("cancer_stone"), CANCER_STONE_BLOCK)
		Registry.register(Registries.ITEM, identifier("cancer_stone"), CANCER_STONE_BLOCK_ITEM)

		Registry.register(Registries.BLOCK, identifier("cancer_dirt_slab"), CANCER_DIRT_SLAB_BLOCK)
		Registry.register(Registries.ITEM, identifier("cancer_dirt_slab"), CANCER_DIRT_SLAB_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, identifier("cancer_dirt_stairs"), CANCER_DIRT_STAIRS_BLOCK)
		Registry.register(Registries.ITEM, identifier("cancer_dirt_stairs"), CANCER_DIRT_STAIRS_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, identifier("cancer_planks_slab"), CANCER_PLANKS_SLAB_BLOCK)
		Registry.register(Registries.ITEM, identifier("cancer_planks_slab"), CANCER_PLANKS_SLAB_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, identifier("cancer_planks_stairs"), CANCER_PLANKS_STAIRS_BLOCK)
		Registry.register(Registries.ITEM, identifier("cancer_planks_stairs"), CANCER_PLANKS_STAIRS_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, identifier("cancer_stone_slab"), CANCER_STONE_SLAB_BLOCK)
		Registry.register(Registries.ITEM, identifier("cancer_stone_slab"), CANCER_STONE_SLAB_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, identifier("cancer_stone_stairs"), CANCER_STONE_STAIRS_BLOCK)
		Registry.register(Registries.ITEM, identifier("cancer_stone_stairs"), CANCER_STONE_STAIRS_BLOCK_ITEM)

		Registry.register(Registries.BLOCK, identifier("cancer_leaves"), CANCER_LEAVES_BLOCK)
		Registry.register(Registries.ITEM, identifier("cancer_leaves"), CANCER_LEAVES_BLOCK_ITEM)

		Registry.register(Registries.BLOCK, identifier("chemotherapeutic_drug"), CHEMOTHERAPEUTIC_DRUG_BLOCK)
		Registry.register(Registries.ITEM, identifier("chemotherapeutic_drug"), CHEMOTHERAPEUTIC_DRUG_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, identifier("processing_table"), PROCESSING_TABLE_BLOCK)
		Registry.register(Registries.ITEM, identifier("processing_table"), PROCESSING_TABLE_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, identifier("targeted_drug_injector"), TARGETED_DRUG_INJECTOR_BLOCK)
		Registry.register(Registries.ITEM, identifier("targeted_drug_injector"), TARGETED_DRUG_INJECTOR_BLOCK_ITEM)

		Registry.register(Registries.BLOCK_ENTITY_TYPE, identifier("processing_table"), PROCESSING_TABLE_BLOCK_ENTITY)

		Registry.register(Registries.ITEM, identifier("debug_tool"), DEBUG_TOOL_ITEM)
		Registry.register(Registries.ITEM, identifier("probe"), PROBE_ITEM)
		Registry.register(Registries.ITEM, identifier("radiation_staff"), RADIATION_STAFF_ITEM)
		Registry.register(Registries.ITEM, identifier("surgery_axe"), SURGERY_AXE_ITEM)
		Registry.register(Registries.ITEM, identifier("surgery_hoe"), SURGERY_HOE_ITEM)
		Registry.register(Registries.ITEM, identifier("surgery_pickaxe"), SURGERY_PICKAXE_ITEM)
		Registry.register(Registries.ITEM, identifier("surgery_shovel"), SURGERY_SHOVEL_ITEM)

		Registry.register(Registries.ITEM_GROUP, identifier("default"), ITEM_GROUP)

		Registry.register(Registries.ENTITY_TYPE, identifier("chemotherapeutic_drug"), CHEMOTHERAPEUTIC_DRUG_ENTITY_TYPE)

		Registry.register(Registries.SCREEN_HANDLER, identifier("processing_table_analyzer"), PROCESSING_TABLE_ANALYZER_SCREEN_HANDLER)

		ServerTickEvents.END_WORLD_TICK.register { world ->
			RadiationStaffItem.doCooldown(world)
		}

		// TODO: replace cancer block textures
		// TODO: add surgery tool crafting recipe
		// TODO: make cancer blocks mine-able only using appropriate tools
		// TODO: replace chemotherapeutic drug textures/model
		// TODO: replace targeted drug injector model and block state to indicate progress
		// TODO: create research table block
		// TODO: create research GUI
		// TODO: create research state store (per player?)
		// TODO: implement towers/beacons
		// TODO: implement radiation staff recharge rate and/or heat capacity research
		// TODO: fix model transformations of surgery tools (on-ground is MASSIVE) .-.
	}
}
