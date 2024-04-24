package com.carpercreative.preventthespread.entity

import com.carpercreative.preventthespread.PreventTheSpread
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
) : TntEntity(entityType, world) {
	/**
	 * Same as [TntEntity.causingEntity], but reimplemented due to visibility constraints.
	 */
	private var causingEntity: LivingEntity? = null

	constructor(
		world: World,
		x: Double,
		y: Double,
		z: Double,
		igniter: LivingEntity?,
	) : this(PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_ENTITY_TYPE, world) {
		setPosition(x, y, z)
		val d = world.random.nextDouble() * Math.PI
		setVelocity(-sin(d) * 0.02, 0.2, -cos(d) * 0.02)
		fuse = 80
		prevX = x
		prevY = y
		prevZ = z
		causingEntity = igniter
	}

	override fun initDataTracker() {
		super.initDataTracker()
		dataTracker.set(TNT_BLOCK_STATE, PreventTheSpread.CHEMOTHERAPEUTIC_DRUG_BLOCK.defaultState)
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

		fun spawn(world: ServerWorld, blockPos: BlockPos, igniter: LivingEntity?, modifyCallback: ((chemotherapeuticDrugEntity: ChemotherapeuticDrugEntity) -> Unit)? = null): ChemotherapeuticDrugEntity {
			val entity = ChemotherapeuticDrugEntity(world, blockPos.x + 0.5, blockPos.y.toDouble(), blockPos.z + 0.5, igniter)
			modifyCallback?.invoke(entity)
			world.spawnEntity(entity)
			return entity
		}
	}
}
