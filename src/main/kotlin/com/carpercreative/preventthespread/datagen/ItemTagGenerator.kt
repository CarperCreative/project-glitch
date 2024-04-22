package com.carpercreative.preventthespread.datagen

import com.carpercreative.preventthespread.PreventTheSpread
import java.util.concurrent.CompletableFuture
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.ItemTagProvider
import net.minecraft.registry.RegistryWrapper.WrapperLookup

class ItemTagGenerator(
	output: FabricDataOutput,
	completableFuture: CompletableFuture<WrapperLookup>,
) : ItemTagProvider(output, completableFuture) {
	override fun configure(arg: WrapperLookup) {
		getOrCreateTagBuilder(PreventTheSpread.SURGERY_TOOL_ITEM_TAG)
			.add(PreventTheSpread.SURGERY_AXE_ITEM)
			.add(PreventTheSpread.SURGERY_HOE_ITEM)
			.add(PreventTheSpread.SURGERY_PICKAXE_ITEM)
			.add(PreventTheSpread.SURGERY_SHOVEL_ITEM)
	}
}
