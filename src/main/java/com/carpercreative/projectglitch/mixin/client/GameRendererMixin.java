package com.carpercreative.projectglitch.mixin.client;

import com.carpercreative.projectglitch.client.helper.SkyBoxHelper;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(
		method = "tick()V",
		at = @At("TAIL")
	)
	public void tick$glitchiness(CallbackInfo ci) {
		GameRenderer that = (GameRenderer) (Object) this;
		that.skyDarkness = Math.max(that.skyDarkness, SkyBoxHelper.INSTANCE.getMinimumSkyDarkness());
	}
}
