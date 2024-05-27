package com.carpercreative.preventthespread

import com.carpercreative.preventthespread.block.CancerLeavesBlock
import com.carpercreative.preventthespread.block.CancerPillarBlock
import com.carpercreative.preventthespread.block.CancerSlabBlock
import com.carpercreative.preventthespread.block.CancerStairsBlock
import com.carpercreative.preventthespread.block.CancerousBlock
import com.carpercreative.preventthespread.block.ChemotherapeuticDrugBlock
import com.carpercreative.preventthespread.block.ChillingTowerBlock
import com.carpercreative.preventthespread.block.ProcessingTableBlock
import com.carpercreative.preventthespread.block.SolidCancerBlock
import com.carpercreative.preventthespread.block.TargetedDrugInjectorBlock
import com.carpercreative.preventthespread.block.TowerBlock
import com.carpercreative.preventthespread.blockEntity.ProcessingTableAnalyzerBlockEntity
import com.carpercreative.preventthespread.blockEntity.ProcessingTableResearchBlockEntity
import com.carpercreative.preventthespread.challenge.controller.ChallengeStatusController
import com.carpercreative.preventthespread.challenge.controller.ChallengeValidityController
import com.carpercreative.preventthespread.controller.BossBarController
import com.carpercreative.preventthespread.controller.CancerSpreadController
import com.carpercreative.preventthespread.controller.CancerTouchEffectController
import com.carpercreative.preventthespread.controller.ClientStateSynchronizationController
import com.carpercreative.preventthespread.controller.EveryoneTeamController
import com.carpercreative.preventthespread.controller.ResearchSynchronizationController
import com.carpercreative.preventthespread.controller.StoryRootUnlockController
import com.carpercreative.preventthespread.entity.ChemotherapeuticDrugEntity
import com.carpercreative.preventthespread.item.DebugToolItem
import com.carpercreative.preventthespread.item.ProbeItem
import com.carpercreative.preventthespread.item.RadiationStaffItem
import com.carpercreative.preventthespread.item.ScannerItem
import com.carpercreative.preventthespread.item.SurgeryAxeItem
import com.carpercreative.preventthespread.item.SurgeryHoeItem
import com.carpercreative.preventthespread.item.SurgeryPickaxeItem
import com.carpercreative.preventthespread.item.SurgeryShovelItem
import com.carpercreative.preventthespread.networking.SelectResearchPacket
import com.carpercreative.preventthespread.screen.ProcessingTableAnalyzerScreenHandler
import com.carpercreative.preventthespread.screen.ProcessingTableResearchScreenHandler
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.resource.ResourcePackActivationType
import net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.MapColor
import net.minecraft.block.entity.BlockEntity
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
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.resource.featuretoggle.FeatureFlags
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.Rarity
import net.minecraft.world.GameRules
import net.minecraft.world.poi.PointOfInterestType
import net.minecraft.world.poi.PointOfInterestTypes

@Suppress("MemberVisibilityCanBePrivate")
object PreventTheSpread : ModInitializer {
	const val MOD_ID = "preventthespread"

	fun identifier(path: String) = Identifier(MOD_ID, path)

	val GLITCH_DIRT_ID = identifier("glitch_dirt")
	val GLITCH_DIRT_BLOCK = SolidCancerBlock(CancerousBlock.defaultBlockSettings().strength(1.5f))
	val GLITCH_DIRT_BLOCK_ITEM = BlockItem(GLITCH_DIRT_BLOCK, FabricItemSettings())
	val GLITCH_LOG_ID = identifier("glitch_log")
	val GLITCH_LOG_BLOCK = CancerPillarBlock(CancerousBlock.defaultBlockSettings())
	val GLITCH_LOG_BLOCK_ITEM = BlockItem(GLITCH_LOG_BLOCK, FabricItemSettings())
	val GLITCH_PLANKS_ID = identifier("glitch_planks")
	val GLITCH_PLANKS_BLOCK = SolidCancerBlock(CancerousBlock.defaultBlockSettings().strength(2.5f, 3f))
	val GLITCH_PLANKS_BLOCK_ITEM = BlockItem(GLITCH_PLANKS_BLOCK, FabricItemSettings())
	val GLITCH_STONE_ID = identifier("glitch_stone")
	val GLITCH_STONE_BLOCK = SolidCancerBlock(CancerousBlock.defaultBlockSettings().requiresTool())
	val GLITCH_STONE_BLOCK_ITEM = BlockItem(GLITCH_STONE_BLOCK, FabricItemSettings())

