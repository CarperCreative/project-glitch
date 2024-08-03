package com.carpercreative.projectglitch.datagen

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

object ProjectGlitchDataGenerator : DataGeneratorEntrypoint {
	override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
		// Data pack generator
		fabricDataGenerator.createPack().apply {
			addProvider(::AdvancementGenerator)
			addProvider(::BlockLootTableGenerator)
			addProvider(::BlockTagGenerator)
			addProvider(::ItemTagGenerator)
			addProvider(::RecipeGenerator)
		}
	}
}
