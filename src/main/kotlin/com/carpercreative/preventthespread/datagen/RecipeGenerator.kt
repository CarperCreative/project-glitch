package com.carpercreative.preventthespread.datagen

import com.carpercreative.preventthespread.PreventTheSpread
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
			PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_ID,
			ShapedRecipe(
				PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_ID.toString(),
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
			PreventTheSpread.TARGETED_DRUG_INJECTOR_ID,
			ShapedRecipe(
				PreventTheSpread.TARGETED_DRUG_INJECTOR_ID.toString(),
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
			PreventTheSpread.PROBE_ITEM_ID,
			ShapedRecipe(
				PreventTheSpread.PROBE_ITEM_ID.toString(),
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
			PreventTheSpread.RADIATION_STAFF_ITEM_ID,
			ShapedRecipe(
				PreventTheSpread.RADIATION_STAFF_ITEM_ID.toString(),
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
			PreventTheSpread.SCANNER_ITEM_ID,
			ShapedRecipe(
				PreventTheSpread.SCANNER_ITEM_ID.toString(),
				CraftingRecipeCategory.EQUIPMENT,
				RawShapedRecipe.create(
					mutableMapOf(
						'e' to Ingredient.ofItems(Items.ENDER_PEARL),
						'i' to Ingredient.ofItems(Items.IRON_INGOT),
						'k' to Ingredient.ofItems(Items.DRIED_KELP),
					),
					"iki",
					"kek",
					"iki",
				),
				PreventTheSpread.SCANNER_ITEM.defaultStack,
			),
			null,
		)

		exporter.accept(
			PreventTheSpread.SURGERY_AXE_ITEM_ID,
			ShapedRecipe(
				PreventTheSpread.SURGERY_AXE_ITEM_ID.toString(),
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
			PreventTheSpread.SURGERY_HOE_ITEM_ID,
			ShapedRecipe(
				PreventTheSpread.SURGERY_HOE_ITEM_ID.toString(),
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
			PreventTheSpread.SURGERY_PICKAXE_ITEM_ID,
			ShapedRecipe(
				PreventTheSpread.SURGERY_PICKAXE_ITEM_ID.toString(),
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
			PreventTheSpread.SURGERY_SHOVEL_ITEM_ID,
			ShapedRecipe(
				PreventTheSpread.SURGERY_SHOVEL_ITEM_ID.toString(),
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