	val GLITCH_DIRT_SLAB_ID = identifier("glitch_dirt_slab")
	val GLITCH_DIRT_SLAB_BLOCK = CancerSlabBlock(FabricBlockSettings.copy(GLITCH_DIRT_BLOCK))
	val GLITCH_DIRT_SLAB_BLOCK_ITEM = BlockItem(GLITCH_DIRT_SLAB_BLOCK, FabricItemSettings())
	val GLITCH_DIRT_STAIRS_ID = identifier("glitch_dirt_stairs")
	val GLITCH_DIRT_STAIRS_BLOCK = CancerStairsBlock(GLITCH_DIRT_BLOCK.defaultState, FabricBlockSettings.copy(GLITCH_DIRT_BLOCK))
	val GLITCH_DIRT_STAIRS_BLOCK_ITEM = BlockItem(GLITCH_DIRT_STAIRS_BLOCK, FabricItemSettings())
	val GLITCH_PLANKS_SLAB_ID = identifier("glitch_planks_slab")
	val GLITCH_PLANKS_SLAB_BLOCK = CancerSlabBlock(FabricBlockSettings.copy(GLITCH_PLANKS_BLOCK))
	val GLITCH_PLANKS_SLAB_BLOCK_ITEM = BlockItem(GLITCH_PLANKS_SLAB_BLOCK, FabricItemSettings())
	val GLITCH_PLANKS_STAIRS_ID = identifier("glitch_planks_stairs")
	val GLITCH_PLANKS_STAIRS_BLOCK = CancerStairsBlock(GLITCH_PLANKS_BLOCK.defaultState, FabricBlockSettings.copy(GLITCH_PLANKS_BLOCK))
	val GLITCH_PLANKS_STAIRS_BLOCK_ITEM = BlockItem(GLITCH_PLANKS_STAIRS_BLOCK, FabricItemSettings())
	val GLITCH_STONE_SLAB_ID = identifier("glitch_stone_slab")
	val GLITCH_STONE_SLAB_BLOCK = CancerSlabBlock(FabricBlockSettings.copy(GLITCH_STONE_BLOCK))
	val GLITCH_STONE_SLAB_BLOCK_ITEM = BlockItem(GLITCH_STONE_SLAB_BLOCK, FabricItemSettings())
	val GLITCH_STONE_STAIRS_ID = identifier("glitch_stone_stairs")
	val GLITCH_STONE_STAIRS_BLOCK = CancerStairsBlock(GLITCH_STONE_BLOCK.defaultState, FabricBlockSettings.copy(GLITCH_STONE_BLOCK))
	val GLITCH_STONE_STAIRS_BLOCK_ITEM = BlockItem(GLITCH_STONE_STAIRS_BLOCK, FabricItemSettings())

	val GLITCH_LEAVES_ID = identifier("glitch_leaves")
	val GLITCH_LEAVES_BLOCK = CancerLeavesBlock(
		CancerousBlock.defaultBlockSettings()
			.allowsSpawning(Blocks::canSpawnOnLeaves)
			.blockVision(Blocks::never)
			.nonOpaque()
			.pistonBehavior(PistonBehavior.DESTROY)
			.solidBlock(Blocks::never)
			.strength(0.2f)
			.suffocates(Blocks::never)
	)
	val GLITCH_LEAVES_BLOCK_ITEM = BlockItem(GLITCH_LEAVES_BLOCK, FabricItemSettings())

