package com.carpercreative.preventthespread.datagen

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.block.TowerBlock
import java.util.function.BiConsumer
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider
import net.minecraft.item.BlockItem
import net.minecraft.loot.LootPool
import net.minecraft.loot.LootTable
import net.minecraft.loot.condition.BlockStatePropertyLootCondition
import net.minecraft.loot.condition.SurvivesExplosionLootCondition
import net.minecraft.loot.context.LootContextTypes
import net.minecraft.loot.entry.ItemEntry
import net.minecraft.loot.provider.number.ConstantLootNumberProvider
import net.minecraft.predicate.StatePredicate
import net.minecraft.state.property.Property
import net.minecraft.util.Identifier
import net.minecraft.util.StringIdentifiable

class BlockLootTableGenerator(
	output: FabricDataOutput,
) : SimpleFabricLootTableProvider(output, LootContextTypes.BLOCK) {
	override fun accept(exporter: BiConsumer<Identifier, LootTable.Builder>) {
		exporter.run {
			addBlockDrop(
				PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_ID,
				PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_BLOCK_ITEM,
			)
			addBlockDropWithPropertyCondition(
				PreventTheSpread.CHILLING_TOWER_ID,
				PreventTheSpread.CHILLING_TOWER_BLOCK_ITEM,
				TowerBlock.PART,
				TowerBlock.TowerPart.BOTTOM,
			)
			// Processing table item drops are handled in code.
			addBlockDrop(
				PreventTheSpread.TARGETED_DRUG_INJECTOR_ID,
				PreventTheSpread.TARGETED_DRUG_INJECTOR_BLOCK_ITEM,
			)
		}
	}

	private val Identifier.withBlocksPrefix: Identifier
		get() = withPrefixedPath("blocks/")

	private fun BiConsumer<Identifier, LootTable.Builder>.addBlockDrop(identifier: Identifier, item: BlockItem) {
		accept(
			identifier.withBlocksPrefix,
			LootTable.builder()
				.type(LootContextTypes.BLOCK)
				.randomSequenceId(identifier.withBlocksPrefix)
				.pool(
					LootPool.builder()
						.bonusRolls(ConstantLootNumberProvider.create(0f))
						.conditionally(SurvivesExplosionLootCondition.builder())
						.with(ItemEntry.builder(item))
						.build()
				)
		)
	}

	private fun <T> BiConsumer<Identifier, LootTable.Builder>.addBlockDropWithPropertyCondition(
		identifier: Identifier,
		item: BlockItem,
		property: Property<T>,
		propertyValue: T,
	) where T : Comparable<T>, T : StringIdentifiable {
		accept(
			identifier.withBlocksPrefix,
			LootTable.builder()
				.type(LootContextTypes.BLOCK)
				.randomSequenceId(identifier.withBlocksPrefix)
				.pool(
					LootPool.builder()
						.bonusRolls(ConstantLootNumberProvider.create(0f))
						.conditionally(SurvivesExplosionLootCondition.builder())
						.conditionally(
							BlockStatePropertyLootCondition.builder(item.block)
								.properties(
									StatePredicate.Builder.create()
										.exactMatch(property, propertyValue)
								)
								.build()
						)
						.with(ItemEntry.builder(item))
						.build()
				)
		)
	}
}
