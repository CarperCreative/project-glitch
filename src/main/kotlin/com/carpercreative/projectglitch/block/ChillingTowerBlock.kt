package com.carpercreative.projectglitch.block

import net.minecraft.block.BlockState
import net.minecraft.particle.DustColorTransitionParticleEffect
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.random.Random
import net.minecraft.world.World
import org.joml.Vector3f

class ChillingTowerBlock(
	settings: Settings,
) : TowerBlock(settings) {
	override fun spawnParticle(state: BlockState, world: World, particlePos: Vec3d, random: Random) {
		world.addParticle(
			DUST_PARTICLE,
			particlePos.x, particlePos.y, particlePos.z,
			0.0, -0.4, 0.0,
		)
	}

	companion object {
		private val DUST_PARTICLE = DustColorTransitionParticleEffect(Vector3f(0.4f, 0.4f, 1f), Vector3f(0.8f, 0.8f, 1f), 1f)
	}
}
