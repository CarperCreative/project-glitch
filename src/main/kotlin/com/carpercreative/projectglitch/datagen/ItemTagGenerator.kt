package com.carpercreative.projectglitch.datagen

import com.carpercreative.projectglitch.ProjectGlitch
import java.util.concurrent.CompletableFuture
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.ItemTagProvider
import net.minecraft.registry.RegistryWrapper.WrapperLookup

class ItemTagGenerator(
	output: FabricDataOutput,
	completableFuture: CompletableFuture<WrapperLookup>,
) : ItemTagProvider(output, completableFuture) {
	override fun configure(arg: WrapperLookup) {
		getOrCreateTagBuilder(ProjectGlitch.REQUIRES_RECIPE_TO_CRAFT_ITEM_TAG)
			.add(ProjectGlitch.CHEMOTHERAPEUTIC_DRUG_ID)
			.add(ProjectGlitch.CHILLING_TOWER_ID)
			.add(ProjectGlitch.RADIATION_STAFF_ITEM_ID)
			.add(ProjectGlitch.TARGETED_DRUG_INJECTOR_ID)

		getOrCreateTagBuilder(ProjectGlitch.SURGERY_TOOL_ITEM_TAG)
			.add(ProjectGlitch.SURGERY_AXE_ITEM)
			.add(ProjectGlitch.SURGERY_HOE_ITEM)
			.add(ProjectGlitch.SURGERY_PICKAXE_ITEM)
			.add(ProjectGlitch.SURGERY_SHOVEL_ITEM)
	}
}
