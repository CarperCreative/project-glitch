package com.carpercreative.preventthespread

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object PreventTheSpread : ModInitializer {
	val MOD_ID = "preventthespread"

	private val logger = LoggerFactory.getLogger(MOD_ID)

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		logger.info("Hello Fabric world!")
	}
}
