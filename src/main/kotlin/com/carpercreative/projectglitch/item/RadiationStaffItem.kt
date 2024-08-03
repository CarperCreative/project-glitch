package com.carpercreative.projectglitch.item

import com.carpercreative.projectglitch.ProjectGlitch
import com.carpercreative.projectglitch.cancer.CancerLogic
import com.carpercreative.projectglitch.cancer.CancerLogic.isGlitched
import com.carpercreative.projectglitch.cancer.TreatmentType
import com.carpercreative.projectglitch.persistence.CancerBlobPersistentState.Companion.getCancerBlobOrNull
import com.carpercreative.projectglitch.util.getRadiationStaffHeat
import com.carpercreative.projectglitch.util.getRadiationStaffSideRayCount
import com.carpercreative.projectglitch.util.getRadiationStaffStrength
import java.util.function.Consumer
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import net.fabricmc.fabric.api.item.v1.CustomDamageHandler
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.particle.DustColorTransitionParticleEffect
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.UseAction
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.RaycastContext
import net.minecraft.world.World
import org.joml.Vector3f

class RadiationStaffItem(
	settings: Settings,
) : Item(settings) {
	override fun getUseAction(stack: ItemStack?) = UseAction.BOW

	override fun getMaxUseTime(stack: ItemStack?) = 72000

	override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
		val stack = user.getStackInHand(hand)

		if (isOverheated(stack)) {
			// Gun is cooling down - do nothing until cooled off.
			// TODO: play a hissing sound or something

			return TypedActionResult.fail(stack)
		}

		val heatResearch = user.getRadiationStaffHeat()
		val triggerFrequency = getTriggerFrequency(heatResearch)

		// Prevent repeatedly shooting by spamming use right after the action point.
		// This does nothing in creative due to the game resetting the damage back to its pre-use value.
		stack.damage += (triggerFrequency - 1) - ((stack.damage + (triggerFrequency - 1)) % triggerFrequency)

		user.setCurrentHand(hand)
		return TypedActionResult.consume(stack)
	}

	override fun usageTick(world: World, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
		if (world.isClient) return
		world as ServerWorld

		val player = user as? PlayerEntity

		val oldDamage = stack.damage

		val heatResearch = player?.getRadiationStaffHeat() ?: 0

		val maxDamage = stack.maxDamage
		stack.damage = (stack.damage + getHeatingPerTick(heatResearch)).coerceAtMost(maxDamage)

		if (stack.damage >= maxDamage) {
			setOverheated(stack, true)

			// Interrupt the item usage.
			user.clearActiveItem()

			playSound(
				world,
				user,
				OVERHEATED_SOUND,
				1f,
			)
			return
		}

		val triggerFrequency = getTriggerFrequency(heatResearch)
		if (stack.damage / triggerFrequency != oldDamage / triggerFrequency) {
			doHit(world, user, stack)
		}
	}

	override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
		// Ignore radiation staffs without damage as those don't require cooling down.
		if (stack.damage <= 0) return

		val player = entity as? PlayerEntity

		// Don't do cooldown if the radiation staff is currently in use.
		if (stack == player?.activeItem) return

		val heatResearch = player?.getRadiationStaffHeat() ?: 0
		val coolingPerTick = getCoolingPerTick(heatResearch)

		stack.damage -= if (isOverheated(stack)) (coolingPerTick / 2) else coolingPerTick

		if (stack.damage <= 0) {
			setOverheated(stack, false)
		}
	}

	private fun doHit(world: ServerWorld, user: LivingEntity, stack: ItemStack) {
		val player = user as? PlayerEntity
		val strength = player?.getRadiationStaffStrength() ?: 0
		val sideRays = player?.getRadiationStaffSideRayCount() ?: 0

		sendRay(
			world,
			user,
			range = 10.0,
			penetrationDepth = 2 + strength * 2,
		)

		for (rayOffset in -sideRays..sideRays step 2) {
			sendRay(
				world,
				user,
				range = 10.0,
				penetrationDepth = 1 + (strength * 1.5f).toInt(),
				yRotationOffset = rayOffset * 15f,
			)
		}

		val heatResearch = player?.getRadiationStaffHeat() ?: 0
		val triggerFrequency = getTriggerFrequency(heatResearch)
		val lastShot = stack.damage + triggerFrequency >= stack.maxDamage
		playSound(
			world,
			user,
			RAY_SOUND,
			when (lastShot) {
				true -> 1.28f
				false -> 0.85f + (stack.damage / stack.maxDamage.toFloat()) * 0.3f
			},
		)
	}

	/**
	 * @return Depth of hit from the camera position, or `-1` if nothing was hit.
	 */
	private fun sendRay(world: ServerWorld, user: LivingEntity, range: Double, startRange: Double = 0.0, penetrationDepth: Int = 1, yRotationOffset: Float = 0f): Double {
		if (startRange >= range) return -1.0

		// TODO: tickDelta should be set to prevent quick movements from being ignored
		val tickDelta = 1f

		val cameraPosVector = user.getCameraPosVec(tickDelta)
		val lookVector = user.run { getRotationVector(getPitch(tickDelta), getYaw(tickDelta) + yRotationOffset) }

		val rayStart = lookVector.multiply(startRange).add(cameraPosVector)
		val rayEnd = lookVector.multiply(range).add(cameraPosVector)

		val blockHitResult = world.raycast(
			RaycastContext(
				rayStart,
				rayEnd,
				RaycastContext.ShapeType.OUTLINE,
				RaycastContext.FluidHandling.ANY,
				user,
			)
		)

		val searchBBox = user.boundingBox.stretch(lookVector).expand(1.0, 1.0, 1.0)
		// Check if there's an entity closer to the player than the block.
		val entityHitResult = ProjectileUtil.raycast(
			user,
			rayStart,
			rayEnd,
			searchBBox,
			{ it is LivingEntity },
			if (blockHitResult.type != HitResult.Type.MISS) blockHitResult.pos.squaredDistanceTo(rayStart) else (range - startRange).pow(2),
		)

		val hitResult = entityHitResult ?: blockHitResult

		// Spawn particles.
		val rayHitDistance = sqrt(hitResult.pos.squaredDistanceTo(rayStart))
		for (distanceStep in (startRange.coerceAtLeast(1.0) * 10).roundToInt() until (rayHitDistance * 10).roundToInt() step 5) {
			val distance = distanceStep / 10f
			val size = 0.5f * (distance / rayHitDistance.toFloat())
			world.spawnParticles(
				DustColorTransitionParticleEffect(Vector3f(100f, 0f, 1f), Vector3f(1f, 0f, 1f), size),
				rayStart.x + lookVector.x * distance,
				rayStart.y + lookVector.y * distance,
				rayStart.z + lookVector.z * distance,
				1,
				0.02, 0.02, 0.02,
				0.0,
			)
		}

		var fluidPenalty = 0.0

		when (hitResult) {
			is BlockHitResult -> {
				if (hitResult.type == HitResult.Type.MISS) return -1.0

				val targetPos = hitResult.blockPos
				val targetBlockState = world.getBlockState(targetPos)
				if (!targetBlockState.fluidState.isEmpty) {
					// We hit a liquid - reduce penetration depth without breaking anything, but continue.
					fluidPenalty = sqrt(3.0)
				} else if (targetBlockState.isGlitched()) {
					breakBlock(world, targetPos, user)
				} else {
					return -1.0
				}
			}
			is EntityHitResult -> {
				val entity = hitResult.entity
				if (entity !is LivingEntity) return -1.0

				val inFireDamageTypeEntry = world.registryManager.get(RegistryKeys.DAMAGE_TYPE).entryOf(DamageTypes.IN_FIRE)
				entity.damage(DamageSource(inFireDamageTypeEntry, user), 4f)
			}
			else -> {}
		}

		if (penetrationDepth > 1) {
			sendRay(world, user, range, startRange + rayHitDistance + fluidPenalty, penetrationDepth - 1, yRotationOffset)
		}

		return startRange + rayHitDistance
	}

	private fun breakBlock(world: ServerWorld, pos: BlockPos, user: LivingEntity) {
		world.getCancerBlobOrNull(pos)?.also { cancerBlob ->
			if (!cancerBlob.isTreatmentValid(TreatmentType.RADIATION_THERAPY)) {
				CancerLogic.hastenSpread(world, pos, world.random)
			}

			CancerLogic.checkMissingAnalysis(world, pos, user as? PlayerEntity, cancerBlob)
		}

		world.breakBlock(pos, true, user)

		// TODO: this might be skipping permission checks: look into a more appropriate way of simulating a block break as a player (then move hastenSpread to Block.onBreak logic)
		// (user as? ServerPlayerEntity)?.interactionManager?.tryBreakBlock(targetPos)
	}

	override fun getItemBarColor(stack: ItemStack): Int {
		if (isOverheated(stack)) return 0xff0000

		return super.getItemBarColor(stack)
	}

	override fun allowNbtUpdateAnimation(player: PlayerEntity, hand: Hand, oldStack: ItemStack, newStack: ItemStack): Boolean {
		return oldStack.item != newStack.item
	}

	companion object {
		/**
		 * How often the staff will trigger its action while being used, in ticks.
		 */
		private const val TRIGGER_FREQUENCY = 10

		fun getTriggerFrequency(heatResearch: Int): Int {
			return getHeatingPerTick(heatResearch) * TRIGGER_FREQUENCY
		}

		private const val KEY_OVERHEATED = "${ProjectGlitch.MOD_ID}:overheated"

		fun getHeatingPerTick(heatResearch: Int) = when (heatResearch) {
			1 -> 3
			else -> 5
		}

		fun getCoolingPerTick(heatResearch: Int) = when (heatResearch) {
			1 -> 6
			else -> 5
		}

		fun isOverheated(stack: ItemStack): Boolean {
			return stack.nbt?.contains(KEY_OVERHEATED) == true
		}

		fun setOverheated(stack: ItemStack, overheated: Boolean) {
			if (overheated) {
				stack.getOrCreateNbt().putBoolean(KEY_OVERHEATED, true)
			} else {
				stack.nbt?.remove(KEY_OVERHEATED)
			}
		}

		private fun playSound(world: World, user: LivingEntity, sound: SoundEvent, pitch: Float) {
			world.playSound(
				// Keep source null because this code never runs on the client.
				null,
				user.x,
				user.y + user.getEyeHeight(user.pose),
				user.z,
				sound,
				if (user is PlayerEntity) SoundCategory.PLAYERS else SoundCategory.HOSTILE,
				1f,
				pitch,
			)
		}

		private val OVERHEATED_SOUND = SoundEvent.of(ProjectGlitch.identifier("item.radiation_staff.overheated"), 24f)
		private val RAY_SOUND = SoundEvent.of(ProjectGlitch.identifier("item.radiation_staff.ray"), 32f)
	}

	object RadiationBeamGunDamageHandler : CustomDamageHandler {
		override fun damage(stack: ItemStack, amount: Int, entity: LivingEntity, breakCallback: Consumer<LivingEntity>): Int {
			// Do no damage to the item. The item never breaks, but the damage bar is used to indicate overheating.
			return 0
		}
	}
}
