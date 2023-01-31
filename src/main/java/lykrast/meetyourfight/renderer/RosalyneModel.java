package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.RosalyneEntity;
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

//Made with Blockbench 4.1.5
//Exported for Minecraft version 1.17 with Mojang mappings
public class RosalyneModel extends EntityModel<RosalyneEntity> {
	public static final ModelLayerLocation MODEL = new ModelLayerLocation(MeetYourFight.rl("rosalyne"), "main");
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart rightArm;
	private final ModelPart forearm;
	private final ModelPart blade;
	private final ModelPart leftArm;
	private final ModelPart rightLeg;
	private final ModelPart leftLeg;
	private final ModelPart coffin;
	//x, y, z rotations for the right arm for each animation
	private static final Vector3f[] animRotations = {
			new Vector3f(0,0,0), 
			new Vector3f(0,0,2.3561945F), 
			new Vector3f(-15*Mth.DEG_TO_RAD,0,75*Mth.DEG_TO_RAD), 
			new Vector3f(-135*Mth.DEG_TO_RAD,0,75*Mth.DEG_TO_RAD)
			};
	private float animProgress;
	private int phase;

	public RosalyneModel(ModelPart root) {
		head = root.getChild("Head");
		body = root.getChild("Body");
		rightArm = root.getChild("RightArm");
		forearm = rightArm.getChild("Forearm");
		blade = forearm.getChild("Blade");
		leftArm = root.getChild("LeftArm");
		rightLeg = root.getChild("RightLeg");
		leftLeg = root.getChild("LeftLeg");
		coffin = root.getChild("Coffin");
	}
	
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		//Head
		partdefinition.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));
		//Body
		partdefinition.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
		
		//Right arm (the one with the blade)
		PartDefinition RightArm = partdefinition.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));
		PartDefinition Forearm = RightArm.addOrReplaceChild("Forearm", CubeListBuilder.create().texOffs(0, 34).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 10.0F, 0.0F));
		Forearm.addOrReplaceChild("Blade", CubeListBuilder.create().texOffs(16, 32).addBox(-0.5F, -6.0F, -3.0F, 1.0F, 20.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 52).addBox(-3.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 10.0F, 0.0F));

		//Left arm
		partdefinition.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 2.0F, 0.0F));
		//Right leg
		partdefinition.addOrReplaceChild("RightLeg", CubeListBuilder.create().texOffs(40, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
		//Left leg
		partdefinition.addOrReplaceChild("LeftLeg", CubeListBuilder.create().texOffs(40, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 0.0F));
		//Coffin
		partdefinition.addOrReplaceChild("Coffin", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -32.0F, -8.0F, 16.0F, 32.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void prepareMobModel(RosalyneEntity entityIn, float limbSwing, float limbSwingAmount, float partialTick) {
		super.prepareMobModel(entityIn, limbSwing, limbSwingAmount, partialTick);
		animProgress = entityIn.getAnimProgress(partialTick);
		if (entityIn.clientAnim != RosalyneEntity.ANIM_NEUTRAL) {
			//Fast initial then slows down
			animProgress = 1-animProgress;
			animProgress *= animProgress;
			animProgress *= animProgress;
			animProgress = 1-animProgress;
		}
		phase = entityIn.getPhase();
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		if (phase == RosalyneEntity.ENCASED || phase == RosalyneEntity.BREAKING_OUT) {
			coffin.render(poseStack, buffer, packedLight, packedOverlay);
		}
		else {
			head.render(poseStack, buffer, packedLight, packedOverlay);
			body.render(poseStack, buffer, packedLight, packedOverlay);
			rightArm.render(poseStack, buffer, packedLight, packedOverlay);
			leftArm.render(poseStack, buffer, packedLight, packedOverlay);
			rightLeg.render(poseStack, buffer, packedLight, packedOverlay);
			leftLeg.render(poseStack, buffer, packedLight, packedOverlay);
		}
	}
	
	@Override
	public void setupAnim(RosalyneEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		//Head
		head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
		head.xRot = headPitch * Mth.DEG_TO_RAD;
		coffin.yRot = netHeadYaw * Mth.DEG_TO_RAD;
		//Legs, lifted from HumanoidModel
		leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
		rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		//Arms
		Vector3f anim = animRotations[entityIn.clientAnim];
		if (animProgress >= 0.99) {
			rightArm.xRot = anim.x();
			rightArm.yRot = anim.y();
			rightArm.zRot = anim.z();
		}
		else {
			Vector3f prev = animRotations[entityIn.prevAnim];
			rightArm.xRot = rotlerpRad(animProgress, prev.x(), anim.x());
			rightArm.yRot = rotlerpRad(animProgress, prev.y(), anim.y());
			rightArm.zRot = rotlerpRad(animProgress, prev.z(), anim.z());
		}
	}

	//LIKE REALLY? THAT'S PROTECTED AND IN HUMANOIDRENDER???
	//might as well clean up the names and constants
	private float rotlerpRad(float progress, float start, float end) {
		float diff = (end - start) % Mth.TWO_PI;
		if (diff < -Mth.PI) diff += Mth.TWO_PI;
		if (diff >= Mth.PI) diff -= Mth.TWO_PI;
		return start + progress * diff;
	}

}
