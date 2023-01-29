package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.RoseSpiritEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

//Made with Blockbench 4.1.5
//Exported for Minecraft version 1.17 with Mojang mappings
//Paste this class into your mod and generate all required imports
public class RoseSpiritModel extends EntityModel<RoseSpiritEntity> {
	public static final ModelLayerLocation MODEL = new ModelLayerLocation(MeetYourFight.rl("rose_spirit"), "main");
	private final ModelPart blob;
	private final ModelPart hat;
	private final ModelPart bb_main;

	public RoseSpiritModel(ModelPart root) {
		this.blob = root.getChild("blob");
		this.hat = root.getChild("hat");
		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		partdefinition.addOrReplaceChild("blob", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 18.0F, 0.0F));
		PartDefinition hat = partdefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 36).addBox(-6.0F, -2.0F, -6.0F, 12.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 14.0F, 0.0F));
		hat.addOrReplaceChild("top", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -1.0F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 0.0F));
		partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 16).addBox(-5.0F, -10.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(RoseSpiritEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		int status = entity.getStatus();
		if (status != RoseSpiritEntity.HIDING) {
			blob.y = 10;
			hat.y = 6;
		}
		else {
			blob.y = 18;
			hat.y = 14;
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		blob.render(poseStack, buffer, packedLight, packedOverlay);
		hat.render(poseStack, buffer, packedLight, packedOverlay);
		bb_main.render(poseStack, buffer, packedLight, packedOverlay);
	}
}