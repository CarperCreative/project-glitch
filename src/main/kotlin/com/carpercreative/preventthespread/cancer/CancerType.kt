package com.carpercreative.preventthespread.cancer

import com.carpercreative.preventthespread.PreventTheSpread
import net.minecraft.text.Text

enum class CancerType(
	val displayName: Text,
	@Deprecated("Use CancerBlob.treatments")
	val treatments: Array<TreatmentType>,
) {
	PRE_CANCEROUS(
		Text.translatable("${PreventTheSpread.MOD_ID}.cancer_type.pre_cancerous"),
		arrayOf(TreatmentType.SURGERY, TreatmentType.TARGETED_DRUG),
	),
	EARLY(
		Text.translatable("${PreventTheSpread.MOD_ID}.cancer_type.early"),
		arrayOf(TreatmentType.CHEMOTHERAPY, TreatmentType.RADIATION_THERAPY),
	),
	ADVANCED(
		Text.translatable("${PreventTheSpread.MOD_ID}.cancer_type.advanced"),
		arrayOf(TreatmentType.SURGERY, TreatmentType.CHEMOTHERAPY),
	),
	;
}
