package com.carpercreative.projectglitch.mixin;

import com.carpercreative.projectglitch.ProjectGlitch;
import com.carpercreative.projectglitch.util.ResearchHelperKt;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
	@Redirect(
		method = "getBlockBreakingSpeed(Lnet/minecraft/block/BlockState;)F",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getEfficiency(Lnet/minecraft/entity/LivingEntity;)I")
	)
	public int surgeryEfficiencyResearch$getBlockBreakingSpeed$getEfficiency(LivingEntity entity) {
		PlayerEntity that = (PlayerEntity) (Object) this;
		if (that.getMainHandStack().isIn(ProjectGlitch.INSTANCE.getSURGERY_TOOL_ITEM_TAG())) {
			return ResearchHelperKt.getSurgeryEfficiencyEnchantmentLevel(that);
		} else {
			return EnchantmentHelper.getEfficiency(entity);
		}
	}
}
