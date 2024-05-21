package com.carpercreative.preventthespread.item

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.cancer.CancerLogic
import com.carpercreative.preventthespread.cancer.CancerLogic.isGlitched
import com.carpercreative.preventthespread.cancer.TreatmentType
import com.carpercreative.preventthespread.persistence.CancerBlobPersistentState.Companion.getCancerBlobOrNull
import com.carpercreative.preventthespread.util.getRadiationStaffSideRayCount
import com.carpercreative.preventthespread.util.getRadiationStaffStrength
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

		// Prevent repeatedly shooting by spamming use right after the action point.
		// This does nothing in creative due to the game resetting the damage back to its pre-use value.
		stack.damage += (TRIGGER_FREQUENCY - 1) - ((stack.damage + (TRIGGER_FREQUENCY - 1)) % TRIGGER_FREQUENCY)

		user.setCurrentHand(hand)
		return TypedActionResult.consume(stack)
	}

	override fun usageTick(world: World, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
		if (world.isClient) return
		world as ServerWorld

		val maxDamage = stack.maxDamage
		stack.damage = (stack.damage + 1).coerceAtMost(maxDamage)

		if (stack.damage >= maxDamage) {
			setOverheated(stack, true)

			// Interrupt the item usage.
			user.clearActiveItem()
			return
		}

		if (stack.damage % TRIGGER_FREQUENCY == 0) {
			doHit(world, user, stack)
		}
	}

	override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
		// Ignore radiation staffs without damage as those don't require cooling down.
		if (stack.damage <= 0) return

		// Don't do cooldown if the radiation staff is currently in use.
		if (stack == (entity as? PlayerEntity)?.activeItem) return

		stack.damage -= if (isOverheated(stack)) 1 else 2

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
			if (!cancerBlob.type.isTreatmentValid(TreatmentType.RADIATION_THERAPY)) {
				CancerLogic.hastenSpread(world, pos, world.random)
			}
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

		private const val KEY_OVERHEATED = "${PreventTheSpread.MOD_ID}:overheated"

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

		fun getAffectedBlockCount(strength: Int): Int {
			return 1 + strength * 4
		}
	}

	object RadiationBeamGunDamageHandler : CustomDamageHandler {
		override fun damage(stack: ItemStack, amount: Int, entity: LivingEntity, breakCallback: Consumer<LivingEntity>): Int {
			// Do no damage to the item. The item never breaks, but the damage bar is used to indicate overheating.
			return 0
		}
	}
}
