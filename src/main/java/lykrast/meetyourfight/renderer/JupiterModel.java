package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.JupiterEntity;
import lykrast.meetyourfight.misc.MYFUtils;
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

public class JupiterModel extends EntityModel<JupiterEntity> {
	public static final ModelLayerLocation MODEL = new ModelLayerLocation(MeetYourFight.rl("jupiter"), "main");
	public static final ModelLayerLocation MODEL_HEAD = new ModelLayerLocation(MeetYourFight.rl("jupiter"), "head");
	private Pose pose, prevPose;
	//ANIM_IDLE = 0, ANIM_CHARGE = 1, ANIM_AIM = 2, ANIM_THROW = 3;
	public static final Pose[] POSES = {new Pose(),
			new Pose().leftArm(25, -35, 0).leftForearm(80, 0, 0).rightArm(25, 35, 0).rightForearm(80, 0, 0),
			new Pose().leftArm(-60, 0, 0),
			new Pose().leftArm(100, 0, 0).leftForearm(40, 0, 0)};
	private float animProgress, projectileScale;
	private final ModelPart body, ball, head, projectile, leftArm, leftForearm, rightArm, rightForearm;

	public JupiterModel(ModelPart modelPart) {
		body = modelPart.getChild("Body");
		ball = modelPart.getChild("Ball");
		head = body.getChild("Head");
		leftArm = body.getChild("LeftArm");
		leftForearm = leftArm.getChild("LeftForearm");
		rightArm = body.getChild("RightArm");
		rightForearm = rightArm.getChild("RightForearm");
		projectile = leftForearm.getChild("Projectile");
	}

	public static LayerDefinition createBodyLayer(CubeDeformation deform) {
		//made in blockbench
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(0, 16).addBox(-6, -12, -3, 12, 8, 6, deform).texOffs(0, 30).addBox(-4, -4, -2, 8, 4, 4, deform),
				PartPose.offset(0, 8, 0));

