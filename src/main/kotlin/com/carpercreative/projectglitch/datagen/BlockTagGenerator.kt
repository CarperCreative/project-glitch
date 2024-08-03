package com.carpercreative.projectglitch.datagen

import com.carpercreative.projectglitch.ProjectGlitch
import java.util.concurrent.CompletableFuture
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.BlockTagProvider
import net.minecraft.block.Blocks
import net.minecraft.registry.RegistryWrapper.WrapperLookup
import net.minecraft.registry.tag.BlockTags

class BlockTagGenerator(
	output: FabricDataOutput,
	completableFuture: CompletableFuture<WrapperLookup>,
) : BlockTagProvider(output, completableFuture) {
	override fun configure(arg: WrapperLookup) {
		getOrCreateTagBuilder(ProjectGlitch.GLITCHABLE_BLOCK_TAG)
			.add(Blocks.AIR)
			.forceAddTag(BlockTags.BASE_STONE_NETHER)
			.forceAddTag(BlockTags.BASE_STONE_OVERWORLD)
			.add(Blocks.CAVE_AIR)
			.forceAddTag(BlockTags.DIRT)
			.add(Blocks.GRAVEL)
			.forceAddTag(BlockTags.LEAVES)
			.forceAddTag(BlockTags.LOGS)
			.add(Blocks.RED_SAND)
			.add(Blocks.SAND)
			.addSandstoneBlocks()
			.forceAddTag(BlockTags.SLABS)
			.forceAddTag(BlockTags.STAIRS)
			.forceAddTag(BlockTags.TERRACOTTA)
			.forceAddTag(BlockTags.WOOL)

		getOrCreateTagBuilder(ProjectGlitch.GLITCHED_BLOCK_TAG)
			.addTag(ProjectGlitch.SURGERY_AXE_MINEABLE_BLOCK_TAG)
			.addTag(ProjectGlitch.SURGERY_HOE_MINEABLE_BLOCK_TAG)
			.addTag(ProjectGlitch.SURGERY_PICKAXE_MINEABLE_BLOCK_TAG)
			.addTag(ProjectGlitch.SURGERY_SHOVEL_MINEABLE_BLOCK_TAG)

		getOrCreateTagBuilder(ProjectGlitch.SURGERY_AXE_MINEABLE_BLOCK_TAG)
			.add(ProjectGlitch.GLITCH_LOG_BLOCK)
			.add(ProjectGlitch.GLITCH_PLANKS_BLOCK)
			.add(ProjectGlitch.GLITCH_PLANKS_SLAB_BLOCK)
			.add(ProjectGlitch.GLITCH_PLANKS_STAIRS_BLOCK)
		getOrCreateTagBuilder(ProjectGlitch.SURGERY_HOE_MINEABLE_BLOCK_TAG)
			.add(ProjectGlitch.GLITCH_LEAVES_BLOCK)
		getOrCreateTagBuilder(ProjectGlitch.SURGERY_PICKAXE_MINEABLE_BLOCK_TAG)
			.add(ProjectGlitch.GLITCH_STONE_BLOCK)
			.add(ProjectGlitch.GLITCH_STONE_SLAB_BLOCK)
			.add(ProjectGlitch.GLITCH_STONE_STAIRS_BLOCK)
		getOrCreateTagBuilder(ProjectGlitch.SURGERY_SHOVEL_MINEABLE_BLOCK_TAG)
			.add(ProjectGlitch.GLITCH_DIRT_BLOCK)
			.add(ProjectGlitch.GLITCH_DIRT_SLAB_BLOCK)
			.add(ProjectGlitch.GLITCH_DIRT_STAIRS_BLOCK)

		getOrCreateTagBuilder(ProjectGlitch.VALID_GLITCH_SEED_BLOCK_TAG)
			.forceAddTag(BlockTags.BASE_STONE_NETHER)
			.forceAddTag(BlockTags.BASE_STONE_OVERWORLD)
			.forceAddTag(BlockTags.DIRT)
			.add(Blocks.GRAVEL)
			.add(Blocks.RED_SAND)
			.add(Blocks.SAND)
			.addSandstoneBlocks()
			.forceAddTag(BlockTags.TERRACOTTA)

		getOrCreateTagBuilder(BlockTags.AXE_MINEABLE)
			.add(ProjectGlitch.PROCESSING_TABLE_BLOCK)
	}

	private fun FabricTagBuilder.addSandstoneBlocks(): FabricTagBuilder {
		add(Blocks.CHISELED_RED_SANDSTONE)
		add(Blocks.CHISELED_SANDSTONE)
		add(Blocks.CUT_SANDSTONE)
		add(Blocks.CUT_RED_SANDSTONE)
		add(Blocks.RED_SANDSTONE)
		add(Blocks.SANDSTONE)
		add(Blocks.SMOOTH_RED_SANDSTONE)
		add(Blocks.SMOOTH_SANDSTONE)
		return this
	}
}