	val CHEMOTHERAPEUTIC_DRUG_ID = identifier("chemotherapeutic_drug")
	val CHEMOTHERAPEUTIC_DRUG_BLOCK = ChemotherapeuticDrugBlock(FabricBlockSettings.create().mapColor(MapColor.LIGHT_BLUE).nonOpaque().breakInstantly().sounds(BlockSoundGroup.GRASS).solidBlock(Blocks::never))
	val CHEMOTHERAPEUTIC_DRUG_BLOCK_ITEM = BlockItem(CHEMOTHERAPEUTIC_DRUG_BLOCK, FabricItemSettings())
	val CHILLING_TOWER_ID = identifier("chilling_tower")
	val CHILLING_TOWER_BLOCK = ChillingTowerBlock(FabricBlockSettings.create().mapColor(MapColor.BROWN).nonOpaque().sounds(BlockSoundGroup.METAL).solidBlock(Blocks::never))
	val CHILLING_TOWER_BLOCK_ITEM = BlockItem(CHILLING_TOWER_BLOCK, FabricItemSettings())
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

	val PROCESSING_TABLE_BLOCK_ENTITY: BlockEntityType<BlockEntity> = BlockEntityType.Builder.create({ pos, state ->
		@Suppress("USELESS_CAST") // Idea/Kotlin compiler incorrectly reports the cast as useless because its heuristics are wrong.
		when (val propertyValue = state.get(ProcessingTableBlock.PROCESSING_TABLE_PART)) {
			ProcessingTableBlock.ProcessingTablePart.LEFT -> ProcessingTableAnalyzerBlockEntity(pos, state)
			ProcessingTableBlock.ProcessingTablePart.RIGHT -> ProcessingTableResearchBlockEntity(pos, state)
			else -> throw IllegalArgumentException("Unknown property value: $propertyValue")
		} as BlockEntity
	}, PROCESSING_TABLE_BLOCK).build()

	val GLITCH_MATERIAL_ID = identifier("glitch_material")
	val GLITCH_MATERIAL_ITEM = Item(Item.Settings())
	val DEBUG_TOOL_ITEM_ID = identifier("debug_tool")
	val DEBUG_TOOL_ITEM = DebugToolItem(FabricItemSettings().maxCount(1).rarity(Rarity.EPIC))
	val PROBE_ITEM_ID = identifier("probe")
	val PROBE_ITEM = ProbeItem(FabricItemSettings().maxCount(1))
	val RADIATION_STAFF_ITEM_ID = identifier("radiation_staff")
	val RADIATION_STAFF_ITEM = RadiationStaffItem(FabricItemSettings().maxCount(1).maxDamage(60 * RadiationStaffItem.getHeatingPerTick(0)).customDamage(RadiationStaffItem.RadiationBeamGunDamageHandler))
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

	val GLITCHABLE_BLOCK_TAG: TagKey<Block> = TagKey.of(RegistryKeys.BLOCK, identifier("glitchable"))
	val GLITCHED_BLOCK_TAG: TagKey<Block> = TagKey.of(RegistryKeys.BLOCK, identifier("glitched"))
	val SURGERY_AXE_MINEABLE_BLOCK_TAG: TagKey<Block> = TagKey.of(RegistryKeys.BLOCK, identifier("mineable/surgery_axe"))
	val SURGERY_HOE_MINEABLE_BLOCK_TAG: TagKey<Block> = TagKey.of(RegistryKeys.BLOCK, identifier("mineable/surgery_hoe"))
	val SURGERY_PICKAXE_MINEABLE_BLOCK_TAG: TagKey<Block> = TagKey.of(RegistryKeys.BLOCK, identifier("mineable/surgery_pickaxe"))
	val SURGERY_SHOVEL_MINEABLE_BLOCK_TAG: TagKey<Block> = TagKey.of(RegistryKeys.BLOCK, identifier("mineable/surgery_shovel"))
	val VALID_GLITCH_SEED_BLOCK_TAG: TagKey<Block> = TagKey.of(RegistryKeys.BLOCK, identifier("valid_glitch_seed"))

