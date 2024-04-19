package com.carpercreative.preventthespread.cancer

enum class CancerType(
	val treatments: Array<TreatmentType>,
) {
	PRE_CANCEROUS(
		arrayOf(TreatmentType.SURGERY, TreatmentType.TARGETED_DRUG),
	),
	EARLY(
		arrayOf(TreatmentType.CHEMOTHERAPY, TreatmentType.RADIATION_THERAPY),
	),
	ADVANCED(
		arrayOf(TreatmentType.SURGERY, TreatmentType.CHEMOTHERAPY),
	),
	;

	fun isTreatmentValid(treatment: TreatmentType): Boolean {
		return treatments.contains(treatment)
	}
}
