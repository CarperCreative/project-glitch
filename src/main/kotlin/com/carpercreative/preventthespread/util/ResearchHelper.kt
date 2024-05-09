package com.carpercreative.preventthespread.util

import com.carpercreative.preventthespread.PreventTheSpread
import net.minecraft.entity.player.PlayerEntity

fun PlayerEntity.getSurgeryEfficiencyLevel(): Int {
	if (hasAdvancement(PreventTheSpread.ResearchAdvancement.SURGERY_EFFICIENCY_2_ID)) return 2
	if (hasAdvancement(PreventTheSpread.ResearchAdvancement.SURGERY_EFFICIENCY_1_ID)) return 1
	return 0
}

fun PlayerEntity.getSurgeryEfficiencyEnchantmentLevel(): Int {
	return when (val level = getSurgeryEfficiencyLevel()) {
		0 -> 0
		else -> (level * 2) + 1
	}
}

fun PlayerEntity.getChemotherapeuticDrugStrength(): Int {
	if (hasAdvancement(PreventTheSpread.ResearchAdvancement.CHEMOTHERAPEUTIC_DRUG_STRENGTH_2_ID)) return 2
	if (hasAdvancement(PreventTheSpread.ResearchAdvancement.CHEMOTHERAPEUTIC_DRUG_STRENGTH_1_ID)) return 1
	return 0
}

fun PlayerEntity.getTargetedDrugInjectorStrength(): Int {
	if (hasAdvancement(PreventTheSpread.ResearchAdvancement.TARGETED_DRUG_STRENGTH_2_ID)) return 2
	if (hasAdvancement(PreventTheSpread.ResearchAdvancement.TARGETED_DRUG_STRENGTH_1_ID)) return 1
	return 0
}
