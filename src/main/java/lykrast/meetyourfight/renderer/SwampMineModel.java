package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.SwampMineEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

// Made with Blockbench 3.7.4
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
public class SwampMineModel extends EntityModel<SwampMineEntity> {
	public static final ModelLayerLocation MODEL = new ModelLayerLocation(MeetYourFight.rl("swamp_mine"), "main");
	private final ModelPart spikes;
	private final ModelPart bb_main;

	public SwampMineModel(ModelPart modelPart) {
		spikes = modelPart.getChild("spikes");
		bb_main = modelPart.getChild("main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(-8, -16, -8, 16, 16, 16), PartPose.offset(0, 24, 0));
		PartDefinition spikes = partdefinition.addOrReplaceChild("spikes", CubeListBuilder.create(), PartPose.offset(-6, 10, 6));
		spikes.addOrReplaceChild("bottombackright", CubeListBuilder.create().texOffs(0, 0).addBox(-1, -7, -1, 2, 7, 2), PartPose.offsetAndRotation(0, 12, 0, -2.3562F, -0.7854F, 0.0F));
		spikes.addOrReplaceChild("bottombackleft", CubeListBuilder.create().texOffs(0, 0).addBox(-1, -7, -1, 2, 7, 2), PartPose.offsetAndRotation(12, 12, 0, -2.3562F, 0.7854F, 0.0F));
		spikes.addOrReplaceChild("bottomfrontright", CubeListBuilder.create().texOffs(0, 0).addBox(-1, -7, -1, 2, 7, 2), PartPose.offsetAndRotation(0, 12, -12, 2.3562F, 0.7854F, 0.0F));
		spikes.addOrReplaceChild("bottomfrontleft", CubeListBuilder.create().texOffs(0, 0).addBox(-1, -7, -1, 2, 7, 2), PartPose.offsetAndRotation(12, 12, -12, 2.3562F, -0.7854F, 0.0F));
		spikes.addOrReplaceChild("topbackright", CubeListBuilder.create().texOffs(0, 0).addBox(-1, -7, -1, 2, 7, 2), PartPose.offsetAndRotation(0, 0, 0, -0.7854F, -0.7854F, 0.0F));
		spikes.addOrReplaceChild("topbackleft", CubeListBuilder.create().texOffs(0, 0).addBox(-1, -7, -1, 2, 7, 2), PartPose.offsetAndRotation(12, 0, 0, -0.7854F, 0.7854F, 0.0F));
		spikes.addOrReplaceChild("topfrontright", CubeListBuilder.create().texOffs(0, 0).addBox(-1, -7, -1, 2, 7, 2), PartPose.offsetAndRotation(0, 0, -12, 0.7854F, 0.7854F, 0.0F));
		spikes.addOrReplaceChild("topfrontleft", CubeListBuilder.create().texOffs(0, 0).addBox(-1, -7, -1, 2, 7, 2), PartPose.offsetAndRotation(12, 0, -12, 0.7854F, -0.7854F, 0.0F));
		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void setupAnim(SwampMineEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){
		//previously the render function, render code was moved to a method below
	}

	@Override
	public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		spikes.render(matrixStack, buffer, packedLight, packedOverlay);
		bb_main.render(matrixStack, buffer, packedLight, packedOverlay);
	}
}