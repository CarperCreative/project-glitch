package com.carpercreative.preventthespread.datagen

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.block.ProcessingTableBlock
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
import net.minecraft.util.Identifier

class BlockLootTableGenerator(
	output: FabricDataOutput,
) : SimpleFabricLootTableProvider(output, LootContextTypes.BLOCK) {
	override fun accept(exporter: BiConsumer<Identifier, LootTable.Builder>) {
		exporter.run {
			addBlockDrop(
				PreventTheSpread.identifier("blocks/chemotherapeutic_drug"),
				PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_BLOCK_ITEM,
			)
			accept(
				PreventTheSpread.identifier("blocks/processing_table"),
				LootTable.builder()
					.type(LootContextTypes.BLOCK)
					.randomSequenceId(PreventTheSpread.identifier("blocks/processing_table"))
					.pool(
						LootPool.builder()
							.bonusRolls(ConstantLootNumberProvider.create(0f))
							.conditionally(SurvivesExplosionLootCondition.builder())
							.conditionally(
								BlockStatePropertyLootCondition.builder(PreventTheSpread.PROCESSING_TABLE_BLOCK)
									.properties(
										StatePredicate.Builder.create()
											.exactMatch(ProcessingTableBlock.PROCESSING_TABLE_PART, ProcessingTableBlock.ProcessingTablePart.LEFT)
									)
									.build()
							)
							.with(ItemEntry.builder(PreventTheSpread.PROCESSING_TABLE_BLOCK_ITEM))
							.build()
					)
			)
			addBlockDrop(
				PreventTheSpread.identifier("blocks/targeted_drug_injector"),
				PreventTheSpread.TARGETED_DRUG_INJECTOR_BLOCK_ITEM,
			)
		}
	}

	private fun BiConsumer<Identifier, LootTable.Builder>.addBlockDrop(identifier: Identifier, item: BlockItem) {
		accept(
			identifier,
			LootTable.builder()
				.type(LootContextTypes.BLOCK)
				.randomSequenceId(identifier)
				.pool(
					LootPool.builder()
						.bonusRolls(ConstantLootNumberProvider.create(0f))
						.conditionally(SurvivesExplosionLootCondition.builder())
						.with(ItemEntry.builder(item))
						.build()
				)
		)
	}
}