	val REQUIRES_RECIPE_TO_CRAFT_ITEM_TAG: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, identifier("requires_recipe_to_craft"))
	val SURGERY_TOOL_ITEM_TAG: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, identifier("surgery_tool"))

	val CHEMOTHERAPEUTIC_DRUG_ENTITY_TYPE: EntityType<ChemotherapeuticDrugEntity> = EntityType.Builder.create({ entityType, world -> ChemotherapeuticDrugEntity(entityType, world) }, SpawnGroup.MISC).makeFireImmune().setDimensions(0.98f, 0.98f).maxTrackingRange(10).trackingTickInterval(10).build()

	val PROCESSING_TABLE_ANALYZER_SCREEN_HANDLER = ScreenHandlerType(::ProcessingTableAnalyzerScreenHandler, FeatureFlags.VANILLA_FEATURES)
	val PROCESSING_TABLE_RESEARCH_SCREEN_HANDLER = ScreenHandlerType(::ProcessingTableResearchScreenHandler, FeatureFlags.VANILLA_FEATURES)

	val CHILLING_TOWER_POI_TYPE: RegistryKey<PointOfInterestType> = RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, CHILLING_TOWER_ID)
	lateinit var CHILLING_TOWER_POI: PointOfInterestType
		private set

	val GLITCH_PROGRESS_PACKET_ID = identifier("glitch_progress")
	val SELECT_RESEARCH_PACKET_ID = identifier("select_research")

	val DO_GLITCH_SPAWNING_GAME_RULE: GameRules.Key<GameRules.BooleanRule> = GameRuleRegistry.register("$MOD_ID:doGlitchSpawning", GameRules.Category.UPDATES, GameRuleFactory.createBooleanRule(false))
	val DO_GLITCH_SPREAD_GAME_RULE: GameRules.Key<GameRules.BooleanRule> = GameRuleRegistry.register("$MOD_ID:doGlitchSpread", GameRules.Category.UPDATES, GameRuleFactory.createBooleanRule(true))

	private val ITEM_GROUP = FabricItemGroup.builder()
		.icon { ItemStack(GLITCH_DIRT_BLOCK_ITEM) }
		.displayName(Text.translatable("itemGroup.$MOD_ID.default"))
		.entries { context, entries ->
			entries.add(SCANNER_ITEM)
			entries.add(PROBE_ITEM)
			entries.add(PROCESSING_TABLE_BLOCK_ITEM)
			entries.add(GLITCH_MATERIAL_ITEM)
			entries.add(RESEARCH_ITEM)
			entries.add(CHEMOTHERAPEUTIC_DRUG_BLOCK_ITEM)
			entries.add(RADIATION_STAFF_ITEM)
			entries.add(SURGERY_AXE_ITEM)
			entries.add(SURGERY_HOE_ITEM)
			entries.add(SURGERY_PICKAXE_ITEM)
			entries.add(SURGERY_SHOVEL_ITEM)
			entries.add(TARGETED_DRUG_INJECTOR_BLOCK_ITEM)
			entries.add(CHILLING_TOWER_BLOCK_ITEM)
			entries.add(GLITCH_DIRT_BLOCK_ITEM)
			entries.add(GLITCH_LOG_BLOCK_ITEM)
			entries.add(GLITCH_PLANKS_BLOCK_ITEM)
			entries.add(GLITCH_STONE_BLOCK_ITEM)
			entries.add(GLITCH_DIRT_SLAB_BLOCK_ITEM)
			entries.add(GLITCH_DIRT_STAIRS_BLOCK_ITEM)
			entries.add(GLITCH_PLANKS_SLAB_BLOCK_ITEM)
			entries.add(GLITCH_PLANKS_STAIRS_BLOCK_ITEM)
			entries.add(GLITCH_STONE_SLAB_BLOCK_ITEM)
			entries.add(GLITCH_STONE_STAIRS_BLOCK_ITEM)
			entries.add(GLITCH_LEAVES_BLOCK_ITEM)
			entries.add(DEBUG_TOOL_ITEM)
		}
		.build()

	object StoryAdvancement {
		private fun storyIdentifier(path: String) = PreventTheSpread.identifier("story/$path")

		val ROOT_ID = storyIdentifier("root")
		val OBTAIN_PROBE_ID = storyIdentifier("obtain_probe")
		val GET_SAMPLE_ID = storyIdentifier("get_sample")
		val CRAFT_PROCESSING_TABLE_ID = storyIdentifier("craft_processing_table")
		val ANALYZE_SAMPLE_ID = storyIdentifier("analyze_sample")
		val DEFEAT_BLOB_ID = storyIdentifier("defeat_blob")
		val PROCESS_GLITCH_MATERIAL_ID = storyIdentifier("process_glitch_material")
		val UNLOCK_TREATMENT_ID = storyIdentifier("unlock_treatment")
	}

	object ResearchAdvancement {
		private fun researchIdentifier(path: String) = PreventTheSpread.identifier("research/$path")

		val ALL_IDS: Set<Identifier> by lazy {
			ResearchAdvancement::class.java.declaredMethods.asSequence()
				.filter {
					it.canAccess(ResearchAdvancement)
					&& it.returnType == Identifier::class.java
					&& it.parameterCount == 0
				}
				.map { it.invoke(ResearchAdvancement) as Identifier }
				.toHashSet()
		}

		val ROOT_ID = researchIdentifier("root")
		val SURGERY_EFFICIENCY_1_ID = researchIdentifier("surgery_efficiency_1")
		val SURGERY_EFFICIENCY_2_ID = researchIdentifier("surgery_efficiency_2")
		val CHEMOTHERAPEUTIC_DRUG_ID = researchIdentifier(PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_ID.path)
		val CHEAPER_CHEMOTHERAPEUTIC_DRUG_ID = researchIdentifier("cheaper_${PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_ID.path}")
		val CHEMOTHERAPEUTIC_DRUG_STRENGTH_1_ID = researchIdentifier("${PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_ID.path}_strength_1")
		val CHEMOTHERAPEUTIC_DRUG_STRENGTH_2_ID = researchIdentifier("${PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_ID.path}_strength_2")
		val CHILLING_TOWER_ID = researchIdentifier(PreventTheSpread.CHILLING_TOWER_ID.path)
		val RADIATION_STAFF_ID = researchIdentifier(RADIATION_STAFF_ITEM_ID.path)
		val RADIATION_STAFF_HEAT_1_ID = researchIdentifier("${RADIATION_STAFF_ITEM_ID.path}_heat_1")
		val RADIATION_STAFF_RAYS_1_ID = researchIdentifier("${RADIATION_STAFF_ITEM_ID.path}_rays_1")
		val RADIATION_STAFF_STRENGTH_1_ID = researchIdentifier("${RADIATION_STAFF_ITEM_ID.path}_strength_1")
		val RADIATION_STAFF_STRENGTH_2_ID = researchIdentifier("${RADIATION_STAFF_ITEM_ID.path}_strength_2")
		val TARGETED_DRUG_ID = researchIdentifier(TARGETED_DRUG_INJECTOR_ID.path)
		val CHEAPER_TARGETED_DRUG_ID = researchIdentifier("cheaper_${TARGETED_DRUG_INJECTOR_ID.path}")
		val TARGETED_DRUG_STRENGTH_1_ID = researchIdentifier("${TARGETED_DRUG_INJECTOR_ID.path}_strength_1")
		val TARGETED_DRUG_STRENGTH_2_ID = researchIdentifier("${TARGETED_DRUG_INJECTOR_ID.path}_strength_2")
	}

	override fun onInitialize() {
		Registry.register(Registries.BLOCK, GLITCH_DIRT_ID, GLITCH_DIRT_BLOCK)
		Registry.register(Registries.ITEM, GLITCH_DIRT_ID, GLITCH_DIRT_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, GLITCH_LOG_ID, GLITCH_LOG_BLOCK)
		Registry.register(Registries.ITEM, GLITCH_LOG_ID, GLITCH_LOG_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, GLITCH_PLANKS_ID, GLITCH_PLANKS_BLOCK)
		Registry.register(Registries.ITEM, GLITCH_PLANKS_ID, GLITCH_PLANKS_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, GLITCH_STONE_ID, GLITCH_STONE_BLOCK)
		Registry.register(Registries.ITEM, GLITCH_STONE_ID, GLITCH_STONE_BLOCK_ITEM)

		Registry.register(Registries.BLOCK, GLITCH_DIRT_SLAB_ID, GLITCH_DIRT_SLAB_BLOCK)
		Registry.register(Registries.ITEM, GLITCH_DIRT_SLAB_ID, GLITCH_DIRT_SLAB_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, GLITCH_DIRT_STAIRS_ID, GLITCH_DIRT_STAIRS_BLOCK)
		Registry.register(Registries.ITEM, GLITCH_DIRT_STAIRS_ID, GLITCH_DIRT_STAIRS_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, GLITCH_PLANKS_SLAB_ID, GLITCH_PLANKS_SLAB_BLOCK)
		Registry.register(Registries.ITEM, GLITCH_PLANKS_SLAB_ID, GLITCH_PLANKS_SLAB_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, GLITCH_PLANKS_STAIRS_ID, GLITCH_PLANKS_STAIRS_BLOCK)
		Registry.register(Registries.ITEM, GLITCH_PLANKS_STAIRS_ID, GLITCH_PLANKS_STAIRS_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, GLITCH_STONE_SLAB_ID, GLITCH_STONE_SLAB_BLOCK)
		Registry.register(Registries.ITEM, GLITCH_STONE_SLAB_ID, GLITCH_STONE_SLAB_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, GLITCH_STONE_STAIRS_ID, GLITCH_STONE_STAIRS_BLOCK)
		Registry.register(Registries.ITEM, GLITCH_STONE_STAIRS_ID, GLITCH_STONE_STAIRS_BLOCK_ITEM)

		Registry.register(Registries.BLOCK, GLITCH_LEAVES_ID, GLITCH_LEAVES_BLOCK)
		Registry.register(Registries.ITEM, GLITCH_LEAVES_ID, GLITCH_LEAVES_BLOCK_ITEM)

		Registry.register(Registries.BLOCK, CHEMOTHERAPEUTIC_DRUG_ID, CHEMOTHERAPEUTIC_DRUG_BLOCK)
		Registry.register(Registries.ITEM, CHEMOTHERAPEUTIC_DRUG_ID, CHEMOTHERAPEUTIC_DRUG_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, CHILLING_TOWER_ID, CHILLING_TOWER_BLOCK)
		Registry.register(Registries.ITEM, CHILLING_TOWER_ID, CHILLING_TOWER_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, PROCESSING_TABLE_ID, PROCESSING_TABLE_BLOCK)
		Registry.register(Registries.ITEM, PROCESSING_TABLE_ID, PROCESSING_TABLE_BLOCK_ITEM)
		Registry.register(Registries.BLOCK, TARGETED_DRUG_INJECTOR_ID, TARGETED_DRUG_INJECTOR_BLOCK)
		Registry.register(Registries.ITEM, TARGETED_DRUG_INJECTOR_ID, TARGETED_DRUG_INJECTOR_BLOCK_ITEM)

		Registry.register(Registries.BLOCK_ENTITY_TYPE, PROCESSING_TABLE_ID, PROCESSING_TABLE_BLOCK_ENTITY)

		Registry.register(Registries.ITEM, GLITCH_MATERIAL_ID, GLITCH_MATERIAL_ITEM)
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
		Registry.register(Registries.SCREEN_HANDLER, identifier("processing_table_research"), PROCESSING_TABLE_RESEARCH_SCREEN_HANDLER)

		CHILLING_TOWER_POI = PointOfInterestTypes.register(Registries.POINT_OF_INTEREST_TYPE, CHILLING_TOWER_POI_TYPE, CHILLING_TOWER_BLOCK.stateManager.states.filter { it.get(TowerBlock.PART) == TowerBlock.TowerPart.MIDDLE }.toSet(), 0, 1)

		ServerPlayNetworking.registerGlobalReceiver(SELECT_RESEARCH_PACKET_ID, SelectResearchPacket::handle)

		Storage.init()

		BossBarController.init()
		CancerSpreadController.init()
		CancerTouchEffectController.init()
		ChallengeStatusController.init()
		ChallengeValidityController.init()
		ClientStateSynchronizationController.init()
		EveryoneTeamController.init()
		ResearchSynchronizationController.init()
		StoryRootUnlockController.init()

		@Suppress("UnstableApiUsage")
		ResourceManagerHelperImpl.registerBuiltinResourcePack(
			ChallengeConstants.DATA_PACK_ID,
			"data/$MOD_ID/datapacks/" + ChallengeConstants.DATA_PACK_ID.path,
			FabricLoader.getInstance().getModContainer(MOD_ID).get(),
			Text.translatable("dataPack.preventthespread.challenge.displayName"),
			ResourcePackActivationType.NORMAL,
		)

		// TODO: create research state store (per team)
		// TODO: fix model transformations of surgery tools (on-ground is MASSIVE) .-.
		// TODO: spawn mobs
		// TODO: stretch goal: liquid cancer
	}
}
