package com.carpercreative.projectglitch.cancer

import com.carpercreative.projectglitch.ProjectGlitch
import net.minecraft.text.Text

enum class CancerType(
	val displayName: Text,
	@Deprecated("Use CancerBlob.treatments")
	val treatments: Array<TreatmentType>,

	/**
	 * Chance to spread when not surrounded by any other cancerous blocks.
	 */
	val baseSpreadChance: Float,

	/**
	 * Chance to spread when fully surrounded by other cancerous blocks.
	 */
	val surroundedSpreadChance: Float,
) {
	PRE_CANCEROUS(
		Text.translatable("${ProjectGlitch.MOD_ID}.cancer_type.pre_cancerous"),
		arrayOf(TreatmentType.SURGERY, TreatmentType.TARGETED_DRUG),
		baseSpreadChance = 0.35f,
		surroundedSpreadChance = 0.85f,
	),
	EARLY(
		Text.translatable("${ProjectGlitch.MOD_ID}.cancer_type.early"),
		arrayOf(TreatmentType.CHEMOTHERAPY, TreatmentType.RADIATION_THERAPY),
		baseSpreadChance = 0.5f,
		surroundedSpreadChance = 1f,
	),
	ADVANCED(
		Text.translatable("${ProjectGlitch.MOD_ID}.cancer_type.advanced"),
		arrayOf(TreatmentType.SURGERY, TreatmentType.CHEMOTHERAPY),
		baseSpreadChance = 0.65f,
		surroundedSpreadChance = 1f,
	),
	;
}