		Body.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8, deform).texOffs(32, 0).addBox(-4, -8, -4, 8, 8, 8, deform.extend(0.5F)).texOffs(0, 0)
				.addBox(-1, -10, -1, 2, 2, 2, deform).texOffs(24, 0).addBox(-2, -14, -2, 4, 4, 4, deform), PartPose.offset(0, -12, 0));

		PartDefinition LeftArm = Body.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(48, 16).mirror().addBox(-1, -2, -2, 4, 10, 4, deform).mirror(false), PartPose.offset(7, -10, 0));
		PartDefinition LeftForearm = LeftArm.addOrReplaceChild("LeftForearm", CubeListBuilder.create().texOffs(48, 30).mirror().addBox(-2, -2, -2, 4, 10, 4, deform.extend(-0.1F)).mirror(false),
				PartPose.offset(1, 6, 0));
		LeftForearm.addOrReplaceChild("Projectile", CubeListBuilder.create().texOffs(32, 44).addBox(-4, -4, -4, 8, 8, 8, deform), PartPose.offset(0, 12, 0));

		PartDefinition RightArm = Body.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(48, 16).addBox(-3, -2, -2, 4, 10, 4, deform), PartPose.offset(-7, -10, 0));
		RightArm.addOrReplaceChild("RightForearm", CubeListBuilder.create().texOffs(48, 30).addBox(-2, -2, -2, 4, 10, 4, deform.extend(-0.1F)), PartPose.offset(-1, 6, 0));

		partdefinition.addOrReplaceChild("Ball", CubeListBuilder.create().texOffs(0, 38).addBox(-4, -4, -4, 8, 8, 8, deform), PartPose.offset(0, 18, 0));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void prepareMobModel(JupiterEntity entity, float limbSwing, float limbSwingAmount, float partialTick) {
		super.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
		animProgress = entity.getAnimProgress(partialTick);
		projectileScale = entity.getProjectileScale(partialTick);
		if (entity.clientAnim == JupiterEntity.ANIM_AIM || entity.clientAnim == JupiterEntity.ANIM_THROW) animProgress = MYFUtils.easeOutQuad(animProgress);
		else animProgress = MYFUtils.easeInQuad(animProgress);
		projectileScale = MYFUtils.easeInOut(projectileScale);
		pose = POSES[entity.clientAnim];
		prevPose = POSES[entity.prevAnim];
	}

	@Override
	public void setupAnim(JupiterEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		projectile.xScale = projectileScale;
		projectile.yScale = projectileScale;
		projectile.zScale = projectileScale;
		//I'd love to have the ball roll reastically but eh for now that'll placehold
		//it's the same as shulker bullets
		ball.xRot = Mth.cos(ageInTicks * 0.1f) * Mth.PI;
		ball.yRot = Mth.sin(ageInTicks * 0.1f) * Mth.PI;
		ball.zRot = Mth.sin(ageInTicks * 0.15f) * Mth.TWO_PI;
		head.xRot = headPitch * Mth.DEG_TO_RAD;
		head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
		//setup the base arm animation
		leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
		leftArm.yRot = 0.0F;
		leftArm.zRot = 0.0F;
		rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F;
		rightArm.yRot = 0.0F;
		rightArm.zRot = 0.0F;
		leftForearm.xRot = Math.max(0, leftArm.xRot * 0.1f);
		leftForearm.yRot = 0.0F;
		leftForearm.zRot = 0.0F;
		rightForearm.xRot = Math.max(0, rightArm.xRot * 0.1f);
		leftForearm.yRot = 0.0F;
		leftForearm.zRot = 0.0F;
		pose.interpolate(this, prevPose, animProgress);
	}

	@Override
	public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		body.render(matrixStack, buffer, packedLight, packedOverlay);
		ball.render(matrixStack, buffer, packedLight, packedOverlay);
	}
	
	private static class Pose {
		//copied that back from bpas's skybenders
		//x y z rot
		public final float[] leftArm = {0,0,0},
				leftForearm = {0,0,0},
				rightArm = {0,0,0},
				rightForearm = {0,0,0};
		public boolean usesLeft = false, usesRight;
		
		public void interpolate(JupiterModel model, Pose prev, float progress) {
			if (progress > 0.99) {
				if (usesLeft) {
					apply(model.leftArm, leftArm);
					apply(model.leftForearm, leftForearm);
				}
				if (usesRight) {
					apply(model.rightArm, rightArm);
					apply(model.rightForearm, rightForearm);
				}
			}
			else {
				if (usesLeft) {
					if (prev.usesLeft) {
						//both prev and current use arm
						interpolateRot(model.leftArm, leftArm, prev.leftArm, progress);
						interpolateRot(model.leftForearm, leftForearm, prev.leftForearm, progress);
					}
					else {
						//go from neutral to new animation
						interpolateFromNeutral(model.leftArm, leftArm, progress);
						interpolateFromNeutral(model.leftForearm, leftForearm, progress);
					}
				}
				else if (prev.usesLeft) {
					//go from prev back to neutral
					//if neither current or prev use the arm, then don't need to animate anything
					interpolateToNeutral(model.leftArm, prev.leftArm, progress);
					interpolateToNeutral(model.leftForearm, prev.leftForearm, progress);
				}
				if (usesRight) {
					if (prev.usesRight) {
						//both prev and current use arm
						interpolateRot(model.rightArm, rightArm, prev.rightArm, progress);
						interpolateRot(model.rightForearm, rightForearm, prev.rightForearm, progress);
					}
					else {
						//go from neutral to new animation
						interpolateFromNeutral(model.rightArm, rightArm, progress);
						interpolateFromNeutral(model.rightForearm, rightForearm, progress);
					}
				}
				else if (prev.usesRight) {
					//go from prev back to neutral
					//if neither current or prev use the arm, then don't need to animate anything
					interpolateToNeutral(model.rightArm, prev.rightArm, progress);
					interpolateToNeutral(model.rightForearm, prev.rightForearm, progress);
				}
			}
		}
		
		private static void interpolateRot(ModelPart part, float[] self, float[] prev, float progress) {
			part.xRot = MYFUtils.rotlerpRad(progress, prev[0], self[0]);
			part.yRot = MYFUtils.rotlerpRad(progress, prev[1], self[1]);
			part.zRot = MYFUtils.rotlerpRad(progress, prev[2], self[2]);
		}

		private static void interpolateFromNeutral(ModelPart part, float[] pose, float progress) {
			part.xRot = MYFUtils.rotlerpRad(progress, part.xRot, pose[0]);
			part.yRot = MYFUtils.rotlerpRad(progress, part.yRot, pose[1]);
			part.zRot = MYFUtils.rotlerpRad(progress, part.zRot, pose[2]);
		}
		
		private static void interpolateToNeutral(ModelPart part, float[] pose, float progress) {
			part.xRot = MYFUtils.rotlerpRad(progress, pose[0], part.xRot);
			part.yRot = MYFUtils.rotlerpRad(progress, pose[1], part.yRot);
			part.zRot = MYFUtils.rotlerpRad(progress, pose[2], part.zRot);
		}
		
		private static void apply(ModelPart part, float[] self) {
			part.xRot = self[0];
			part.yRot = self[1];
			part.zRot = self[2];
		}
		
		public Pose leftArm(float x, float y, float z) {
			leftArm[0] = x * -Mth.DEG_TO_RAD;
			leftArm[1] = y * -Mth.DEG_TO_RAD;
			leftArm[2] = z * Mth.DEG_TO_RAD;
			usesLeft = true;
			return this;
		}
		public Pose leftForearm(float x, float y, float z) {
			leftForearm[0] = x * -Mth.DEG_TO_RAD;
			leftForearm[1] = y * -Mth.DEG_TO_RAD;
			leftForearm[2] = z * Mth.DEG_TO_RAD;
			usesLeft = true;
			return this;
		}
		public Pose rightArm(float x, float y, float z) {
			rightArm[0] = x * -Mth.DEG_TO_RAD;
			rightArm[1] = y * -Mth.DEG_TO_RAD;
			rightArm[2] = z * Mth.DEG_TO_RAD;
			usesRight = true;
			return this;
		}
		public Pose rightForearm(float x, float y, float z) {
			rightForearm[0] = x * -Mth.DEG_TO_RAD;
			rightForearm[1] = y * -Mth.DEG_TO_RAD;
			rightForearm[2] = z * Mth.DEG_TO_RAD;
			usesRight = true;
			return this;
		}
	}

}
