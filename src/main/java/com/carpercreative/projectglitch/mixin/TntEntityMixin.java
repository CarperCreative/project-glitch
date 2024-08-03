package com.carpercreative.projectglitch.mixin;

import com.carpercreative.projectglitch.entity.ChemotherapeuticDrugEntity;
import net.minecraft.entity.TntEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(TntEntity.class)
public class TntEntityMixin {
	@ModifyConstant(
		method = "explode",
		constant = @Constant(floatValue = 4f)
	)
	public float explode$chemotherapeuticDrugStrength(float power) {
		if (((Object) this) instanceof ChemotherapeuticDrugEntity) {
			return ChemotherapeuticDrugEntity.Companion.getExplosionPower((ChemotherapeuticDrugEntity) (Object) this);
		}

		return power;
	}
}
