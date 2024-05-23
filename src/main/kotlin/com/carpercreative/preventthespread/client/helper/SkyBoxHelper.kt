package com.carpercreative.preventthespread.client.helper

import com.carpercreative.preventthespread.client.ClientStorage
import net.minecraft.world.World

object SkyBoxHelper {
	val skyColorShift: Float
		// 0.0..0.6
		get() = (ClientStorage.glitchProgress - 0.4f).coerceAtLeast(0f)

	val cloudColorShift: Float
		// 0.0..0.4
		get() = (ClientStorage.glitchProgress - 0.5f).coerceAtLeast(0f) * 2f * 0.4f
}
