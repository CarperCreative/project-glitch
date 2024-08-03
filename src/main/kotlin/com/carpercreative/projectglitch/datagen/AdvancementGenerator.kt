package com.carpercreative.projectglitch.datagen

import com.carpercreative.projectglitch.ProjectGlitch
import com.carpercreative.projectglitch.ProjectGlitch.ResearchAdvancement
import com.carpercreative.projectglitch.ProjectGlitch.StoryAdvancement
import com.carpercreative.projectglitch.blockEntity.ProcessingTableAnalyzerBlockEntity
import java.util.Optional
import java.util.function.Consumer
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider
import net.minecraft.advancement.Advancement
import net.minecraft.advancement.AdvancementEntry
import net.minecraft.advancement.AdvancementFrame
import net.minecraft.advancement.AdvancementRequirements
import net.minecraft.advancement.AdvancementRewards
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.advancement.criterion.ImpossibleCriterion
import net.minecraft.advancement.criterion.InventoryChangedCriterion
import net.minecraft.advancement.criterion.ItemCriterion
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.WrittenBookItem
import net.minecraft.nbt.NbtCompound
import net.minecraft.predicate.BlockPredicate
import net.minecraft.predicate.entity.LocationPredicate
import net.minecraft.predicate.item.ItemPredicate
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class AdvancementGenerator(
	output: FabricDataOutput,
) : FabricAdvancementProvider(output) {
	private fun Consumer<AdvancementEntry>.createAdvancement(
		identifier: Identifier,
		parent: AdvancementEntry,
		displayStack: ItemStack,
		hidden: Boolean = false,
		callback: (Advancement.Builder.() -> Unit)? = null,
	): AdvancementEntry {
		return Advancement.Builder.createUntelemetered()
			.parent(parent)
			.display(
				displayStack,
				identifier.titleText,
				identifier.descriptionText,
				null,
				AdvancementFrame.TASK,
				true,
				false,
				hidden,
			)
			.also { callback?.invoke(it) }
			.build(this, identifier)
	}

	private fun Consumer<AdvancementEntry>.createResearchAdvancement(
		identifier: Identifier,
		parent: AdvancementEntry,
		displayStack: ItemStack,
		callback: (Advancement.Builder.() -> Unit)? = null,
	): AdvancementEntry {
		return createAdvancement(identifier, parent, displayStack) {
			withImpossibleCriterion()
			callback?.invoke(this)
		}
	}

	private fun Advancement.Builder.withImpossibleCriterion(): Advancement.Builder {
		return criterion("never", Criteria.IMPOSSIBLE.create(ImpossibleCriterion.Conditions()))
	}

	override fun generateAdvancement(consumer: Consumer<AdvancementEntry>) {
		val storyRoot = Advancement.Builder.createUntelemetered()
			.display(
				ProjectGlitch.GLITCH_STONE_BLOCK_ITEM.defaultStack,
				StoryAdvancement.ROOT_ID.titleText,
				StoryAdvancement.ROOT_ID.descriptionText,
				Identifier("textures/gui/advancements/backgrounds/adventure.png"),
				AdvancementFrame.TASK,
				true,
				false,
				false,
			)
			.withImpossibleCriterion()
			.rewards(AdvancementRewards.Builder.recipe(ProjectGlitch.PROBE_ITEM_ID))
			.build(consumer, StoryAdvancement.ROOT_ID)

		val obtainProbe = consumer.createAdvancement(
			StoryAdvancement.OBTAIN_PROBE_ID,
			storyRoot,
			ProjectGlitch.PROBE_ITEM.defaultStack,
		) {
			criterion("obtained_probe", InventoryChangedCriterion.Conditions.items(ProjectGlitch.PROBE_ITEM))

			rewards(AdvancementRewards.Builder.recipe(ProjectGlitch.SCANNER_ITEM_ID))
		}

		val obtainScanner = consumer.createAdvancement(
			StoryAdvancement.OBTAIN_SCANNER_ID,
			obtainProbe,
			ProjectGlitch.SCANNER_ITEM.defaultStack,
		) {
			criterion("obtained_scanner", InventoryChangedCriterion.Conditions.items(ProjectGlitch.SCANNER_ITEM))
		}

		val getSample = consumer.createAdvancement(
			StoryAdvancement.GET_SAMPLE_ID,
			obtainScanner,
			ProjectGlitch.GLITCH_STONE_BLOCK_ITEM.defaultStack,
		) {
			criterion("obtained_sample", ItemCriterion.Conditions.createItemUsedOnBlock(
				LocationPredicate.Builder.create()
					.block(BlockPredicate.Builder.create().tag(ProjectGlitch.GLITCHED_BLOCK_TAG)),
				ItemPredicate.Builder.create().items(ProjectGlitch.PROBE_ITEM),
			))

			rewards(AdvancementRewards.Builder.recipe(ProjectGlitch.PROCESSING_TABLE_ID))
		}

		val craftProcessingTable = consumer.createAdvancement(
			StoryAdvancement.CRAFT_PROCESSING_TABLE_ID,
			getSample,
			ProjectGlitch.PROCESSING_TABLE_BLOCK_ITEM.defaultStack,
		) {
			criterion("obtained_processing_table", InventoryChangedCriterion.Conditions.items(ProjectGlitch.PROCESSING_TABLE_BLOCK_ITEM))

			rewards(
				AdvancementRewards.Builder()
					.addRecipe(ProjectGlitch.SURGERY_AXE_ITEM_ID)
					.addRecipe(ProjectGlitch.SURGERY_HOE_ITEM_ID)
					.addRecipe(ProjectGlitch.SURGERY_PICKAXE_ITEM_ID)
					.addRecipe(ProjectGlitch.SURGERY_SHOVEL_ITEM_ID)
			)
		}

		val analyzeSample = consumer.createAdvancement(
			StoryAdvancement.ANALYZE_SAMPLE_ID,
			craftProcessingTable,
			Items.WRITTEN_BOOK.defaultStack,
		) {
			criterion(
				"obtained_analysis_results",
				InventoryChangedCriterion.Conditions.items(
					ItemPredicate.Builder.create()
						.items(Items.WRITTEN_BOOK)
						.nbt(
							NbtCompound().apply {
								putString(WrittenBookItem.AUTHOR_KEY, ProcessingTableAnalyzerBlockEntity.BOOK_AUTHOR)
								putString(WrittenBookItem.TITLE_KEY, ProcessingTableAnalyzerBlockEntity.BOOK_TITLE)
							}
						)
				)
			)
		}

		val defeatBlob = consumer.createAdvancement(
			StoryAdvancement.DEFEAT_BLOB_ID,
			analyzeSample,
			ProjectGlitch.SURGERY_PICKAXE_ITEM.defaultStack,
		) {
			criterion("obtained_glitch_material", InventoryChangedCriterion.Conditions.items(ProjectGlitch.GLITCH_MATERIAL_ITEM))
		}

		val processGlitchMaterial = consumer.createAdvancement(
			StoryAdvancement.PROCESS_GLITCH_MATERIAL_ID,
			defeatBlob,
			ProjectGlitch.GLITCH_MATERIAL_ITEM.defaultStack,
		) {
			criterion("obtained_research", InventoryChangedCriterion.Conditions.items(ProjectGlitch.RESEARCH_ITEM))
		}

		val unlockTreatment = consumer.createAdvancement(
			StoryAdvancement.UNLOCK_TREATMENT_ID,
			processGlitchMaterial,
			ProjectGlitch.PROCESSING_TABLE_BLOCK_ITEM.defaultStack,
		) {
			criteriaMerger(AdvancementRequirements.CriterionMerger.OR)
			criterion("unlocked_${ProjectGlitch.CHEMOTHERAPEUTIC_DRUG_ID.path}", Criteria.RECIPE_UNLOCKED.create(RecipeUnlockedCriterion.Conditions(Optional.empty(), ProjectGlitch.CHEMOTHERAPEUTIC_DRUG_ID)))
			criterion("unlocked_${ProjectGlitch.RADIATION_STAFF_ITEM_ID.path}", Criteria.RECIPE_UNLOCKED.create(RecipeUnlockedCriterion.Conditions(Optional.empty(), ProjectGlitch.RADIATION_STAFF_ITEM_ID)))
			criterion("unlocked_${ProjectGlitch.TARGETED_DRUG_INJECTOR_ID.path}", Criteria.RECIPE_UNLOCKED.create(RecipeUnlockedCriterion.Conditions(Optional.empty(), ProjectGlitch.TARGETED_DRUG_INJECTOR_ID)))
		}

		consumer.createAdvancement(
			StoryAdvancement.GAME_OVER_ID,
			unlockTreatment,
			ProjectGlitch.GLITCH_STONE_BLOCK_ITEM.defaultStack,
			hidden = true,
		) {
			criterion("never", Criteria.IMPOSSIBLE.create(ImpossibleCriterion.Conditions()))
		}

		val researchRoot = Advancement.Builder.createUntelemetered()
			.display(
				ProjectGlitch.PROCESSING_TABLE_BLOCK_ITEM.defaultStack,
				ResearchAdvancement.ROOT_ID.titleText,
				ResearchAdvancement.ROOT_ID.descriptionText,
				Identifier("textures/gui/advancements/backgrounds/adventure.png"),
				AdvancementFrame.TASK,
				false,
				false,
				false,
			)
			.withImpossibleCriterion()
			.build(consumer, ResearchAdvancement.ROOT_ID)

		// Surgery tools.
		val surgeryEfficiency1 = consumer.createResearchAdvancement(
			ResearchAdvancement.SURGERY_EFFICIENCY_1_ID,
			researchRoot,
			ProjectGlitch.SURGERY_PICKAXE_ITEM.defaultStack,
		)

		consumer.createResearchAdvancement(
			ResearchAdvancement.SURGERY_EFFICIENCY_2_ID,
			surgeryEfficiency1,
			ProjectGlitch.SURGERY_PICKAXE_ITEM.defaultStack,
		)

		// Chemotherapeutic drug.
		val chemotherapeuticDrug = consumer.createResearchAdvancement(
			ResearchAdvancement.CHEMOTHERAPEUTIC_DRUG_ID,
			researchRoot,
			ProjectGlitch.CHEMOTHERAPEUTIC_DRUG_BLOCK_ITEM.defaultStack,
		) {
			rewards(AdvancementRewards.Builder.recipe(ProjectGlitch.CHEMOTHERAPEUTIC_DRUG_ID))
		}

		consumer.createResearchAdvancement(
			ResearchAdvancement.CHEAPER_CHEMOTHERAPEUTIC_DRUG_ID,
			chemotherapeuticDrug,
			ProjectGlitch.CHEMOTHERAPEUTIC_DRUG_BLOCK_ITEM.defaultStack,
		) {
			rewards(AdvancementRewards.Builder.recipe(ResearchAdvancement.CHEAPER_CHEMOTHERAPEUTIC_DRUG_ID))
		}

		consumer.createResearchAdvancement(
			ResearchAdvancement.CHEMOTHERAPEUTIC_DRUG_STRENGTH_1_ID,
			chemotherapeuticDrug,
			ProjectGlitch.CHEMOTHERAPEUTIC_DRUG_BLOCK_ITEM.defaultStack,
		).also {
			consumer.createResearchAdvancement(
				ResearchAdvancement.CHEMOTHERAPEUTIC_DRUG_STRENGTH_2_ID,
				it,
				ProjectGlitch.CHEMOTHERAPEUTIC_DRUG_BLOCK_ITEM.defaultStack,
			)
		}

		// Chilling tower.
		consumer.createResearchAdvancement(
			ResearchAdvancement.CHILLING_TOWER_ID,
			researchRoot,
			ProjectGlitch.CHILLING_TOWER_BLOCK_ITEM.defaultStack,
		) {
			rewards(AdvancementRewards.Builder.recipe(ProjectGlitch.CHILLING_TOWER_ID))
		}

		// Radiation staff.
		val radiationStaff = consumer.createResearchAdvancement(
			ResearchAdvancement.RADIATION_STAFF_ID,
			researchRoot,
			ProjectGlitch.RADIATION_STAFF_ITEM.defaultStack,
		) {
			rewards(AdvancementRewards.Builder.recipe(ProjectGlitch.RADIATION_STAFF_ITEM_ID))
		}

		consumer.createResearchAdvancement(
			ResearchAdvancement.RADIATION_STAFF_HEAT_1_ID,
			radiationStaff,
			ProjectGlitch.RADIATION_STAFF_ITEM.defaultStack,
		)

		consumer.createResearchAdvancement(
			ResearchAdvancement.RADIATION_STAFF_RAYS_1_ID,
			radiationStaff,
			ProjectGlitch.RADIATION_STAFF_ITEM.defaultStack,
		)

		consumer.createResearchAdvancement(
			ResearchAdvancement.RADIATION_STAFF_STRENGTH_1_ID,
			radiationStaff,
			ProjectGlitch.RADIATION_STAFF_ITEM.defaultStack,
		).also {
			consumer.createResearchAdvancement(
				ResearchAdvancement.RADIATION_STAFF_STRENGTH_2_ID,
				it,
				ProjectGlitch.RADIATION_STAFF_ITEM.defaultStack,
			)
		}

		// Targeted drug.
		val targetedDrug = consumer.createResearchAdvancement(
			ResearchAdvancement.TARGETED_DRUG_ID,
			researchRoot,
			ProjectGlitch.TARGETED_DRUG_INJECTOR_BLOCK_ITEM.defaultStack,
		) {
			rewards(AdvancementRewards.Builder.recipe(ProjectGlitch.TARGETED_DRUG_INJECTOR_ID))
		}

		consumer.createResearchAdvancement(
			ResearchAdvancement.TARGETED_DRUG_STRENGTH_1_ID,
			targetedDrug,
			ProjectGlitch.TARGETED_DRUG_INJECTOR_BLOCK_ITEM.defaultStack,
		).also {
			consumer.createResearchAdvancement(
				ResearchAdvancement.TARGETED_DRUG_STRENGTH_2_ID,
				it,
				ProjectGlitch.TARGETED_DRUG_INJECTOR_BLOCK_ITEM.defaultStack,
			)
		}

		consumer.createResearchAdvancement(
			ResearchAdvancement.CHEAPER_TARGETED_DRUG_ID,
			targetedDrug,
			ProjectGlitch.TARGETED_DRUG_INJECTOR_BLOCK_ITEM.defaultStack,
		) {
			rewards(AdvancementRewards.Builder.recipe(ResearchAdvancement.CHEAPER_TARGETED_DRUG_ID))
		}
	}

	private fun Advancement.Builder.build(consumer: Consumer<AdvancementEntry>, identifier: Identifier): AdvancementEntry {
		val advancement = build(identifier)
		consumer.accept(advancement)
		return advancement
	}

	private val Identifier.titleText: Text
		get() = Text.translatable("advancement.${namespace}.${path.replace('/', '.')}.title")

	private val Identifier.descriptionText: Text
		get() = Text.translatable("advancement.${namespace}.${path.replace('/', '.')}.description")
}
