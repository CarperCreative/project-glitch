package com.carpercreative.projectglitch.datagen

import com.carpercreative.projectglitch.ProjectGlitch
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
			ProjectGlitch.CHEMOTHERAPEUTIC_DRUG_ID,
			ShapedRecipe(
				ProjectGlitch.CHEMOTHERAPEUTIC_DRUG_ID.toString(),
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
				ItemStack(ProjectGlitch.CHEMOTHERAPEUTIC_DRUG_BLOCK_ITEM, 8),
			),
			null,
		)

		exporter.accept(
			ProjectGlitch.ResearchAdvancement.CHEAPER_CHEMOTHERAPEUTIC_DRUG_ID,
			ShapedRecipe(
				ProjectGlitch.CHEMOTHERAPEUTIC_DRUG_ID.toString(),
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
				ItemStack(ProjectGlitch.CHEMOTHERAPEUTIC_DRUG_BLOCK_ITEM, 16),
			),
			null,
		)

		exporter.accept(
			ProjectGlitch.CHILLING_TOWER_ID,
			ShapedRecipe(
				ProjectGlitch.CHILLING_TOWER_ID.toString(),
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
				ItemStack(ProjectGlitch.CHILLING_TOWER_BLOCK_ITEM, 2),
			),
			null,
		)

		exporter.accept(
			ProjectGlitch.TARGETED_DRUG_INJECTOR_ID,
			ShapedRecipe(
				ProjectGlitch.TARGETED_DRUG_INJECTOR_ID.toString(),
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
				ItemStack(ProjectGlitch.TARGETED_DRUG_INJECTOR_BLOCK_ITEM, 8),
			),
			null,
		)

		exporter.accept(
			ProjectGlitch.ResearchAdvancement.CHEAPER_TARGETED_DRUG_ID,
			ShapedRecipe(
				ProjectGlitch.TARGETED_DRUG_INJECTOR_ID.toString(),
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
				ItemStack(ProjectGlitch.TARGETED_DRUG_INJECTOR_BLOCK_ITEM, 16),
			),
			null,
		)

		exporter.accept(
			ProjectGlitch.PROBE_ITEM_ID,
			ShapedRecipe(
				ProjectGlitch.PROBE_ITEM_ID.toString(),
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
				ProjectGlitch.PROBE_ITEM.defaultStack,
			),
			null,
		)

		exporter.accept(
			ProjectGlitch.PROCESSING_TABLE_ID,
			ShapedRecipe(
				ProjectGlitch.PROCESSING_TABLE_ID.toString(),
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
				ProjectGlitch.PROCESSING_TABLE_BLOCK_ITEM.defaultStack,
			),
			null,
		)

		exporter.accept(
			ProjectGlitch.RADIATION_STAFF_ITEM_ID,
			ShapedRecipe(
				ProjectGlitch.RADIATION_STAFF_ITEM_ID.toString(),
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
				ProjectGlitch.RADIATION_STAFF_ITEM.defaultStack,
			),
			null,
		)

		exporter.accept(
			ProjectGlitch.SCANNER_ITEM_ID,
			ShapedRecipe(
				ProjectGlitch.SCANNER_ITEM_ID.toString(),
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
				ProjectGlitch.SCANNER_ITEM.defaultStack,
			),
			null,
		)

		exporter.accept(
			ProjectGlitch.SURGERY_AXE_ITEM_ID,
			ShapedRecipe(
				ProjectGlitch.SURGERY_AXE_ITEM_ID.toString(),
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
				ProjectGlitch.SURGERY_AXE_ITEM.defaultStack,
			),
			null,
		)

		exporter.accept(
			ProjectGlitch.SURGERY_HOE_ITEM_ID,
			ShapedRecipe(
				ProjectGlitch.SURGERY_HOE_ITEM_ID.toString(),
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
				ProjectGlitch.SURGERY_HOE_ITEM.defaultStack,
			),
			null,
		)

		exporter.accept(
			ProjectGlitch.SURGERY_PICKAXE_ITEM_ID,
			ShapedRecipe(
				ProjectGlitch.SURGERY_PICKAXE_ITEM_ID.toString(),
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
				ProjectGlitch.SURGERY_PICKAXE_ITEM.defaultStack,
			),
			null,
		)

		exporter.accept(
			ProjectGlitch.SURGERY_SHOVEL_ITEM_ID,
			ShapedRecipe(
				ProjectGlitch.SURGERY_SHOVEL_ITEM_ID.toString(),
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
				ProjectGlitch.SURGERY_SHOVEL_ITEM.defaultStack,
			),
			null,
		)
	}
}
