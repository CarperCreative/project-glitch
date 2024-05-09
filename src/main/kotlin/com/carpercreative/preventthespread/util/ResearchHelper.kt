package com.carpercreative.preventthespread.util

import com.carpercreative.preventthespread.PreventTheSpread
import net.minecraft.entity.player.PlayerEntity

fun PlayerEntity.getSurgeryEfficiencyLevel(): Int {
	if (hasAdvancement(PreventTheSpread.ResearchAdvancement.SURGERY_EFFICIENCY_2_ID)) return 2
	if (hasAdvancement(PreventTheSpread.ResearchAdvancement.SURGERY_EFFICIENCY_1_ID)) return 1
	return 0
}

fun PlayerEntity.getSurgeryEfficiencyMultiplier(): Float {
	return when (val level = getSurgeryEfficiencyLevel()) {
		0 -> 0f
		else -> (level + 2).let { it * it + 1f }
	}
}
