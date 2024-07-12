package com.carpercreative.preventthespread.datagen

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.PreventTheSpread.ResearchAdvancement
import com.carpercreative.preventthespread.PreventTheSpread.StoryAdvancement
import com.carpercreative.preventthespread.blockEntity.ProcessingTableAnalyzerBlockEntity
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
				PreventTheSpread.GLITCH_STONE_BLOCK_ITEM.defaultStack,
				StoryAdvancement.ROOT_ID.titleText,
				StoryAdvancement.ROOT_ID.descriptionText,
				Identifier("textures/gui/advancements/backgrounds/adventure.png"),
				AdvancementFrame.TASK,
				true,
				false,
				false,
			)
			.withImpossibleCriterion()
			.rewards(AdvancementRewards.Builder.recipe(PreventTheSpread.PROBE_ITEM_ID))
			.build(consumer, StoryAdvancement.ROOT_ID)

		val obtainProbe = consumer.createAdvancement(
			StoryAdvancement.OBTAIN_PROBE_ID,
			storyRoot,
			PreventTheSpread.PROBE_ITEM.defaultStack,
		) {
			criterion("obtained_probe", InventoryChangedCriterion.Conditions.items(PreventTheSpread.PROBE_ITEM))

			rewards(AdvancementRewards.Builder.recipe(PreventTheSpread.SCANNER_ITEM_ID))
		}

		val obtainScanner = consumer.createAdvancement(
			StoryAdvancement.OBTAIN_SCANNER_ID,
			obtainProbe,
			PreventTheSpread.SCANNER_ITEM.defaultStack,
		) {
			criterion("obtained_scanner", InventoryChangedCriterion.Conditions.items(PreventTheSpread.SCANNER_ITEM))
		}

		val getSample = consumer.createAdvancement(
			StoryAdvancement.GET_SAMPLE_ID,
			obtainScanner,
			PreventTheSpread.GLITCH_STONE_BLOCK_ITEM.defaultStack,
		) {
			criterion("obtained_sample", ItemCriterion.Conditions.createItemUsedOnBlock(
				LocationPredicate.Builder.create()
					.block(BlockPredicate.Builder.create().tag(PreventTheSpread.GLITCHED_BLOCK_TAG)),
				ItemPredicate.Builder.create().items(PreventTheSpread.PROBE_ITEM),
			))

			rewards(AdvancementRewards.Builder.recipe(PreventTheSpread.PROCESSING_TABLE_ID))
		}

		val craftProcessingTable = consumer.createAdvancement(
			StoryAdvancement.CRAFT_PROCESSING_TABLE_ID,
			getSample,
			PreventTheSpread.PROCESSING_TABLE_BLOCK_ITEM.defaultStack,
		) {
			criterion("obtained_processing_table", InventoryChangedCriterion.Conditions.items(PreventTheSpread.PROCESSING_TABLE_BLOCK_ITEM))

			rewards(
				AdvancementRewards.Builder()
					.addRecipe(PreventTheSpread.SURGERY_AXE_ITEM_ID)
					.addRecipe(PreventTheSpread.SURGERY_HOE_ITEM_ID)
					.addRecipe(PreventTheSpread.SURGERY_PICKAXE_ITEM_ID)
					.addRecipe(PreventTheSpread.SURGERY_SHOVEL_ITEM_ID)
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
			PreventTheSpread.SURGERY_PICKAXE_ITEM.defaultStack,
		) {
			criterion("obtained_glitch_material", InventoryChangedCriterion.Conditions.items(PreventTheSpread.GLITCH_MATERIAL_ITEM))
		}

		val processGlitchMaterial = consumer.createAdvancement(
			StoryAdvancement.PROCESS_GLITCH_MATERIAL_ID,
			defeatBlob,
			PreventTheSpread.GLITCH_MATERIAL_ITEM.defaultStack,
		) {
			criterion("obtained_research", InventoryChangedCriterion.Conditions.items(PreventTheSpread.RESEARCH_ITEM))
		}

		val unlockTreatment = consumer.createAdvancement(
			StoryAdvancement.UNLOCK_TREATMENT_ID,
			processGlitchMaterial,
			PreventTheSpread.PROCESSING_TABLE_BLOCK_ITEM.defaultStack,
		) {
			criteriaMerger(AdvancementRequirements.CriterionMerger.OR)
			criterion("unlocked_${PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_ID.path}", Criteria.RECIPE_UNLOCKED.create(RecipeUnlockedCriterion.Conditions(Optional.empty(), PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_ID)))
			criterion("unlocked_${PreventTheSpread.RADIATION_STAFF_ITEM_ID.path}", Criteria.RECIPE_UNLOCKED.create(RecipeUnlockedCriterion.Conditions(Optional.empty(), PreventTheSpread.RADIATION_STAFF_ITEM_ID)))
			criterion("unlocked_${PreventTheSpread.TARGETED_DRUG_INJECTOR_ID.path}", Criteria.RECIPE_UNLOCKED.create(RecipeUnlockedCriterion.Conditions(Optional.empty(), PreventTheSpread.TARGETED_DRUG_INJECTOR_ID)))
		}

		consumer.createAdvancement(
			StoryAdvancement.GAME_OVER_ID,
			unlockTreatment,
			PreventTheSpread.GLITCH_STONE_BLOCK_ITEM.defaultStack,
			hidden = true,
		) {
			criterion("never", Criteria.IMPOSSIBLE.create(ImpossibleCriterion.Conditions()))
		}

		val researchRoot = Advancement.Builder.createUntelemetered()
			.display(
				PreventTheSpread.PROCESSING_TABLE_BLOCK_ITEM.defaultStack,
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
			PreventTheSpread.SURGERY_PICKAXE_ITEM.defaultStack,
		)

		consumer.createResearchAdvancement(
			ResearchAdvancement.SURGERY_EFFICIENCY_2_ID,
			surgeryEfficiency1,
			PreventTheSpread.SURGERY_PICKAXE_ITEM.defaultStack,
		)

		// Chemotherapeutic drug.
		val chemotherapeuticDrug = consumer.createResearchAdvancement(
			ResearchAdvancement.CHEMOTHERAPEUTIC_DRUG_ID,
			researchRoot,
			PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_BLOCK_ITEM.defaultStack,
		) {
			rewards(AdvancementRewards.Builder.recipe(PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_ID))
		}

		consumer.createResearchAdvancement(
			ResearchAdvancement.CHEAPER_CHEMOTHERAPEUTIC_DRUG_ID,
			chemotherapeuticDrug,
			PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_BLOCK_ITEM.defaultStack,
		) {
			rewards(AdvancementRewards.Builder.recipe(ResearchAdvancement.CHEAPER_CHEMOTHERAPEUTIC_DRUG_ID))
		}

		consumer.createResearchAdvancement(
			ResearchAdvancement.CHEMOTHERAPEUTIC_DRUG_STRENGTH_1_ID,
			chemotherapeuticDrug,
			PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_BLOCK_ITEM.defaultStack,
		).also {
			consumer.createResearchAdvancement(
				ResearchAdvancement.CHEMOTHERAPEUTIC_DRUG_STRENGTH_2_ID,
				it,
				PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_BLOCK_ITEM.defaultStack,
			)
		}

		// Chilling tower.
		consumer.createResearchAdvancement(
			ResearchAdvancement.CHILLING_TOWER_ID,
			researchRoot,
			PreventTheSpread.CHILLING_TOWER_BLOCK_ITEM.defaultStack,
		) {
			rewards(AdvancementRewards.Builder.recipe(PreventTheSpread.CHILLING_TOWER_ID))
		}

		// Radiation staff.
		val radiationStaff = consumer.createResearchAdvancement(
			ResearchAdvancement.RADIATION_STAFF_ID,
			researchRoot,
			PreventTheSpread.RADIATION_STAFF_ITEM.defaultStack,
		) {
			rewards(AdvancementRewards.Builder.recipe(PreventTheSpread.RADIATION_STAFF_ITEM_ID))
		}

		consumer.createResearchAdvancement(
			ResearchAdvancement.RADIATION_STAFF_HEAT_1_ID,
			radiationStaff,
			PreventTheSpread.RADIATION_STAFF_ITEM.defaultStack,
		)

		consumer.createResearchAdvancement(
			ResearchAdvancement.RADIATION_STAFF_RAYS_1_ID,
			radiationStaff,
			PreventTheSpread.RADIATION_STAFF_ITEM.defaultStack,
		)

		consumer.createResearchAdvancement(
			ResearchAdvancement.RADIATION_STAFF_STRENGTH_1_ID,
			radiationStaff,
			PreventTheSpread.RADIATION_STAFF_ITEM.defaultStack,
		).also {
			consumer.createResearchAdvancement(
				ResearchAdvancement.RADIATION_STAFF_STRENGTH_2_ID,
				it,
				PreventTheSpread.RADIATION_STAFF_ITEM.defaultStack,
			)
		}

		// Targeted drug.
		val targetedDrug = consumer.createResearchAdvancement(
			ResearchAdvancement.TARGETED_DRUG_ID,
			researchRoot,
			PreventTheSpread.TARGETED_DRUG_INJECTOR_BLOCK_ITEM.defaultStack,
		) {
			rewards(AdvancementRewards.Builder.recipe(PreventTheSpread.TARGETED_DRUG_INJECTOR_ID))
		}

		consumer.createResearchAdvancement(
			ResearchAdvancement.TARGETED_DRUG_STRENGTH_1_ID,
			targetedDrug,
			PreventTheSpread.TARGETED_DRUG_INJECTOR_BLOCK_ITEM.defaultStack,
		).also {
			consumer.createResearchAdvancement(
				ResearchAdvancement.TARGETED_DRUG_STRENGTH_2_ID,
				it,
				PreventTheSpread.TARGETED_DRUG_INJECTOR_BLOCK_ITEM.defaultStack,
			)
		}

		consumer.createResearchAdvancement(
			ResearchAdvancement.CHEAPER_TARGETED_DRUG_ID,
			targetedDrug,
			PreventTheSpread.TARGETED_DRUG_INJECTOR_BLOCK_ITEM.defaultStack,
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
