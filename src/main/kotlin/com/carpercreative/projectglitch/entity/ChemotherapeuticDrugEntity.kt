package com.carpercreative.projectglitch.entity

import com.carpercreative.projectglitch.ProjectGlitch
import com.carpercreative.projectglitch.block.ChemotherapeuticDrugBlock
import kotlin.math.cos
import kotlin.math.sin
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.TntEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class ChemotherapeuticDrugEntity(
	entityType: EntityType<out ChemotherapeuticDrugEntity>,
	world: World,
	private val strength: Int,
) : TntEntity(entityType, world) {
	/**
	 * Same as [TntEntity.causingEntity], but reimplemented due to visibility constraints.
	 */
	private var causingEntity: LivingEntity? = null

	constructor(
		entityType: EntityType<out ChemotherapeuticDrugEntity>,
		world: World,
	) : this(
		entityType,
		world,
		0,
	)

	constructor(
		world: World,
		x: Double,
		y: Double,
		z: Double,
		igniter: LivingEntity?,
		strength: Int,
	) : this(ProjectGlitch.CHEMOTHERAPEUTIC_DRUG_ENTITY_TYPE, world, strength) {
		setPosition(x, y, z)
		val d = world.random.nextDouble() * Math.PI
		setVelocity(-sin(d) * 0.02, 0.2, -cos(d) * 0.02)
		fuse = 80
		prevX = x
		prevY = y
		prevZ = z
		causingEntity = igniter
	}

	init {
		// `TntEntity.initDataTracker` is called from the super constructor, meaning we can now set the data to the appropriate value.
		// We can't override the `initDataTracker` method ourselves because the class property is assigned in our constructor after the super constructor calls it.
		dataTracker.set(
			BLOCK_STATE,
			ProjectGlitch.CHEMOTHERAPEUTIC_DRUG_BLOCK.defaultState
				.with(ChemotherapeuticDrugBlock.STRENGTH, strength)
		)
	}

	override fun getOwner(): LivingEntity? {
		return this.causingEntity
	}

	companion object {
		fun spawn(world: ServerWorld, blockPos: BlockPos, igniter: LivingEntity?, strength: Int, modifyCallback: ((chemotherapeuticDrugEntity: ChemotherapeuticDrugEntity) -> Unit)? = null): ChemotherapeuticDrugEntity {
			val entity = ChemotherapeuticDrugEntity(world, blockPos.x + 0.5, blockPos.y.toDouble(), blockPos.z + 0.5, igniter, strength)
			modifyCallback?.invoke(entity)
			world.spawnEntity(entity)
			return entity
		}

		fun getExplosionPower(entity: ChemotherapeuticDrugEntity): Float {
			if (!entity.dataTracker.containsKey(BLOCK_STATE)) return 4f

			val strength = entity.dataTracker
				.get(BLOCK_STATE)
				.takeIf { it.isOf(ProjectGlitch.CHEMOTHERAPEUTIC_DRUG_BLOCK) }
				?.get(ChemotherapeuticDrugBlock.STRENGTH)
				?: 0

			return (strength + 1) * 4f
		}
	}
}
