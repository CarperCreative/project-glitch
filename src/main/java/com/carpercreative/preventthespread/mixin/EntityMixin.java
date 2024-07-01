package com.carpercreative.preventthespread.mixin;

import com.carpercreative.preventthespread.entity.RobotEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
	@Inject(
		method = "pushAwayFrom(Lnet/minecraft/entity/Entity;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	public void pushAwayFrom$preventRobotPushing(Entity entity, CallbackInfo callback) {
		if (entity instanceof RobotEntity) {
			callback.cancel();
		}
	}
}
