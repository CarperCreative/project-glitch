package com.carpercreative.preventthespread.client.render.entity

import com.carpercreative.preventthespread.PreventTheSpread
import com.carpercreative.preventthespread.client.PreventTheSpreadClient
import com.carpercreative.preventthespread.client.render.entity.model.RobotEntityModel
import com.carpercreative.preventthespread.entity.RobotEntity
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.MobEntityRenderer
import net.minecraft.util.Identifier

@Environment(EnvType.CLIENT)
class RobotEntityRenderer(
	context: EntityRendererFactory.Context,
) : MobEntityRenderer<RobotEntity, RobotEntityModel>(
	context,
	RobotEntityModel(context.getPart(PreventTheSpreadClient.ROBOT_ENTITY_MODEL_LAYER)),
	0.7f,
) {
	override fun getTexture(entity: RobotEntity?): Identifier {
		return PreventTheSpread.identifier("textures/entity/robot/robot.png")
	}
}
