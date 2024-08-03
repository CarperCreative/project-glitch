package com.carpercreative.projectglitch.mixin;

import com.carpercreative.projectglitch.controller.RobotController;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancementTracker.class)
public class PlayerAdvancementTrackerMixin {
	@Shadow
	private ServerPlayerEntity owner;

	@Inject(
		method = "Lnet/minecraft/advancement/PlayerAdvancementTracker;grantCriterion(Lnet/minecraft/advancement/AdvancementEntry;Ljava/lang/String;)Z",
		at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V")
	)
	public void grantCriterion$onAdvancementDone(AdvancementEntry advancementEntry, String criterion, CallbackInfoReturnable<Boolean> callback) {
		RobotController.INSTANCE.onAdvancementMade(owner, advancementEntry);
	}
}
