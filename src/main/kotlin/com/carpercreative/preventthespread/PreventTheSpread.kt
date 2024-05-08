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
import com.carpercreative.preventthespread.controller.BossBarController
import com.carpercreative.preventthespread.controller.CancerSpreadController
import com.carpercreative.preventthespread.entity.ChemotherapeuticDrugEntity
import com.carpercreative.preventthespread.item.DebugToolItem
import com.carpercreative.preventthespread.item.ProbeItem
import com.carpercreative.preventthespread.item.RadiationStaffItem
import com.carpercreative.preventthespread.item.ScannerItem
import com.carpercreative.preventthespread.item.SurgeryAxeItem
import com.carpercreative.preventthespread.item.SurgeryHoeItem
import com.carpercreative.preventthespread.item.SurgeryPickaxeItem
import com.carpercreative.preventthespread.item.SurgeryShovelItem
import com.carpercreative.preventthespread.screen.ProcessingTableAnalyzerScreenHandler
import net.fabricmc.api.ModInitializer
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

@Suppress("MemberVisibilityCanBePrivate")
object PreventTheSpread : ModInitializer {
	const val MOD_ID = "preventthespread"

	fun identifier(path: String) = Identifier(MOD_ID, path)

	val CANCER_DIRT_ID = identifier("cancer_dirt")
	val CANCER_DIRT_BLOCK = SolidCancerBlock(CancerousBlock.defaultBlockSettings())
	val CANCER_DIRT_BLOCK_ITEM = BlockItem(CANCER_DIRT_BLOCK, FabricItemSettings())
	val CANCER_LOG_ID = identifier("cancer_log")
	val CANCER_LOG_BLOCK = CancerPillarBlock(CancerousBlock.defaultBlockSettings())
	val CANCER_LOG_BLOCK_ITEM = BlockItem(CANCER_LOG_BLOCK, FabricItemSettings())
	val CANCER_PLANKS_ID = identifier("cancer_planks")
	val CANCER_PLANKS_BLOCK = SolidCancerBlock(CancerousBlock.defaultBlockSettings())
	val CANCER_PLANKS_BLOCK_ITEM = BlockItem(CANCER_PLANKS_BLOCK, FabricItemSettings())
	val CANCER_STONE_ID = identifier("cancer_stone")
	val CANCER_STONE_BLOCK = SolidCancerBlock(CancerousBlock.defaultBlockSettings().requiresTool())
	val CANCER_STONE_BLOCK_ITEM = BlockItem(CANCER_STONE_BLOCK, FabricItemSettings())

	val CANCER_DIRT_SLAB_ID = identifier("cancer_dirt_slab")
	val CANCER_DIRT_SLAB_BLOCK = CancerSlabBlock(CancerousBlock.defaultBlockSettings())
	val CANCER_DIRT_SLAB_BLOCK_ITEM = BlockItem(CANCER_DIRT_SLAB_BLOCK, FabricItemSettings())
	val CANCER_DIRT_STAIRS_ID = identifier("cancer_dirt_stairs")
	val CANCER_DIRT_STAIRS_BLOCK = CancerStairsBlock(CANCER_DIRT_BLOCK.defaultState, FabricBlockSettings.copy(CANCER_DIRT_BLOCK))
	val CANCER_DIRT_STAIRS_BLOCK_ITEM = BlockItem(CANCER_DIRT_STAIRS_BLOCK, FabricItemSettings())
	val CANCER_PLANKS_SLAB_ID = identifier("cancer_planks_slab")
	val CANCER_PLANKS_SLAB_BLOCK = CancerSlabBlock(CancerousBlock.defaultBlockSettings())
	val CANCER_PLANKS_SLAB_BLOCK_ITEM = BlockItem(CANCER_PLANKS_SLAB_BLOCK, FabricItemSettings())
	val CANCER_PLANKS_STAIRS_ID = identifier("cancer_planks_stairs")
	val CANCER_PLANKS_STAIRS_BLOCK = CancerStairsBlock(CANCER_PLANKS_BLOCK.defaultState, FabricBlockSettings.copy(CANCER_PLANKS_BLOCK))
	val CANCER_PLANKS_STAIRS_BLOCK_ITEM = BlockItem(CANCER_PLANKS_STAIRS_BLOCK, FabricItemSettings())
	val CANCER_STONE_SLAB_ID = identifier("cancer_stone_slab")
	val CANCER_STONE_SLAB_BLOCK = CancerSlabBlock(CancerousBlock.defaultBlockSettings().requiresTool())
	val CANCER_STONE_SLAB_BLOCK_ITEM = BlockItem(CANCER_STONE_SLAB_BLOCK, FabricItemSettings())
	val CANCER_STONE_STAIRS_ID = identifier("cancer_stone_stairs")
	val CANCER_STONE_STAIRS_BLOCK = CancerStairsBlock(CANCER_STONE_BLOCK.defaultState, FabricBlockSettings.copy(CANCER_STONE_BLOCK).requiresTool())
	val CANCER_STONE_STAIRS_BLOCK_ITEM = BlockItem(CANCER_STONE_STAIRS_BLOCK, FabricItemSettings())

