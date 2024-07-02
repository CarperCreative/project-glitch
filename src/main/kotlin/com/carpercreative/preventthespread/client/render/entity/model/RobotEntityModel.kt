package com.carpercreative.preventthespread.client.render.entity.model

import com.carpercreative.preventthespread.entity.RobotEntity
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.model.Dilation
import net.minecraft.client.model.ModelData
import net.minecraft.client.model.ModelPart
import net.minecraft.client.model.ModelPartBuilder
import net.minecraft.client.model.ModelPartData
import net.minecraft.client.model.ModelTransform
import net.minecraft.client.model.TexturedModelData
import net.minecraft.client.render.entity.model.EntityModelPartNames
import net.minecraft.client.render.entity.model.SinglePartEntityModel

@Environment(EnvType.CLIENT)
class RobotEntityModel(
	modelPart: ModelPart,
) : SinglePartEntityModel<RobotEntity>() {
	private val basePart = modelPart.getChild(EntityModelPartNames.BODY)

	override fun setAngles(entity: RobotEntity, limbAngle: Float, limbDistance: Float, animationProgress: Float, headYaw: Float, headPitch: Float) {
		// TODO
	}

	override fun getPart(): ModelPart {
		return basePart
	}

	companion object {
		fun getTexturedModelData(): TexturedModelData {
			// I don't care how messy this code is.
			val modelData = ModelData()
			val rootPart: ModelPartData = modelData.root

			val bone8: ModelPartData = rootPart.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create(), ModelTransform.pivot(0.0f, 24.0f, 0.0f))

			val bone7: ModelPartData = bone8.addChild("bone7", ModelPartBuilder.create().uv(39, 46).cuboid(-6.0f, 0.0f, -1.5f, 12.0f, 5.0f, 12.0f, Dilation(0.0f)), ModelTransform.pivot(0.0f, 0.0f, -4.5f))

			val ye: ModelPartData = bone7.addChild("ye", ModelPartBuilder.create().uv(70, 8).cuboid(-7.0f, -4.85f, -2.6935f, 14.0f, 2.4f, 5.0f, Dilation(0.0f))
				.uv(61, 64).cuboid(-7.0f, 8.95f, -2.6935f, 14.0f, 2.4f, 5.0f, Dilation(0.0f))
				.uv(52, 72).cuboid(7.0f, -4.85f, -2.6935f, 2.0f, 16.2f, 5.0f, Dilation(0.0f))
				.uv(37, 72).cuboid(-9.0f, -4.85f, -2.6935f, 2.0f, 16.2f, 5.0f, Dilation(0.0f))
				.uv(70, 16).cuboid(-7.2f, -2.55f, -1.1935f, 14.4f, 11.5f, 0.0f, Dilation(0.0f)), ModelTransform.of(0.0f, -8.95f, -2.3065f, -0.0524f, 0.0f, 0.0f))

			val cube_r1: ModelPartData = ye.addChild("cube_r1", ModelPartBuilder.create().uv(67, 78).cuboid(-1.0f, -5.8f, -2.5f, 2.0f, 11.9f, 5.0f, Dilation(0.0f)), ModelTransform.of(-7.2254f, 3.15f, -0.0104f, 0.0f, 0.2967f, 0.0f))

			val cube_r2: ModelPartData = ye.addChild("cube_r2", ModelPartBuilder.create().uv(82, 78).cuboid(-1.0f, -5.8f, -2.5f, 2.0f, 11.9f, 5.0f, Dilation(0.0f)), ModelTransform.of(7.2254f, 3.15f, -0.0104f, 0.0f, -0.2967f, 0.0f))

			val cube_r3: ModelPartData = ye.addChild("cube_r3", ModelPartBuilder.create().uv(22, 64).cuboid(-7.2f, -6.4317f, -3.8477f, 14.4f, 2.4f, 5.0f, Dilation(0.0f)), ModelTransform.of(0.0f, 2.5305f, -0.1927f, -0.2967f, 0.0f, 0.0f))

			val cube_r4: ModelPartData = ye.addChild("cube_r4", ModelPartBuilder.create().uv(48, 0).cuboid(-7.4f, -1.2f, -2.5f, 14.8f, 2.4f, 5.0f, Dilation(0.0f)), ModelTransform.of(0.2f, 9.3666f, 0.0481f, 0.2967f, 0.0f, 0.0f))

			val cube_r5: ModelPartData = ye.addChild("cube_r5", ModelPartBuilder.create().uv(0, 0).mirrored().cuboid(-1.2f, -1.675f, -0.15f, 3.0f, 2.0f, 3.0f, Dilation(0.0f)).mirrored(false)
				.uv(0, 6).mirrored().cuboid(-0.8f, -0.875f, 0.25f, 2.0f, 3.0f, 2.0f, Dilation(0.0f)).mirrored(false), ModelTransform.of(6.0f, -6.675f, -0.7435f, 0.0f, 0.7854f, 0.0f))

			val cube_r6: ModelPartData = ye.addChild("cube_r6", ModelPartBuilder.create().uv(0, 0).cuboid(-1.8f, -1.675f, -0.15f, 3.0f, 2.0f, 3.0f, Dilation(0.0f))
				.uv(0, 6).cuboid(-1.2f, -0.875f, 0.25f, 2.0f, 3.0f, 2.0f, Dilation(0.0f)), ModelTransform.of(-6.0f, -6.675f, -0.7435f, 0.0f, -0.7854f, 0.0f))

			val bone2: ModelPartData = ye.addChild("bone2", ModelPartBuilder.create().uv(76, 43).cuboid(-7.2f, -1.9657f, -0.4101f, 14.4f, 4.8f, 0.0f, Dilation(0.0f)), ModelTransform.pivot(0.0f, -0.15f, -1.3935f))

			// Mouth
			val bone: ModelPartData = ye.addChild("bone", ModelPartBuilder.create().uv(67, 72).cuboid(-7.2f, -2.75f, 0.0f, 14.4f, 5.5f, 0.0f, Dilation(0.0f)), ModelTransform.pivot(0.0f, 5.0147f, -1.4564f))

			val bone4: ModelPartData = ye.addChild("bone4", ModelPartBuilder.create().uv(8, 11).cuboid(-0.8f, -0.35f, -0.55f, 1.6f, 0.7f, 1.1f, Dilation(0.0f)), ModelTransform.pivot(4.1f, 8.2f, -0.9435f))

			val bone16: ModelPartData = ye.addChild("bone16", ModelPartBuilder.create().uv(9, 7).cuboid(-0.1f, -0.15f, -0.55f, 0.2f, 0.3f, 1.1f, Dilation(0.0f)), ModelTransform.pivot(6.5f, 3.3f, -0.9435f))

			val bone17: ModelPartData = ye.addChild("bone17", ModelPartBuilder.create().uv(7, 6).cuboid(-0.1f, -0.15f, -0.55f, 0.2f, 0.3f, 1.1f, Dilation(0.0f)), ModelTransform.pivot(6.8f, 4.3f, -0.9435f))

			val bone18: ModelPartData = ye.addChild("bone18", ModelPartBuilder.create().uv(0, 0).cuboid(-0.1f, -0.15f, -0.55f, 0.2f, 0.3f, 1.1f, Dilation(0.0f)), ModelTransform.pivot(6.5f, 4.3f, -0.9435f))

			// Body
			val bone9: ModelPartData = bone7.addChild("bone9", ModelPartBuilder.create().uv(0, 0).cuboid(-8.0f, 0.55f, -2.9344f, 16.0f, 13.0f, 15.0f, Dilation(0.0f)), ModelTransform.pivot(0.0f, -13.05f, 2.4344f))

			return TexturedModelData.of(modelData, 128, 128)
		}
	}
}
