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
			.forceAddTag(BlockTags.TERRACOTTA)
			.forceAddTag(BlockTags.WOOL)

		getOrCreateTagBuilder(PreventTheSpread.CANCEROUS_BLOCK_TAG)
			.add(PreventTheSpread.CANCER_BLOCK)
	}
}
