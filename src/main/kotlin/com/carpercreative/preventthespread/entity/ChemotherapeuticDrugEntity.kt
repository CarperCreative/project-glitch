package com.carpercreative.preventthespread.entity

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.block.ChemotherapeuticDrugBlock
import java.lang.reflect.Modifier
import kotlin.math.cos
import kotlin.math.sin
import net.minecraft.block.BlockState
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.TntEntity
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
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
	) : this(PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_ENTITY_TYPE, world, strength) {
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
			TNT_BLOCK_STATE,
			PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_BLOCK.defaultState
				.with(ChemotherapeuticDrugBlock.STRENGTH, strength)
		)
	}

	override fun getOwner(): LivingEntity? {
		return this.causingEntity
	}

	companion object {
		@Suppress("UNCHECKED_CAST")
		private val TNT_BLOCK_STATE: TrackedData<BlockState> = TntEntity::class.java.declaredFields
			.asSequence()
			.filter { Modifier.isStatic(it.modifiers) }
			.filter { it.type.isAssignableFrom(TrackedData::class.java) }
			.map {
				it.isAccessible = true
				it.get(null) as TrackedData<*>
			}
			.find { it.type == TrackedDataHandlerRegistry.BLOCK_STATE }!!
			as TrackedData<BlockState>

		fun spawn(world: ServerWorld, blockPos: BlockPos, igniter: LivingEntity?, strength: Int, modifyCallback: ((chemotherapeuticDrugEntity: ChemotherapeuticDrugEntity) -> Unit)? = null): ChemotherapeuticDrugEntity {
			val entity = ChemotherapeuticDrugEntity(world, blockPos.x + 0.5, blockPos.y.toDouble(), blockPos.z + 0.5, igniter, strength)
			modifyCallback?.invoke(entity)
			world.spawnEntity(entity)
			return entity
		}

		fun getExplosionPower(entity: ChemotherapeuticDrugEntity): Float {
			if (!entity.dataTracker.containsKey(TNT_BLOCK_STATE)) return 4f

			val strength = entity.dataTracker
				.get(TNT_BLOCK_STATE)
				.takeIf { it.isOf(PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_BLOCK) }
				?.get(ChemotherapeuticDrugBlock.STRENGTH)
				?: 0

			return (strength + 1) * 4f
		}
	}
}
