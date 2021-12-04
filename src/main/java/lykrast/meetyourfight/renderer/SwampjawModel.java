package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

// Made with Blockbench 3.7.4
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
public class SwampjawModel extends EntityModel<SwampjawEntity> {
	public static final ModelLayerLocation MODEL = new ModelLayerLocation(MeetYourFight.rl("swampjaw"), "main");
	private final ModelPart bodyMain;
	private final ModelPart finRight;
	private final ModelPart finLeft;
	private final ModelPart tailfinBottom;
	private final ModelPart tailfinTop;
	private final ModelPart tailOuter;
	private final ModelPart tailInner;
	//private final ModelPart jaw;
	private final ModelPart head;
	
	//Blockbench constant
	private static final float TAILFIN_PITCH = 0.2618F;
	
	private float tailYaw, tailPitch;

	public SwampjawModel(ModelPart modelPart) {
		bodyMain = modelPart.getChild("body");
		finRight = modelPart.getChild("fin_right");
		finLeft = modelPart.getChild("fin_left");
		tailInner = modelPart.getChild("tail_inner");
		tailOuter = modelPart.getChild("tail_outer");
		tailfinTop = modelPart.getChild("tail_fin_top");
		tailfinBottom = modelPart.getChild("tail_fin_bottom");
		head = modelPart.getChild("head");
		//jaw = modelPart.getChild("jaw");
	}
	
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(40, 0).addBox(-6, -10, -6, 12, 10, 12), PartPose.offset(0, 24, 0));
		body.addOrReplaceChild("fin_right", CubeListBuilder.create().texOffs(0, 28).addBox(-8, 0, -2, 8, 1, 4), PartPose.offsetAndRotation(-6, -5, 0, 0, 0, -0.4363F));
		body.addOrReplaceChild("fin_left", CubeListBuilder.create().texOffs(0, 28).addBox(0, 0, -2, 8, 1, 4), PartPose.offsetAndRotation(6, -5, 0, 0, 0, 0.4363F));
		PartDefinition tailInner = body.addOrReplaceChild("tail_inner", CubeListBuilder.create().texOffs(40, 22).addBox(-5, 0, 0, 10, 8, 8), PartPose.offset(0, -10, 6));
		PartDefinition tailOuter = tailInner.addOrReplaceChild("tail_outer", CubeListBuilder.create().texOffs(40, 38).addBox(-1, 0, 0, 2, 2, 6), PartPose.offset(0, 0, 8));
		tailOuter.addOrReplaceChild("tail_fin_top", CubeListBuilder.create().texOffs(0, 33).addBox(-0.5f, -10, 0, 1, 10, 5), PartPose.offsetAndRotation(0, 1, 5, -TAILFIN_PITCH, 0, 0));
		tailOuter.addOrReplaceChild("tail_fin_bottom", CubeListBuilder.create().texOffs(12, 33).addBox(-0.5f, 0, 0, 1, 10, 5, new CubeDeformation(-0.1f)), PartPose.offsetAndRotation(0, 1, 5, TAILFIN_PITCH, 0, 0));
		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(12, 33).addBox(-5, -4, -10, 10, 6, 10), PartPose.offset(0, -5, -6));
		head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(0, 16).addBox(-5, 0, -10, 10, 2, 10), PartPose.offset(0, 2, 0));
		return LayerDefinition.create(meshdefinition, 128, 64);
	}

	@Override
	public void prepareMobModel(SwampjawEntity entity, float limbSwing, float limbSwingAmount, float partialTick) {
		super.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
		tailYaw = Mth.degreesDifference(entity.getYRot(), entity.getTailYaw(partialTick)) / 3F;
		tailPitch = -Mth.degreesDifference(entity.getXRot(), entity.getTailPitch(partialTick)) / 1.5F;
		tailYaw *= (float)Math.PI / 180F;
		tailPitch *= (float)Math.PI / 180F;
	}

	@Override
	public void setupAnim(SwampjawEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		//bodyMain.rotateAngleX = entity.rotationPitch * ((float) Math.PI / 180F);
		
		//Head
		head.xRot = headPitch * ((float) Math.PI / 180F);
		head.yRot = netHeadYaw * ((float) Math.PI / 180F);
		
		//Flap fins
		finLeft.yRot = Mth.cos(limbSwing * 0.2F) * 0.8F * limbSwingAmount;
		finRight.yRot = Mth.cos(limbSwing * 0.2F + (float)Math.PI) * 0.8F * limbSwingAmount;
		
		//Rotate tail
		tailInner.xRot = tailPitch;
		tailInner.yRot = tailYaw + Mth.cos(limbSwing * 0.35F) * 0.15F * limbSwingAmount;
		tailOuter.xRot = tailPitch;
		tailOuter.yRot = tailYaw + Mth.cos(limbSwing * 0.35F + (float)Math.PI / 3F) * 0.15F * limbSwingAmount;
		tailfinTop.yRot = tailYaw + Mth.cos(limbSwing * 0.35F + 2F * (float)Math.PI / 3F) * 0.15F * limbSwingAmount;
		tailfinBottom.yRot = tailfinTop.yRot;
	}

	@Override
	public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		bodyMain.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}