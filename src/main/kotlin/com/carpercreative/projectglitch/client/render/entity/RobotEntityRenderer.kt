package com.carpercreative.projectglitch.client.render.entity

import com.carpercreative.projectglitch.ProjectGlitch
import com.carpercreative.projectglitch.client.ProjectGlitchClient
import com.carpercreative.projectglitch.client.render.entity.model.RobotEntityModel
import com.carpercreative.projectglitch.entity.RobotEntity
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
	RobotEntityModel(context.getPart(ProjectGlitchClient.ROBOT_ENTITY_MODEL_LAYER)),
	0.7f,
) {
	override fun getTexture(entity: RobotEntity?): Identifier {
		return ProjectGlitch.identifier("textures/entity/robot/robot.png")
	}
}