	val CANCER_LEAVES_ID = identifier("cancer_leaves")
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

	val CHEMOTHERAPEUTIC_DRUG_ID = identifier("chemotherapeutic_drug")
	val CHEMOTHERAPEUTIC_DRUG_BLOCK = ChemotherapeuticDrugBlock(FabricBlockSettings.create().mapColor(MapColor.LIGHT_BLUE).nonOpaque().breakInstantly().sounds(BlockSoundGroup.GRASS).solidBlock(Blocks::never))
	val CHEMOTHERAPEUTIC_DRUG_BLOCK_ITEM = BlockItem(CHEMOTHERAPEUTIC_DRUG_BLOCK, FabricItemSettings())
	val PROCESSING_TABLE_ID = identifier("processing_table")
	val PROCESSING_TABLE_BLOCK = ProcessingTableBlock(
		FabricBlockSettings.create()
			.hardness(3.5f)
			.mapColor(MapColor.DARK_GREEN)
			.nonOpaque()
			.pistonBehavior(PistonBehavior.BLOCK)
			.sounds(BlockSoundGroup.WOOD)
	)
	val PROCESSING_TABLE_BLOCK_ITEM = BlockItem(PROCESSING_TABLE_BLOCK, FabricItemSettings())
	val TARGETED_DRUG_INJECTOR_ID = identifier("targeted_drug_injector")
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

	val CANCEROUS_MATERIAL_ID = identifier("cancerous_material")
	val CANCEROUS_MATERIAL_ITEM = Item(Item.Settings())
	val DEBUG_TOOL_ITEM_ID = identifier("debug_tool")
	val DEBUG_TOOL_ITEM = DebugToolItem(FabricItemSettings().maxCount(1).rarity(Rarity.EPIC))
	val PROBE_ITEM_ID = identifier("probe")
	val PROBE_ITEM = ProbeItem(FabricItemSettings().maxCount(1))
	val RADIATION_STAFF_ITEM_ID = identifier("radiation_staff")
	val RADIATION_STAFF_ITEM = RadiationStaffItem(FabricItemSettings().maxCount(1).maxDamage(60).customDamage(RadiationStaffItem.RadiationBeamGunDamageHandler))
	val RESEARCH_ID = identifier("research")
	val RESEARCH_ITEM = Item(Item.Settings())
	val SCANNER_ITEM_ID = identifier("scanner")
	val SCANNER_ITEM = ScannerItem(FabricItemSettings())
	val SURGERY_AXE_ITEM_ID = identifier("surgery_axe")
	val SURGERY_AXE_ITEM = SurgeryAxeItem(ToolMaterials.IRON, 6.0f, -3.1f, FabricItemSettings().maxDamage(800))
	val SURGERY_HOE_ITEM_ID = identifier("surgery_hoe")
	val SURGERY_HOE_ITEM = SurgeryHoeItem(ToolMaterials.IRON, -2, -1.0f, FabricItemSettings().maxDamage(800))
	val SURGERY_PICKAXE_ITEM_ID = identifier("surgery_pickaxe")
	val SURGERY_PICKAXE_ITEM = SurgeryPickaxeItem(ToolMaterials.IRON, 1, -2.8f, FabricItemSettings().maxDamage(800))
	val SURGERY_SHOVEL_ITEM_ID = identifier("surgery_shovel")
	val SURGERY_SHOVEL_ITEM = SurgeryShovelItem(ToolMaterials.IRON, 1.5f, -3.0f, FabricItemSettings().maxDamage(800))

	val CANCEROUS_BLOCK_TAG: TagKey<Block> = TagKey.of(RegistryKeys.BLOCK, identifier("cancerous"))
	val CANCER_SPREADABLE_BLOCK_TAG: TagKey<Block> = TagKey.of(RegistryKeys.BLOCK, identifier("cancer_spreadable"))
	val SURGERY_AXE_MINEABLE_BLOCK_TAG: TagKey<Block> = TagKey.of(RegistryKeys.BLOCK, identifier("mineable/surgery_axe"))
	val SURGERY_HOE_MINEABLE_BLOCK_TAG: TagKey<Block> = TagKey.of(RegistryKeys.BLOCK, identifier("mineable/surgery_hoe"))
	val SURGERY_PICKAXE_MINEABLE_BLOCK_TAG: TagKey<Block> = TagKey.of(RegistryKeys.BLOCK, identifier("mineable/surgery_pickaxe"))
	val SURGERY_SHOVEL_MINEABLE_BLOCK_TAG: TagKey<Block> = TagKey.of(RegistryKeys.BLOCK, identifier("mineable/surgery_shovel"))

