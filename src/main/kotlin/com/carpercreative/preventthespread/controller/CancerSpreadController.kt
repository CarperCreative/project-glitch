package com.carpercreative.preventthespread.controller

import com.carpercreative.preventthespread.Storage
import com.carpercreative.preventthespread.cancer.CancerLogic
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.world.ServerWorld

object CancerSpreadController {
	fun init() {
		ServerTickEvents.END_WORLD_TICK.register { world ->
			tickWorld(world)
		}
	}

	private fun tickWorld(world: ServerWorld) {
		if (world.time % 20 != 12L) return

		// Cancer grows only in the main world.
		if (world != world.server.overworld) return

		tryCreateNewBlob(world)
	}

	private fun tryCreateNewBlob(world: ServerWorld) {
		val spreadDifficulty = Storage.spreadDifficulty

		if (Storage.cancerBlob.getActiveCancerBlobCount() >= spreadDifficulty.maxActiveBlobs) return

		// Create the cancer blob.
		CancerLogic.createCancerBlob(world)
	}
}
