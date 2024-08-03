package com.carpercreative.projectglitch.cancer

import com.carpercreative.projectglitch.ProjectGlitch
import net.minecraft.text.Text

enum class TreatmentType(
	val displayName: Text,
) {
	SURGERY(
		Text.translatable("${ProjectGlitch.MOD_ID}.treatment_type.surgery"),
	),
	CHEMOTHERAPY(
		Text.translatable("${ProjectGlitch.MOD_ID}.treatment_type.chemotherapy"),
	),
	RADIATION_THERAPY(
		Text.translatable("${ProjectGlitch.MOD_ID}.treatment_type.radiation_therapy"),
	),
	TARGETED_DRUG(
		Text.translatable("${ProjectGlitch.MOD_ID}.treatment_type.targeted_drug"),
	),
}