	val REQUIRES_RECIPE_TO_CRAFT_ITEM_TAG: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, identifier("requires_recipe_to_craft"))
	val SURGERY_TOOL_ITEM_TAG: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, identifier("surgery_tool"))

	val CHEMOTHERAPEUTIC_DRUG_ENTITY_TYPE: EntityType<ChemotherapeuticDrugEntity> = EntityType.Builder.create({ entityType, world -> ChemotherapeuticDrugEntity(entityType, world) }, SpawnGroup.MISC).makeFireImmune().setDimensions(0.98f, 0.98f).maxTrackingRange(10).trackingTickInterval(10).build()

	val PROCESSING_TABLE_ANALYZER_SCREEN_HANDLER = ScreenHandlerType(::ProcessingTableAnalyzerScreenHandler, FeatureFlags.VANILLA_FEATURES)

	private val ITEM_GROUP = FabricItemGroup.builder()
		.icon { ItemStack(CANCER_DIRT_BLOCK_ITEM) }
		.displayName(Text.translatable("itemGroup.$MOD_ID.default"))
		.entries { context, entries ->
			entries.add(SCANNER_ITEM)
			entries.add(PROBE_ITEM)
			entries.add(PROCESSING_TABLE_BLOCK_ITEM)
			entries.add(CANCEROUS_MATERIAL_ITEM)
			entries.add(RESEARCH_ITEM)
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

	object StoryAdvancement {
		private fun storyIdentifier(path: String) = PreventTheSpread.identifier("story/$path")

		val ROOT_ID = storyIdentifier("root")
		val OBTAIN_PROBE_ID = storyIdentifier("obtain_probe")
		val GET_SAMPLE_ID = storyIdentifier("get_sample")
	}

	object ResearchAdvancement {
		private fun researchIdentifier(path: String) = PreventTheSpread.identifier("research/$path")

		val ROOT_ID = researchIdentifier("root")
		val SURGERY_EFFICIENCY_1_ID = researchIdentifier("surgery_efficiency_1")
		val SURGERY_EFFICIENCY_2_ID = researchIdentifier("surgery_efficiency_2")
		val CHEMOTHERAPEUTIC_DRUG_ID = researchIdentifier(PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_ID.path)
		val CHEAPER_CHEMOTHERAPEUTIC_DRUG_ID = researchIdentifier("cheaper_${PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_ID.path}")
		val CHEMOTHERAPEUTIC_DRUG_STRENGTH_1_ID = researchIdentifier("${PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_ID.path}_strength_1")
		val CHEMOTHERAPEUTIC_DRUG_STRENGTH_2_ID = researchIdentifier("${PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_ID.path}_strength_2")
		val RADIATION_STAFF_ID = researchIdentifier(RADIATION_STAFF_ITEM_ID.path)
		val TARGETED_DRUG_ID = researchIdentifier(TARGETED_DRUG_INJECTOR_ID.path)
		val CHEAPER_TARGETED_DRUG_ID = researchIdentifier("cheaper_${TARGETED_DRUG_INJECTOR_ID.path}")
		val TARGETED_DRUG_STRENGTH_1_ID = researchIdentifier("${TARGETED_DRUG_INJECTOR_ID.path}_strength_1")
		val TARGETED_DRUG_STRENGTH_2_ID = researchIdentifier("${TARGETED_DRUG_INJECTOR_ID.path}_strength_2")
	}

	override fun onInitialize() {
		Registry.register(Registries.BLOCK, CANCER_DIRT_ID, CANCER_DIRT_BLOCK)
		Registry.register(Registries.ITEM, CANCER_DIRT_ID, CANCER_DIRT_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, CANCER_LOG_ID, CANCER_LOG_BLOCK)
		Registry.register(Registries.ITEM, CANCER_LOG_ID, CANCER_LOG_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, CANCER_PLANKS_ID, CANCER_PLANKS_BLOCK)
		Registry.register(Registries.ITEM, CANCER_PLANKS_ID, CANCER_PLANKS_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, CANCER_STONE_ID, CANCER_STONE_BLOCK)
		Registry.register(Registries.ITEM, CANCER_STONE_ID, CANCER_STONE_BLOCK_ITEM)

		Registry.register(Registries.BLOCK, CANCER_DIRT_SLAB_ID, CANCER_DIRT_SLAB_BLOCK)
		Registry.register(Registries.ITEM, CANCER_DIRT_SLAB_ID, CANCER_DIRT_SLAB_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, CANCER_DIRT_STAIRS_ID, CANCER_DIRT_STAIRS_BLOCK)
		Registry.register(Registries.ITEM, CANCER_DIRT_STAIRS_ID, CANCER_DIRT_STAIRS_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, CANCER_PLANKS_SLAB_ID, CANCER_PLANKS_SLAB_BLOCK)
		Registry.register(Registries.ITEM, CANCER_PLANKS_SLAB_ID, CANCER_PLANKS_SLAB_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, CANCER_PLANKS_STAIRS_ID, CANCER_PLANKS_STAIRS_BLOCK)
		Registry.register(Registries.ITEM, CANCER_PLANKS_STAIRS_ID, CANCER_PLANKS_STAIRS_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, CANCER_STONE_SLAB_ID, CANCER_STONE_SLAB_BLOCK)
		Registry.register(Registries.ITEM, CANCER_STONE_SLAB_ID, CANCER_STONE_SLAB_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, CANCER_STONE_STAIRS_ID, CANCER_STONE_STAIRS_BLOCK)
		Registry.register(Registries.ITEM, CANCER_STONE_STAIRS_ID, CANCER_STONE_STAIRS_BLOCK_ITEM)

		Registry.register(Registries.BLOCK, CANCER_LEAVES_ID, CANCER_LEAVES_BLOCK)
		Registry.register(Registries.ITEM, CANCER_LEAVES_ID, CANCER_LEAVES_BLOCK_ITEM)

		Registry.register(Registries.BLOCK, CHEMOTHERAPEUTIC_DRUG_ID, CHEMOTHERAPEUTIC_DRUG_BLOCK)
		Registry.register(Registries.ITEM, CHEMOTHERAPEUTIC_DRUG_ID, CHEMOTHERAPEUTIC_DRUG_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, PROCESSING_TABLE_ID, PROCESSING_TABLE_BLOCK)
		Registry.register(Registries.ITEM, PROCESSING_TABLE_ID, PROCESSING_TABLE_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, TARGETED_DRUG_INJECTOR_ID, TARGETED_DRUG_INJECTOR_BLOCK)
		Registry.register(Registries.ITEM, TARGETED_DRUG_INJECTOR_ID, TARGETED_DRUG_INJECTOR_BLOCK_ITEM)

		Registry.register(Registries.BLOCK_ENTITY_TYPE, PROCESSING_TABLE_ID, PROCESSING_TABLE_BLOCK_ENTITY)

		Registry.register(Registries.ITEM, CANCEROUS_MATERIAL_ID, CANCEROUS_MATERIAL_ITEM)
		Registry.register(Registries.ITEM, DEBUG_TOOL_ITEM_ID, DEBUG_TOOL_ITEM)
		Registry.register(Registries.ITEM, PROBE_ITEM_ID, PROBE_ITEM)
		Registry.register(Registries.ITEM, RADIATION_STAFF_ITEM_ID, RADIATION_STAFF_ITEM)
		Registry.register(Registries.ITEM, RESEARCH_ID, RESEARCH_ITEM)
		Registry.register(Registries.ITEM, SCANNER_ITEM_ID, SCANNER_ITEM)
		Registry.register(Registries.ITEM, SURGERY_AXE_ITEM_ID, SURGERY_AXE_ITEM)
		Registry.register(Registries.ITEM, SURGERY_HOE_ITEM_ID, SURGERY_HOE_ITEM)
		Registry.register(Registries.ITEM, SURGERY_PICKAXE_ITEM_ID, SURGERY_PICKAXE_ITEM)
		Registry.register(Registries.ITEM, SURGERY_SHOVEL_ITEM_ID, SURGERY_SHOVEL_ITEM)

		Registry.register(Registries.ITEM_GROUP, identifier("default"), ITEM_GROUP)

		Registry.register(Registries.ENTITY_TYPE, CHEMOTHERAPEUTIC_DRUG_ID, CHEMOTHERAPEUTIC_DRUG_ENTITY_TYPE)

		Registry.register(Registries.SCREEN_HANDLER, identifier("processing_table_analyzer"), PROCESSING_TABLE_ANALYZER_SCREEN_HANDLER)

		Storage.init()

		BossBarController.init()
		CancerSpreadController.init()

		// TODO: replace cancer block textures
		// TODO: add cancerous material and research item textures
		// TODO: create research GUI
		// TODO: create research state store (per player?)
		// TODO: implement towers/beacons
		// TODO: implement radiation staff recharge rate and/or heat capacity research
		// TODO: fix model transformations of surgery tools (on-ground is MASSIVE) .-.
		// TODO: spawn mobs
		// TODO: make cancerous blocks hurt to walk on
		// TODO: metastatic (spreading with a gap)
		// TODO: stretch goal: liquid cancer
	}
}
