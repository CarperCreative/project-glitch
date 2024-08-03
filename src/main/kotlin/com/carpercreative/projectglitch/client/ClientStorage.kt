package com.carpercreative.projectglitch.client

import com.carpercreative.projectglitch.networking.GlitchProgressPacket

object ClientStorage {
	/**
	 * The glitch progress of the current world, as a [0..1] value.
	 *
	 * Synchronized using [GlitchProgressPacket].
	 */
	var glitchProgress: Float = 0f
}
