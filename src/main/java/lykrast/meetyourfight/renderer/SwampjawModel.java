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
	private final ModelPart jaw;
	private final ModelPart head;
	
	//Blockbench constant
	private static final float TAILFIN_PITCH = 0.2618F;
	
	private float tailYaw, tailPitch;
	private float animProgress;

	public SwampjawModel(ModelPart modelPart) {
		bodyMain = modelPart.getChild("body");
		finRight = bodyMain.getChild("fin_right");
		finLeft = bodyMain.getChild("fin_left");
		tailInner = bodyMain.getChild("tail_inner");
		tailOuter = tailInner.getChild("tail_outer");
		tailfinTop = tailOuter.getChild("tail_fin_top");
		tailfinBottom = tailOuter.getChild("tail_fin_bottom");
		head = bodyMain.getChild("head");
		jaw = head.getChild("jaw");
	}
	
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(40, 0).addBox(-6, -5, -6, 12, 10, 12), PartPose.offset(0, 19, 0));
		body.addOrReplaceChild("fin_right", CubeListBuilder.create().texOffs(0, 28).addBox(-8, 0, -2, 8, 1, 4), PartPose.offsetAndRotation(-6, 0, 0, 0, 0, -0.4363F));
		body.addOrReplaceChild("fin_left", CubeListBuilder.create().texOffs(0, 28).addBox(0, 0, -2, 8, 1, 4), PartPose.offsetAndRotation(6, 0, 0, 0, 0, 0.4363F));
		PartDefinition tailInner = body.addOrReplaceChild("tail_inner", CubeListBuilder.create().texOffs(40, 22).addBox(-5, 0, 0, 10, 8, 8), PartPose.offset(0, -5, 6));
		PartDefinition tailOuter = tailInner.addOrReplaceChild("tail_outer", CubeListBuilder.create().texOffs(40, 38).addBox(-1, 0, 0, 2, 2, 6), PartPose.offset(0, 0, 8));
		tailOuter.addOrReplaceChild("tail_fin_top", CubeListBuilder.create().texOffs(0, 33).addBox(-0.5f, -10, 0, 1, 10, 5), PartPose.offsetAndRotation(0, 1, 5, -TAILFIN_PITCH, 0, 0));
		tailOuter.addOrReplaceChild("tail_fin_bottom", CubeListBuilder.create().texOffs(12, 33).addBox(-0.5f, 0, 0, 1, 10, 5, new CubeDeformation(-0.1f)), PartPose.offsetAndRotation(0, 1, 5, TAILFIN_PITCH, 0, 0));
		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-5, -4, -10, 10, 6, 10), PartPose.offset(0, 0, -6));
		head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(0, 16).addBox(-5, 0, -10, 10, 2, 10), PartPose.offset(0, 2, 0));
		return LayerDefinition.create(meshdefinition, 128, 64);
	}

	@Override
	public void prepareMobModel(SwampjawEntity entity, float limbSwing, float limbSwingAmount, float partialTick) {
		super.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
		tailYaw = Mth.degreesDifference(entity.getYRot(), entity.getTailYaw(partialTick)) / 3F;
		tailPitch = -Mth.degreesDifference(entity.getXRot(), entity.getTailPitch(partialTick)) / 1.5F;
		tailYaw *= Mth.DEG_TO_RAD;
		tailPitch *= Mth.DEG_TO_RAD;
		animProgress = entity.getAnimProgress(partialTick);
		if (entity.clientAnim == SwampjawEntity.ANIM_SWIPE) {
			//ease in and out https://math.stackexchange.com/questions/121720/ease-in-out-function/121755#121755
			float sq = animProgress * animProgress;
			animProgress = sq / (2 * (sq - animProgress)+1);
		}
		else if (entity.clientAnim == SwampjawEntity.ANIM_STUN || entity.prevAnim == SwampjawEntity.ANIM_SWOOP) {
			//quadratic ease out
			animProgress = 1-animProgress;
			animProgress = 1-(animProgress*animProgress);
		}
		else {
			//quadratic ease in
			animProgress *= animProgress;
		}
	}

	@Override
	public void setupAnim(SwampjawEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		//bodyMain.rotateAngleX = entity.rotationPitch * ((float) Math.PI / 180F);

		//animations
		//head and jaw
		if (entity.clientAnim == SwampjawEntity.ANIM_SWOOP) {
			//open mouth, head a bit up to see the mouth
			float offset = 15*animProgress;
			headPitch -= offset;
			jaw.xRot = 3*offset*Mth.DEG_TO_RAD;
			bodyMain.zRot = 180*Mth.DEG_TO_RAD;
		}
		else if (entity.prevAnim == SwampjawEntity.ANIM_SWOOP && animProgress < 0.99) {
			//closing mouth after a swoop
			float offset = 15*(1-animProgress);
			headPitch -= offset;
			//jaw stays open when stunned
			if (entity.clientAnim == SwampjawEntity.ANIM_STUN) jaw.xRot = (offset+30)*Mth.DEG_TO_RAD;
			else jaw.xRot = 3*offset*Mth.DEG_TO_RAD;
		}
		else if (entity.clientAnim == SwampjawEntity.ANIM_STUN) {
			//no need to ease the jaw here because it's covered by previous if
			jaw.xRot = 30*Mth.DEG_TO_RAD;
		}
		else if (entity.prevAnim == SwampjawEntity.ANIM_STUN && animProgress < 0.99) {
			//closing mouth after stun
			float offset = 30*(1-animProgress);
			jaw.xRot = offset*Mth.DEG_TO_RAD;
		}
		else jaw.xRot = 0;
		//body zrot
		if (entity.clientAnim == SwampjawEntity.ANIM_STUN) {
			bodyMain.zRot = 180*animProgress*Mth.DEG_TO_RAD;
		}
		else if (entity.prevAnim == SwampjawEntity.ANIM_STUN && animProgress < 0.99) {
			bodyMain.zRot = 180*(1+animProgress)*Mth.DEG_TO_RAD;
		}
		else bodyMain.zRot = 0;
		//body yrot
		if (entity.clientAnim == SwampjawEntity.ANIM_SWIPE) {
			bodyMain.yRot = 360*animProgress*Mth.DEG_TO_RAD;
		}
		else bodyMain.yRot = 0;
		
		//Head
		head.xRot = headPitch * Mth.DEG_TO_RAD;
		head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
		
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
}