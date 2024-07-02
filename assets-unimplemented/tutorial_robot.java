// Made with Blockbench 4.10.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class tutorial_robot<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "tutorial_robot"), "main");
	private final ModelPart bone8;
	private final ModelPart bone7;
	private final ModelPart ye;
	private final ModelPart bone2;
	private final ModelPart bone;
	private final ModelPart bone4;
	private final ModelPart bone16;
	private final ModelPart bone17;
	private final ModelPart bone18;
	private final ModelPart bone9;

	public tutorial_robot(ModelPart root) {
		this.bone8 = root.getChild("bone8");
		this.bone7 = root.getChild("bone7");
		this.ye = root.getChild("ye");
		this.bone2 = root.getChild("bone2");
		this.bone = root.getChild("bone");
		this.bone4 = root.getChild("bone4");
		this.bone16 = root.getChild("bone16");
		this.bone17 = root.getChild("bone17");
		this.bone18 = root.getChild("bone18");
		this.bone9 = root.getChild("bone9");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone8 = partdefinition.addOrReplaceChild("bone8", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition bone7 = bone8.addOrReplaceChild("bone7", CubeListBuilder.create().texOffs(39, 46).addBox(-6.0F, 0.0F, -1.5F, 12.0F, 5.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -4.5F));

		PartDefinition ye = bone7.addOrReplaceChild("ye", CubeListBuilder.create().texOffs(70, 8).addBox(-7.0F, -4.85F, -2.6935F, 14.0F, 2.4F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(61, 64).addBox(-7.0F, 8.95F, -2.6935F, 14.0F, 2.4F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(52, 72).addBox(7.0F, -4.85F, -2.6935F, 2.0F, 16.2F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(37, 72).addBox(-9.0F, -4.85F, -2.6935F, 2.0F, 16.2F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(70, 16).addBox(-7.2F, -2.55F, -1.1935F, 14.4F, 11.5F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.95F, -2.3065F, -0.0524F, 0.0F, 0.0F));

		PartDefinition cube_r1 = ye.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(67, 78).addBox(-1.0F, -5.8F, -2.5F, 2.0F, 11.9F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.2254F, 3.15F, -0.0104F, 0.0F, 0.2967F, 0.0F));

		PartDefinition cube_r2 = ye.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(82, 78).addBox(-1.0F, -5.8F, -2.5F, 2.0F, 11.9F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.2254F, 3.15F, -0.0104F, 0.0F, -0.2967F, 0.0F));

		PartDefinition cube_r3 = ye.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(22, 64).addBox(-7.2F, -6.4317F, -3.8477F, 14.4F, 2.4F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.5305F, -0.1927F, -0.2967F, 0.0F, 0.0F));

		PartDefinition cube_r4 = ye.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(48, 0).addBox(-7.4F, -1.2F, -2.5F, 14.8F, 2.4F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2F, 9.3666F, 0.0481F, 0.2967F, 0.0F, 0.0F));

		PartDefinition cube_r5 = ye.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.2F, -1.675F, -0.15F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 6).mirror().addBox(-0.8F, -0.875F, 0.25F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(6.0F, -6.675F, -0.7435F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r6 = ye.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 0).addBox(-1.8F, -1.675F, -0.15F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 6).addBox(-1.2F, -0.875F, 0.25F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, -6.675F, -0.7435F, 0.0F, -0.7854F, 0.0F));

		PartDefinition bone2 = ye.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(76, 43).addBox(-7.2F, -1.9657F, -0.4101F, 14.4F, 4.8F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.15F, -1.3935F));

		PartDefinition bone = ye.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(68, 106).addBox(-7.2F, -2.75F, 0.0F, 14.4F, 5.5F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0147F, -1.4564F));

		PartDefinition bone4 = ye.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(8, 11).addBox(-0.8F, -0.35F, -0.55F, 1.6F, 0.7F, 1.1F, new CubeDeformation(0.0F)), PartPose.offset(4.1F, 8.2F, -0.9435F));

		PartDefinition bone16 = ye.addOrReplaceChild("bone16", CubeListBuilder.create().texOffs(9, 7).addBox(-0.1F, -0.15F, -0.55F, 0.2F, 0.3F, 1.1F, new CubeDeformation(0.0F)), PartPose.offset(6.5F, 3.3F, -0.9435F));

		PartDefinition bone17 = ye.addOrReplaceChild("bone17", CubeListBuilder.create().texOffs(7, 6).addBox(-0.1F, -0.15F, -0.55F, 0.2F, 0.3F, 1.1F, new CubeDeformation(0.0F)), PartPose.offset(6.8F, 4.3F, -0.9435F));

		PartDefinition bone18 = ye.addOrReplaceChild("bone18", CubeListBuilder.create().texOffs(0, 0).addBox(-0.1F, -0.15F, -0.55F, 0.2F, 0.3F, 1.1F, new CubeDeformation(0.0F)), PartPose.offset(6.5F, 4.3F, -0.9435F));

		PartDefinition bone9 = bone7.addOrReplaceChild("bone9", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, 0.55F, -2.9344F, 16.0F, 13.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -13.05F, 2.4344F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bone8.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}