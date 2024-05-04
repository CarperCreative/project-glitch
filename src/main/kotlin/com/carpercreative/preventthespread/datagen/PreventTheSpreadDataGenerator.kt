package com.carpercreative.preventthespread.datagen

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

object PreventTheSpreadDataGenerator : DataGeneratorEntrypoint {
	override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
		// Data pack generator
		fabricDataGenerator.createPack().apply {
			addProvider(::BlockLootTableGenerator)
			addProvider(::BlockTagGenerator)
			addProvider(::ItemTagGenerator)
			addProvider(::RecipeGenerator)
		}
	}
}
