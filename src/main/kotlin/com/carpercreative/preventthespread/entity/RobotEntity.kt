package com.carpercreative.preventthespread.entity

import com.carpercreative.preventthespread.PreventTheSpread
import com.google.common.collect.ImmutableList
import com.mojang.serialization.Dynamic
import net.minecraft.block.BlockState
import net.minecraft.entity.EntityType
import net.minecraft.entity.ai.brain.Brain
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.entity.ai.brain.sensor.SensorType
import net.minecraft.entity.ai.brain.task.RandomTask
import net.minecraft.entity.ai.control.FlightMoveControl
import net.minecraft.entity.ai.pathing.BirdNavigation
import net.minecraft.entity.ai.pathing.EntityNavigation
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class RobotEntity(
	entityType: EntityType<out RobotEntity>,
	world: World,
) : PathAwareEntity(entityType, world) {
	init {
		moveControl = FlightMoveControl(this, 10, true)
	}

	override fun initDataTracker() {
		super.initDataTracker()
	}

	override fun createNavigation(world: World?): EntityNavigation {
		val birdNavigation = BirdNavigation(this, world)
		birdNavigation.setCanPathThroughDoors(true)
		birdNavigation.setCanSwim(true)
		birdNavigation.setCanEnterOpenDoors(true)
		return birdNavigation
	}

	override fun createBrainProfile(): Brain.Profile<RobotEntity> {
		return Brain.createProfile(MEMORY_MODULES, SENSORS)
	}

	override fun deserializeBrain(dynamic: Dynamic<*>?): Brain<*> {
		return RobotBrain.create(createBrainProfile().deserialize(dynamic))
	}

	override fun getBrain(): Brain<RobotEntity> {
		@Suppress("UNCHECKED_CAST")
		return super.getBrain() as Brain<RobotEntity>
	}

	override fun mobTick() {
		world.profiler.push("${PreventTheSpread.MOD_ID}:robotBrain")
		getBrain().tick(world as ServerWorld, this)
		world.profiler.pop()
		world.profiler.push("${PreventTheSpread.MOD_ID}:robotActivityUpdate")
		RobotBrain.updateActivities(this)
		world.profiler.pop()
		super.mobTick()
	}

	override fun readCustomDataFromNbt(nbt: NbtCompound) {
	}

	override fun writeCustomDataToNbt(nbt: NbtCompound) {
	}

	override fun interactMob(player: PlayerEntity, hand: Hand): ActionResult {
		// TODO
		return super.interactMob(player, hand)
	}

	override fun fall(heightDifference: Double, onGround: Boolean, state: BlockState?, landedPosition: BlockPos?) {
		// Disable fall damage.
	}

	override fun getSwimHeight(): Double {
		return 0.0
	}

	public override fun getLeashOffset(): Vec3d {
		return Vec3d(0.42, 0.9, 0.4)
	}

	companion object {
		private val SENSORS = ImmutableList.of(
			// TODO
			SensorType.NEAREST_PLAYERS,
		)
		private val MEMORY_MODULES = ImmutableList.of(
			// TODO
			MemoryModuleType.PATH,
			MemoryModuleType.LOOK_TARGET,
			MemoryModuleType.VISIBLE_MOBS,
			MemoryModuleType.WALK_TARGET,
			MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
		)

		fun createRobotAttributes(): DefaultAttributeContainer.Builder {
			return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
				.add(EntityAttributes.GENERIC_FLYING_SPEED, 0.5)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0)
		}
	}
}
