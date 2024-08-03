package com.carpercreative.projectglitch

import com.carpercreative.projectglitch.persistence.CancerBlobPersistentState
import com.carpercreative.projectglitch.persistence.CancerBlobPersistentState.Companion.getCancerBlobPersistentState
import com.carpercreative.projectglitch.persistence.SpreadDifficultyPersistentState
import com.carpercreative.projectglitch.persistence.SpreadDifficultyPersistentState.Companion.getSpreadDifficultyPersistentState
import com.carpercreative.projectglitch.team.TeamManager
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.PersistentState

/**
 * Holds references to [PersistentState] objects unique to a save, i.e. stored in the overworld dimension.
 *
 * Accessing any of these while the overworld isn't loaded will result in an [IllegalStateException].
 */
object Storage {
	private var cancerBlobPersistentState: CancerBlobPersistentState? = null

	private var spreadDifficultyPersistentState: SpreadDifficultyPersistentState? = null

	val cancerBlob: CancerBlobPersistentState
		get() = cancerBlobPersistentState ?: throw IllegalStateException("Tried to access ${CancerBlobPersistentState::class.java.name} without a loaded world.")

	val spreadDifficulty: SpreadDifficultyPersistentState
		get() = spreadDifficultyPersistentState ?: throw IllegalStateException("Tried to access ${SpreadDifficultyPersistentState::class.java.name} without a loaded world.")

	val teamManager = TeamManager()

	fun init() {
		ServerWorldEvents.LOAD.register(::onWorldLoad)
		ServerWorldEvents.UNLOAD.register(::onWorldUnload)
	}

	private fun onWorldLoad(server: MinecraftServer, world: ServerWorld) {
		if (server.overworld == world) {
			cancerBlobPersistentState = server.getCancerBlobPersistentState()
			spreadDifficultyPersistentState = server.getSpreadDifficultyPersistentState()
		}
	}

	private fun onWorldUnload(server: MinecraftServer, world: ServerWorld) {
		if (server.overworld == world) {
			cancerBlobPersistentState = null
			spreadDifficultyPersistentState = null

			teamManager.clear()
		}
	}
}
