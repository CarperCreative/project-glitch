package com.carpercreative.projectglitch.util

import com.carpercreative.projectglitch.ProjectGlitch
import net.minecraft.entity.player.PlayerEntity

fun PlayerEntity.getSurgeryEfficiencyLevel(): Int {
	if (hasAdvancement(ProjectGlitch.ResearchAdvancement.SURGERY_EFFICIENCY_2_ID)) return 2
	if (hasAdvancement(ProjectGlitch.ResearchAdvancement.SURGERY_EFFICIENCY_1_ID)) return 1
	return 0
}

fun PlayerEntity.getSurgeryEfficiencyEnchantmentLevel(): Int {
	return when (val level = getSurgeryEfficiencyLevel()) {
		0 -> 0
		else -> (level * 2) + 1
	}
}

fun PlayerEntity.getChemotherapeuticDrugStrength(): Int {
	if (hasAdvancement(ProjectGlitch.ResearchAdvancement.CHEMOTHERAPEUTIC_DRUG_STRENGTH_2_ID)) return 2
	if (hasAdvancement(ProjectGlitch.ResearchAdvancement.CHEMOTHERAPEUTIC_DRUG_STRENGTH_1_ID)) return 1
	return 0
}

fun PlayerEntity.getRadiationStaffHeat(): Int {
	if (hasAdvancement(ProjectGlitch.ResearchAdvancement.RADIATION_STAFF_HEAT_1_ID)) return 1
	return 0
}

fun PlayerEntity.getRadiationStaffStrength(): Int {
	if (hasAdvancement(ProjectGlitch.ResearchAdvancement.RADIATION_STAFF_STRENGTH_2_ID)) return 2
	if (hasAdvancement(ProjectGlitch.ResearchAdvancement.RADIATION_STAFF_STRENGTH_1_ID)) return 1
	return 0
}

fun PlayerEntity.getRadiationStaffSideRayCount(): Int {
	if (hasAdvancement(ProjectGlitch.ResearchAdvancement.RADIATION_STAFF_RAYS_1_ID)) return 1
	return 0
}

fun PlayerEntity.getTargetedDrugInjectorStrength(): Int {
	if (hasAdvancement(ProjectGlitch.ResearchAdvancement.TARGETED_DRUG_STRENGTH_2_ID)) return 2
	if (hasAdvancement(ProjectGlitch.ResearchAdvancement.TARGETED_DRUG_STRENGTH_1_ID)) return 1
	return 0
}
