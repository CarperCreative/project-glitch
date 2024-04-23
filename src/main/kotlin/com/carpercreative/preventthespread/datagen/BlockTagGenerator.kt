package com.carpercreative.preventthespread.datagen

import com.carpercreative.preventthespread.PreventTheSpread
import java.util.concurrent.CompletableFuture
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.BlockTagProvider
import net.minecraft.registry.RegistryWrapper.WrapperLookup
import net.minecraft.registry.tag.BlockTags

class BlockTagGenerator(
	output: FabricDataOutput,
	completableFuture: CompletableFuture<WrapperLookup>,
) : BlockTagProvider(output, completableFuture) {
	override fun configure(arg: WrapperLookup) {
		getOrCreateTagBuilder(PreventTheSpread.CANCER_SPREADABLE_BLOCK_TAG)
			.forceAddTag(BlockTags.BASE_STONE_NETHER)
			.forceAddTag(BlockTags.BASE_STONE_OVERWORLD)
			.forceAddTag(BlockTags.DIRT)
			.forceAddTag(BlockTags.LOGS)
			.forceAddTag(BlockTags.SLABS)
			.forceAddTag(BlockTags.STAIRS)
			.forceAddTag(BlockTags.TERRACOTTA)
			.forceAddTag(BlockTags.WOOL)

		getOrCreateTagBuilder(PreventTheSpread.CANCEROUS_BLOCK_TAG)
			.addTag(PreventTheSpread.SURGERY_AXE_MINEABLE_BLOCK_TAG)
			.addTag(PreventTheSpread.SURGERY_HOE_MINEABLE_BLOCK_TAG)
			.addTag(PreventTheSpread.SURGERY_PICKAXE_MINEABLE_BLOCK_TAG)
			.addTag(PreventTheSpread.SURGERY_SHOVEL_MINEABLE_BLOCK_TAG)

		getOrCreateTagBuilder(PreventTheSpread.SURGERY_AXE_MINEABLE_BLOCK_TAG)
			.add(PreventTheSpread.CANCER_LOG_BLOCK)
			.add(PreventTheSpread.CANCER_PLANKS_BLOCK)
			.add(PreventTheSpread.CANCER_PLANKS_SLAB_BLOCK)
			.add(PreventTheSpread.CANCER_PLANKS_STAIRS_BLOCK)
		getOrCreateTagBuilder(PreventTheSpread.SURGERY_HOE_MINEABLE_BLOCK_TAG)
			.add(PreventTheSpread.CANCER_LEAVES_BLOCK)
		getOrCreateTagBuilder(PreventTheSpread.SURGERY_PICKAXE_MINEABLE_BLOCK_TAG)
			.add(PreventTheSpread.CANCER_STONE_BLOCK)
			.add(PreventTheSpread.CANCER_STONE_SLAB_BLOCK)
			.add(PreventTheSpread.CANCER_STONE_STAIRS_BLOCK)
		getOrCreateTagBuilder(PreventTheSpread.SURGERY_SHOVEL_MINEABLE_BLOCK_TAG)
			.add(PreventTheSpread.CANCER_DIRT_BLOCK)
			.add(PreventTheSpread.CANCER_DIRT_SLAB_BLOCK)
			.add(PreventTheSpread.CANCER_DIRT_STAIRS_BLOCK)
	}
}
