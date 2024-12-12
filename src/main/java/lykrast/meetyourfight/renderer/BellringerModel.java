package lykrast.meetyourfight.renderer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.BellringerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class BellringerModel extends HumanoidModel<BellringerEntity> {
	public static final ModelLayerLocation MODEL = new ModelLayerLocation(MeetYourFight.rl("bellringer"), "main");
	public static final ModelLayerLocation MODEL_HEAD = new ModelLayerLocation(MeetYourFight.rl("bellringer"), "head");
	// Mostly copied from Vex
	private final ModelPart bell, rightSleeve, leftSleeve, jacket;

	public BellringerModel(ModelPart modelPart) {
		super(modelPart);
		leftLeg.visible = false;
		bell = modelPart.getChild("bell");
		rightSleeve = modelPart.getChild("right_sleeve");
		leftSleeve = modelPart.getChild("left_sleeve");
		jacket = modelPart.getChild("jacket");
	}

	public static LayerDefinition createBodyLayer(CubeDeformation deform) {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(deform, 0);
		PartDefinition partdefinition = meshdefinition.getRoot();
		CubeDeformation clothes = deform.extend(0.25f);
		partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(26, 48).addBox(-1.0F, -1.0F, -2.0F, 6.0F, 10.0F, 4.0F, clothes), PartPose.offset(-1.9F, 12.0F, 0.0F));
		//Same rotation point as right arm, calibrated in tabula
		partdefinition.addOrReplaceChild("bell", CubeListBuilder.create().texOffs(0, 48).addBox(-4, 5, 2, 6, 6, 7), PartPose.offset(-5, 2, 0));
		//arms from humanoid, to move the uv
		partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform), PartPose.offset(-5.0F, 2.0F, 0.0F));
		partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform), PartPose.offset(5.0F, 2.0F, 0.0F));
		//layer 2 from player model
        partdefinition.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(0, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, clothes), PartPose.offset(-5.0F, 2.5F, 0.0F));
        partdefinition.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(40, 32).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, clothes), PartPose.offset(5.0F, 2.5F, 0.0F));
        partdefinition.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, clothes), PartPose.ZERO);
		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return Iterables.concat(super.bodyParts(), ImmutableList.of(bell, rightSleeve, leftSleeve, jacket));
	}

	@Override
	public void setupAnim(BellringerEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		rightArm.xRot = (float) (-Math.PI / 2);
		if (attackTime > 0) {
			// It's currently unmapped, but it's from setupAttackAnimation to swing the arm
			float f = 1 - attackTime;
			f = f * f;
			f = f * f;
			f = 1 - f;
			float f1 = Mth.sin(f * (float) Math.PI);
			float f2 = Mth.sin(attackTime * (float) Math.PI) * -(head.xRot - 0.7F) * 0.75F;
			rightArm.xRot -= f1 * 1.2F + f2;
		}
		bell.copyFrom(rightArm);
		rightSleeve.copyFrom(rightArm);
		leftSleeve.copyFrom(leftArm);
		jacket.copyFrom(body);
	}

}
