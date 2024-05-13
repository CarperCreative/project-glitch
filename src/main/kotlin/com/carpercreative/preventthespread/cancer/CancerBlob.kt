package com.carpercreative.preventthespread.cancer

import net.minecraft.nbt.NbtCompound

typealias BlobIdentifier = Int

class CancerBlob(
	val id: BlobIdentifier,
	val type: CancerType,
	maxMetastaticJumpDistance: Int,
	cancerousBlockCount: Int = 0,
) {
	val maxMetastaticJumpDistance: Int = maxMetastaticJumpDistance.coerceAtLeast(0)

	val isMetastatic: Boolean
		get() = maxMetastaticJumpDistance > 0

	var cancerousBlockCount: Int = cancerousBlockCount
		set(value) {
			field = value.coerceAtLeast(0)
		}

	fun toNbt() = NbtCompound().apply {
		putInt(KEY_VERSION, 1)
		putInt(KEY_ID, id)
		putString(KEY_TYPE, this@CancerBlob.type.name)
		putInt(KEY_MAX_METASTATIC_JUMP_DISTANCE, maxMetastaticJumpDistance)
		putInt(KEY_CANCEROUS_BLOCK_COUNT, cancerousBlockCount)
	}

	companion object {
		private const val KEY_VERSION = "version"
		private const val KEY_ID = "id"
		private const val KEY_TYPE = "type"
		private const val KEY_MAX_METASTATIC_JUMP_DISTANCE = "maxMetastaticJumpDistance"
		private const val KEY_CANCEROUS_BLOCK_COUNT = "cancerousBlockCount"

		fun NbtCompound.toCancerBlob() = CancerBlob(
			getInt(KEY_ID),
			CancerType.valueOf(getString(KEY_TYPE)),
			getInt(KEY_MAX_METASTATIC_JUMP_DISTANCE),
			getInt(KEY_CANCEROUS_BLOCK_COUNT),
		)
	}
}
