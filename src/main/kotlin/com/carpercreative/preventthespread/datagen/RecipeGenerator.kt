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
						'c' to Ingredient.ofItems(Items.COPPER_INGOT),
						'p' to Ingredient.fromTag(ItemTags.PLANKS),
						'r' to Ingredient.ofItems(Items.LIGHTNING_ROD),
					),
					" c ",
					"p p",
					"r r",
				),
				ItemStack(PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_BLOCK_ITEM, 8),
			),
			null,
		)

		exporter.accept(
			PreventTheSpread.ResearchAdvancement.CHEAPER_CHEMOTHERAPEUTIC_DRUG_ID,
			ShapedRecipe(
				PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_ID.toString(),
				CraftingRecipeCategory.EQUIPMENT,
				RawShapedRecipe.create(
					mutableMapOf(
						'c' to Ingredient.ofItems(Items.COPPER_INGOT),
						'p' to Ingredient.fromTag(ItemTags.PLANKS),
						'r' to Ingredient.ofItems(Items.LIGHTNING_ROD),
					),
					" c ",
					"prp",
					"r r",
				),
				ItemStack(PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_BLOCK_ITEM, 16),
			),
			null,
		)

		exporter.accept(
			PreventTheSpread.CHILLING_TOWER_ID,
			ShapedRecipe(
				PreventTheSpread.CHILLING_TOWER_ID.toString(),
				CraftingRecipeCategory.EQUIPMENT,
				RawShapedRecipe.create(
					mutableMapOf(
						'c' to Ingredient.ofItems(Items.COPPER_BLOCK),
						'r' to Ingredient.ofItems(Items.REDSTONE_TORCH),
						's' to Ingredient.ofItems(Items.SNOW_BLOCK),
					),
					"r",
					"s",
					"c",
				),
				ItemStack(PreventTheSpread.CHILLING_TOWER_BLOCK_ITEM, 2),
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
						'g' to Ingredient.ofItems(Items.GLOW_LICHEN),
						'p' to Ingredient.ofItems(Items.HEAVY_WEIGHTED_PRESSURE_PLATE),
						'r' to Ingredient.ofItems(Items.LIGHTNING_ROD),
					),
					" c ",
					"pgp",
					" r ",
				),
				ItemStack(PreventTheSpread.TARGETED_DRUG_INJECTOR_BLOCK_ITEM, 8),
			),
			null,
		)

		exporter.accept(
			PreventTheSpread.ResearchAdvancement.CHEAPER_TARGETED_DRUG_ID,
			ShapedRecipe(
				PreventTheSpread.TARGETED_DRUG_INJECTOR_ID.toString(),
				CraftingRecipeCategory.EQUIPMENT,
				RawShapedRecipe.create(
					mutableMapOf(
						'c' to Ingredient.ofItems(Items.COPPER_INGOT),
						'g' to Ingredient.ofItems(Items.GLOW_LICHEN),
						'r' to Ingredient.ofItems(Items.LIGHTNING_ROD),
						'p' to Ingredient.ofItems(Items.HEAVY_WEIGHTED_PRESSURE_PLATE),
					),
					"c c",
					"pgp",
					" r ",
				),
				ItemStack(PreventTheSpread.TARGETED_DRUG_INJECTOR_BLOCK_ITEM, 16),
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
			PreventTheSpread.PROCESSING_TABLE_ID,
			ShapedRecipe(
				PreventTheSpread.PROCESSING_TABLE_ID.toString(),
				CraftingRecipeCategory.EQUIPMENT,
				RawShapedRecipe.create(
					mutableMapOf(
						'b' to Ingredient.ofItems(Items.GLASS_BOTTLE),
						'f' to Ingredient.fromTag(ItemTags.WOODEN_FENCES),
						'g' to Ingredient.ofItems(Items.GLASS_PANE),
						'i' to Ingredient.ofItems(Items.IRON_INGOT),
						's' to Ingredient.fromTag(ItemTags.WOODEN_SLABS),
					),
					"gbi",
					"sss",
					"f f",
				),
				PreventTheSpread.PROCESSING_TABLE_BLOCK_ITEM.defaultStack,
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
						'd' to Ingredient.ofItems(Items.DIAMOND),
						'L' to Ingredient.ofItems(Items.LAPIS_BLOCK),
						's' to Ingredient.ofItems(Items.STICK),
					),
					"ds",
					" L",
					"s ",
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
						'c' to Ingredient.ofItems(Items.COPPER_INGOT),
						'g' to Ingredient.ofItems(Items.GLASS_PANE),
						'i' to Ingredient.ofItems(Items.IRON_INGOT),
						'p' to Ingredient.fromTag(ItemTags.PLANKS),
					),
					"pcp",
					"cgi",
					"pcp",
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
						'c' to Ingredient.ofItems(Items.COPPER_INGOT),
						's' to Ingredient.ofItems(Items.STICK),
					),
					"cc",
					"sc",
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
						'c' to Ingredient.ofItems(Items.COPPER_INGOT),
						's' to Ingredient.ofItems(Items.STICK),
					),
					"cc",
					"s ",
					"s ",
				),
				PreventTheSpread.SURGERY_HOE_ITEM.defaultStack,
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
						'c' to Ingredient.ofItems(Items.COPPER_INGOT),
						's' to Ingredient.ofItems(Items.STICK),
					),
					"ccc",
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
						'c' to Ingredient.ofItems(Items.COPPER_INGOT),
						's' to Ingredient.ofItems(Items.STICK),
					),
					"c",
					"s",
					"s",
				),
				PreventTheSpread.SURGERY_SHOVEL_ITEM.defaultStack,
			),
			null,
		)
	}
}
