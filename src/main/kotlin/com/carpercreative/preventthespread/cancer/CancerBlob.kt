package com.carpercreative.preventthespread.cancer

import net.minecraft.nbt.NbtCompound

typealias BlobIdentifier = Int

class CancerBlob(
	val id: BlobIdentifier,
	val type: CancerType,
) {
	fun toNbt() = NbtCompound().apply {
		putInt(KEY_VERSION, 1)
		putInt(KEY_ID, id)
		putString(KEY_TYPE, type.toString())
	}

	companion object {
		private const val KEY_VERSION = "version"
		private const val KEY_ID = "id"
		private const val KEY_TYPE = "type"

		fun NbtCompound.toCancerBlob() = CancerBlob(
			getInt(KEY_ID),
			CancerType.valueOf(getString(KEY_TYPE)),
		)
	}
}
