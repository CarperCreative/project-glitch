package com.carpercreative.preventthespread.mixin.client;

import com.carpercreative.preventthespread.client.helper.SkyBoxHelper;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
	@Inject(
		method = "getCloudsColor(F)Lnet/minecraft/util/math/Vec3d;",
		at = @At("RETURN"),
		cancellable = true
	)
	public void getCloudsColor$glitchiness(float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
		float colorShift = SkyBoxHelper.INSTANCE.getCloudColorShift();
		if (colorShift > 0f) {
			Vec3d color = cir.getReturnValue();
			color = color.lerp(new Vec3d(1f, 0f, 0f), colorShift);
			cir.setReturnValue(color);
		}
	}

	@Inject(
		method = "getSkyColor(Lnet/minecraft/util/math/Vec3d;F)Lnet/minecraft/util/math/Vec3d;",
		at = @At("RETURN"),
		cancellable = true
	)
	public void getSkyColor$glitchiness(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
		float colorShift = SkyBoxHelper.INSTANCE.getSkyColorShift();
		if (colorShift > 0f) {
			Vec3d color = cir.getReturnValue();
			color = color.lerp(new Vec3d(1f, 0f, 0f), colorShift);
			cir.setReturnValue(color);
		}
	}
}
