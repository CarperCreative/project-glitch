package com.carpercreative.preventthespread.cancer

import com.carpercreative.preventthespread.PreventTheSpread
import net.minecraft.text.Text

enum class TreatmentType(
	val displayName: Text,
) {
	SURGERY(
		Text.translatable("${PreventTheSpread.MOD_ID}.treatment_type.surgery"),
	),
	CHEMOTHERAPY(
		Text.translatable("${PreventTheSpread.MOD_ID}.treatment_type.chemotherapy"),
	),
	RADIATION_THERAPY(
		Text.translatable("${PreventTheSpread.MOD_ID}.treatment_type.radiation_therapy"),
	),
	TARGETED_DRUG(
		Text.translatable("${PreventTheSpread.MOD_ID}.treatment_type.targeted_drug"),
	),
}
