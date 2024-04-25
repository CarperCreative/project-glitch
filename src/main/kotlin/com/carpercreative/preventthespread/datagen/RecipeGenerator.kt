package com.carpercreative.preventthespread.datagen

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.PreventTheSpread.identifier
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.data.server.recipe.RecipeExporter
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RawShapedRecipe
import net.minecraft.recipe.ShapedRecipe
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.registry.tag.ItemTags

class RecipeGenerator(
	output: FabricDataOutput,
) : FabricRecipeProvider(output) {
	override fun generate(exporter: RecipeExporter) {
		exporter.accept(
			identifier("chemotherapeutic_drug"),
			ShapedRecipe(
				"chemotherapeutic_drug",
				CraftingRecipeCategory.EQUIPMENT,
				RawShapedRecipe.create(
					mutableMapOf(
						'e' to Ingredient.ofItems(Items.EMERALD),
						'p' to Ingredient.fromTag(ItemTags.PLANKS),
						'r' to Ingredient.ofItems(Items.LIGHTNING_ROD),
					),
					" e ",
					"prp",
					"r r",
				),
				ItemStack(PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_BLOCK_ITEM, 8),
			),
			null,
		)

		exporter.accept(
			identifier("targeted_drug_injector"),
			ShapedRecipe(
				"targeted_drug_injector",
				CraftingRecipeCategory.EQUIPMENT,
				RawShapedRecipe.create(
					mutableMapOf(
						'c' to Ingredient.ofItems(Items.COPPER_INGOT),
						'r' to Ingredient.ofItems(Items.LIGHTNING_ROD),
						's' to Ingredient.ofItems(Items.SLIME_BALL),
						't' to Ingredient.ofItems(Items.IRON_TRAPDOOR),
					),
					" c ",
					"tst",
					" r ",
				),
				ItemStack(PreventTheSpread.TARGETED_DRUG_INJECTOR_BLOCK_ITEM, 8),
			),
			null,
		)

		exporter.accept(
			identifier("probe"),
			ShapedRecipe(
				"probe",
				CraftingRecipeCategory.EQUIPMENT,
				RawShapedRecipe.create(
					mutableMapOf(
						'k' to Ingredient.ofItems(Items.KELP),
						's' to Ingredient.ofItems(Items.STICK),
						'w' to Ingredient.ofItems(Items.STRING),
					),
					"  k",
					" w ",
					"s  ",
				),
				PreventTheSpread.PROBE_ITEM.defaultStack,
			),
			null,
		)

		exporter.accept(
			identifier("radiation_staff"),
			ShapedRecipe(
				"radiation_staff",
				CraftingRecipeCategory.EQUIPMENT,
				RawShapedRecipe.create(
					mutableMapOf(
						'a' to Ingredient.ofItems(Items.AMETHYST_SHARD),
						'e' to Ingredient.ofItems(Items.EMERALD),
						's' to Ingredient.ofItems(Items.STICK),
					),
					" es",
					"  a",
					" s ",
				),
				PreventTheSpread.RADIATION_STAFF_ITEM.defaultStack,
			),
			null,
		)

		exporter.accept(
			identifier("surgery_axe"),
			ShapedRecipe(
				"surgery_axe",
				CraftingRecipeCategory.EQUIPMENT,
				RawShapedRecipe.create(
					mutableMapOf(
						'e' to Ingredient.ofItems(Items.EMERALD),
						's' to Ingredient.ofItems(Items.STICK),
					),
					"ee",
					"se",
					"s ",
				),
				PreventTheSpread.SURGERY_AXE_ITEM.defaultStack,
			),
			null,
		)

		exporter.accept(
			identifier("surgery_hoe"),
			ShapedRecipe(
				"surgery_hoe",
				CraftingRecipeCategory.EQUIPMENT,
				RawShapedRecipe.create(
					mutableMapOf(
						'e' to Ingredient.ofItems(Items.EMERALD),
						's' to Ingredient.ofItems(Items.STICK),
					),
					"ee",
					"s ",
					"s ",
				),
				PreventTheSpread.SURGERY_SHOVEL_ITEM.defaultStack,
			),
			null,
		)

		exporter.accept(
			identifier("surgery_pickaxe"),
			ShapedRecipe(
				"surgery_pickaxe",
				CraftingRecipeCategory.EQUIPMENT,
				RawShapedRecipe.create(
					mutableMapOf(
						'e' to Ingredient.ofItems(Items.EMERALD),
						's' to Ingredient.ofItems(Items.STICK),
					),
					"eee",
					" s ",
					" s ",
				),
				PreventTheSpread.SURGERY_PICKAXE_ITEM.defaultStack,
			),
			null,
		)

		exporter.accept(
			identifier("surgery_shovel"),
			ShapedRecipe(
				"surgery_shovel",
				CraftingRecipeCategory.EQUIPMENT,
				RawShapedRecipe.create(
					mutableMapOf(
						'e' to Ingredient.ofItems(Items.EMERALD),
						's' to Ingredient.ofItems(Items.STICK),
					),
					"e",
					"s",
					"s",
				),
				PreventTheSpread.SURGERY_SHOVEL_ITEM.defaultStack,
			),
			null,
		)
	}
}
