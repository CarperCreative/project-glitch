package com.carpercreative.preventthespread.mixin;

import com.carpercreative.preventthespread.PreventTheSpread;
import com.carpercreative.preventthespread.cancer.CancerLogic;
import com.carpercreative.preventthespread.util.ResearchHelperKt;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
	@Inject(
		method = "getBlockBreakingSpeed(Lnet/minecraft/block/BlockState;)F",
		at = @At("RETURN"),
		cancellable = true
	)
	public void getBlockBreakingSpeed$surgeryEfficiencyResearch(BlockState block, CallbackInfoReturnable<Float> cir) {
		PlayerEntity that = (PlayerEntity) (Object) this;
		if (
			cir.getReturnValueF() >= 1f
			&& CancerLogic.INSTANCE.isCancerous(block)
			&& that.getMainHandStack().isIn(PreventTheSpread.INSTANCE.getSURGERY_TOOL_ITEM_TAG())
		) {
			float researchMultiplier = ResearchHelperKt.getSurgeryEfficiencyMultiplier(that);
			cir.setReturnValue(cir.getReturnValueF() + researchMultiplier);
		}
	}
}
