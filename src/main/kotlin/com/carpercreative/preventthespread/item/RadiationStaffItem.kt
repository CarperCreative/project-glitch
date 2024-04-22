package com.carpercreative.preventthespread.item

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.cancer.CancerLogic.isCancerous
import java.util.function.Consumer
import net.fabricmc.fabric.api.item.v1.CustomDamageHandler
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.UseAction
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.world.World

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

	private fun doHit(world: ServerWorld, user: LivingEntity, stack: ItemStack) {
		// TODO: tickDelta should be set to prevent quick movements from being ignored
		val range = 10.0

		val blockHitResult = user.raycast(range, 1f, true)

		val cameraPosVector = user.getCameraPosVec(1f)
		val lookVector = user.getRotationVec(1f).multiply(range)
		val searchBBox = user.getBoundingBox().stretch(lookVector).expand(1.0, 1.0, 1.0)
		// Check if there's an entity closer to the player than the block.
		val entityHitResult = ProjectileUtil.raycast(
			user,
			cameraPosVector,
			lookVector.add(cameraPosVector),
			searchBBox,
			{ it is LivingEntity },
			if (blockHitResult.type != HitResult.Type.MISS) blockHitResult.pos.squaredDistanceTo(cameraPosVector) else range * range,
		)

		val hitResult = entityHitResult ?: blockHitResult

		when (hitResult) {
			is BlockHitResult -> {
				if (hitResult.type == HitResult.Type.MISS) return

				val targetBlockState = world.getBlockState(hitResult.blockPos)
				if (!targetBlockState.isCancerous()) return

				world.breakBlock(hitResult.blockPos, true, user)
			}
			is EntityHitResult -> {
				val entity = hitResult.entity
				if (entity !is LivingEntity) return

				val inFireDamageTypeEntry = world.registryManager.get(RegistryKeys.DAMAGE_TYPE).entryOf(DamageTypes.IN_FIRE)
				entity.damage(DamageSource(inFireDamageTypeEntry, user), 4f)
			}
			else -> {}
		}
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

		/**
		 * Removes damage from radiation staffs in player inventories while they're not in use.
		 */
		fun doCooldown(world: ServerWorld) {
			for (player in world.players) {
				val activeItem = player.activeItem

				for (stack in player.inventory.main) {
					doCooldown(stack, activeItem)
				}

				doCooldown(player.inventory.offHand[0], activeItem)
			}
		}

		private fun doCooldown(stack: ItemStack, skipStack: ItemStack?) {
			// Ignore radiation staffs without damage, and items which aren't the radiation staff.
			if (stack.damage <= 0 || stack.item !is RadiationStaffItem) return

			// Usually the player's active item, which could be the radiation staff currently in use.
			if (stack == skipStack) return

			stack.damage -= if (isOverheated(stack)) 1 else 2

			if (stack.damage <= 0) {
				setOverheated(stack, false)
			}
		}
	}

	object RadiationBeamGunDamageHandler : CustomDamageHandler {
		override fun damage(stack: ItemStack, amount: Int, entity: LivingEntity, breakCallback: Consumer<LivingEntity>): Int {
			// Do no damage to the item. The item never breaks, but the damage bar is used to indicate overheating.
			return 0
		}
	}
}
