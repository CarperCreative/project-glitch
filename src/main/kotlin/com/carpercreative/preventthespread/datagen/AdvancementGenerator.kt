package com.carpercreative.preventthespread.datagen

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.PreventTheSpread.ResearchAdvancement
import com.carpercreative.preventthespread.PreventTheSpread.StoryAdvancement
import java.util.function.Consumer
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider
import net.minecraft.advancement.Advancement
import net.minecraft.advancement.AdvancementEntry
import net.minecraft.advancement.AdvancementFrame
import net.minecraft.advancement.AdvancementRewards
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.advancement.criterion.ImpossibleCriterion
import net.minecraft.advancement.criterion.InventoryChangedCriterion
import net.minecraft.advancement.criterion.ItemCriterion
import net.minecraft.item.ItemStack
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
				false,
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
				PreventTheSpread.CANCER_STONE_BLOCK_ITEM.defaultStack,
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

		val getSample = consumer.createAdvancement(
			StoryAdvancement.GET_SAMPLE_ID,
			obtainProbe,
			PreventTheSpread.CANCER_STONE_BLOCK_ITEM.defaultStack,
		) {
			criterion("obtained_sample", ItemCriterion.Conditions.createItemUsedOnBlock(
				LocationPredicate.Builder.create()
					.block(BlockPredicate.Builder.create().tag(PreventTheSpread.CANCEROUS_BLOCK_TAG)),
				ItemPredicate.Builder.create().items(PreventTheSpread.PROBE_ITEM),
			))

			rewards(
				AdvancementRewards.Builder()
					.addRecipe(PreventTheSpread.PROCESSING_TABLE_ID)
					.addRecipe(PreventTheSpread.SURGERY_AXE_ITEM_ID)
					.addRecipe(PreventTheSpread.SURGERY_HOE_ITEM_ID)
					.addRecipe(PreventTheSpread.SURGERY_PICKAXE_ITEM_ID)
					.addRecipe(PreventTheSpread.SURGERY_SHOVEL_ITEM_ID)
			)
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
