package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

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
	//Right arm pose for each animation they take degrees from blockbench for readability
	//ANIM_NEUTRAL = 0, ANIM_ARM_OUT_UP = 1, ANIM_ARM_IN_UP = 2, ANIM_ARM_OUT_DN = 3, ANIM_ARM_IN_DN = 4, ANIM_PREPARE_DASH = 5;
	//ANIM_BROKE_OUT = 6, ANIM_SUMMONING = 7, ANIM_MADDENING = 8
	private static final Pose[] RARM_POSE = {
			new Pose(-5,0,0, 30,0,0, 15,0,0, false), //ANIM_NEUTRAL
			new Pose(-10,0,100, 0,0,0, 0,0,0), //ANIM_ARM_OUT_UP
			new Pose(105,0,95, 80,0,0, 30,0,0), //ANIM_ARM_IN_UP
			new Pose(-10,0,80, 0,0,0, 0,0,0), //ANIM_ARM_OUT_DN
			new Pose(100,0,120, 80,0,0, 30,0,0), //ANIM_ARM_IN_DN
			new Pose(-5,0,40, 30,0,0, 30,0,0), //ANIM_PREPARE_DASH
			new Pose(-30,0,0, 130,0,0, -100,0,0, false), //ANIM_BROKE_OUT
			new Pose(-5,0,0, 30,0,0, 15,0,0, false), //ANIM_SUMMONING same for the arm as neutral
			new Pose(30,35,0, 110,0,0, 85,0,0, false) //ANIM_MADDENING
			};
	private float animProgress;
	private int phase;
	private Pose pose, prevPose;

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
		phase = entityIn.getPhase();
		pose = RARM_POSE[entityIn.clientAnim];
		prevPose = RARM_POSE[entityIn.prevAnim];
		animProgress = entityIn.getAnimProgress(partialTick);
		if (pose.fast) {
			//Fast initial then slows down
			animProgress = 1-animProgress;
			animProgress *= animProgress;
			animProgress *= animProgress;
			animProgress = 1-animProgress;
		}
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
		//Coffin if we in phase 1 and also it lets us skip the rest
		if (phase == RosalyneEntity.ENCASED || phase == RosalyneEntity.BREAKING_OUT) {
			coffin.yRot = netHeadYaw * Mth.DEG_TO_RAD;
			return;
		}
		//Head and left arm (with left arm swing from HumanoidModel)
		//Set all values to default, then override as needed for Summoning and Maddening animations
		//Also lets us have the default value ready to interpolate
		head.xRot = headPitch * Mth.DEG_TO_RAD;
		head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
		leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
		leftArm.yRot = 0;
		leftArm.zRot = 0;
		//Maddening animation
		if (entityIn.clientAnim == RosalyneEntity.ANIM_MADDENING) {
			if (animProgress >= 0.99) {
				//In the maddening animation
				head.xRot = -25 * Mth.DEG_TO_RAD;
				head.yRot = 0;
				leftArm.xRot = -160 * Mth.DEG_TO_RAD; //In blockbench put 160
				leftArm.yRot = 30 * Mth.DEG_TO_RAD; //In blockbench put -30
			}
			else {
				//Transitionning TO maddening
				head.xRot = rotlerpRad(animProgress, head.xRot, 25 * Mth.DEG_TO_RAD);
				head.yRot = rotlerpRad(animProgress, head.yRot, 0);
				leftArm.xRot = rotlerpRad(animProgress, leftArm.xRot, -160 * Mth.DEG_TO_RAD);
				leftArm.yRot = rotlerpRad(animProgress, leftArm.yRot, 30 * Mth.DEG_TO_RAD);
			}
		}
		else if (entityIn.prevAnim == RosalyneEntity.ANIM_MADDENING) {
			//Transition FROM maddening
			head.xRot = rotlerpRad(animProgress, 25 * Mth.DEG_TO_RAD, head.xRot);
			head.yRot = rotlerpRad(animProgress, 0, head.yRot);
			leftArm.xRot = rotlerpRad(animProgress, -160 * Mth.DEG_TO_RAD, leftArm.xRot);
			leftArm.yRot = rotlerpRad(animProgress, 30 * Mth.DEG_TO_RAD, leftArm.yRot);
		}
		else {
			//No transition from Summoning to Maddening should happen
			//Summoning animation
			//Copied from Illagers spellcasting
			if (entityIn.clientAnim == RosalyneEntity.ANIM_SUMMONING) {
				if (animProgress >= 0.99) {
					//Animation
					leftArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.25F;
					leftArm.zRot = -2.3561945F;
				}
				else {
					//TO animation
					leftArm.xRot = rotlerpRad(animProgress, leftArm.xRot, Mth.cos(ageInTicks * 0.6662F) * 0.25F);
					leftArm.zRot = rotlerpRad(animProgress, leftArm.zRot, -2.3561945F);
				}
			}
			else if (entityIn.prevAnim == RosalyneEntity.ANIM_SUMMONING) {
				//FROM animation
				leftArm.xRot = rotlerpRad(animProgress, Mth.cos(ageInTicks * 0.6662F) * 0.25F, leftArm.xRot);
				leftArm.zRot = rotlerpRad(animProgress, -2.3561945F, leftArm.zRot);
			}
		}
		//Legs, lifted from HumanoidModel
		rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		//Blade arm
		if (animProgress >= 0.99) {
			rightArm.xRot = pose.armX;
			rightArm.yRot = pose.armY;
			rightArm.zRot = pose.armZ;
			forearm.xRot = pose.foreX;
			forearm.yRot = pose.foreY;
			forearm.zRot = pose.foreZ;
			blade.xRot = pose.bladeX;
			blade.yRot = pose.bladeY;
			blade.zRot = pose.bladeZ;
		}
		else {
			rightArm.xRot = rotlerpRad(animProgress, prevPose.armX, pose.armX);
			rightArm.yRot = rotlerpRad(animProgress, prevPose.armY, pose.armY);
			rightArm.zRot = rotlerpRad(animProgress, prevPose.armZ, pose.armZ);
			forearm.xRot = rotlerpRad(animProgress, prevPose.foreX, pose.foreX);
			forearm.yRot = rotlerpRad(animProgress, prevPose.foreY, pose.foreY);
			forearm.zRot = rotlerpRad(animProgress, prevPose.foreZ, pose.foreZ);
			blade.xRot = rotlerpRad(animProgress, prevPose.bladeX, pose.bladeX);
			blade.yRot = rotlerpRad(animProgress, prevPose.bladeY, pose.bladeY);
			blade.zRot = rotlerpRad(animProgress, prevPose.bladeZ, pose.bladeZ);
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
	
	private static class Pose {
		public final float armX, armY, armZ, foreX, foreY, foreZ, bladeX, bladeY, bladeZ;
		public final boolean fast;
		
		public Pose(float armX, float armY, float armZ, float foreX, float foreY, float foreZ, float bladeX, float bladeY, float bladeZ) {
			this(armX, armY, armZ, foreX, foreY, foreZ, bladeX, bladeY, bladeZ, true);
		}

		public Pose(float armX, float armY, float armZ, float foreX, float foreY, float foreZ, float bladeX, float bladeY, float bladeZ, boolean fast) {
			//Don't know why some of the blockbench angles are reversed so that's why there are -
			this.armX = armX * -Mth.DEG_TO_RAD;
			this.armY = armY * -Mth.DEG_TO_RAD;
			this.armZ = armZ * Mth.DEG_TO_RAD;
			this.foreX = foreX * -Mth.DEG_TO_RAD;
			this.foreY = foreY * -Mth.DEG_TO_RAD;
			this.foreZ = foreZ * Mth.DEG_TO_RAD;
			this.bladeX = bladeX * -Mth.DEG_TO_RAD;
			this.bladeY = bladeY * -Mth.DEG_TO_RAD;
			this.bladeZ = bladeZ * Mth.DEG_TO_RAD;
			this.fast = fast;
		}
	}

}
