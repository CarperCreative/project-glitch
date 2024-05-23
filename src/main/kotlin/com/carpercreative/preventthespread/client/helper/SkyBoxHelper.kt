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

	val minimumSkyDarkness: Float
		get() = (ClientStorage.glitchProgress - 0.6f).coerceAtLeast(0f) * 2f

	val World.showGlitchiness: Boolean
		get() {
			val progress = ClientStorage.glitchProgress
			if (progress < 0.3f) return false

			// val x = time / 20f * progress
			// val noise = sin(x) * sin(x / 27 + sin(4 * x)) * max(sin(x / 19) - 0.6f, sin(0.7f + x / 4.6f))
			// val threshold = 0.3f - progress / 5

			val x = (time / 7L) * progress
			val noise = (x.toInt() * 777767777).and((1 shl 24) - 1) / (1 shl 24).toFloat()
			val threshold = 0.8f - ((progress - 0.3f) / 0.7f) * 0.6f

			return noise > threshold
		}
}
