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
	// Mostly copied from Vex
	private final ModelPart bell;

	public BellringerModel(ModelPart modelPart) {
		super(modelPart);
		leftLeg.visible = false;
		hat.visible = false;
		bell = modelPart.getChild("bell");
	}

	public static LayerDefinition createBodyLayer() {
	      MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
	      PartDefinition partdefinition = meshdefinition.getRoot();
	      partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, -1.0F, -2.0F, 6.0F, 10.0F, 4.0F), PartPose.offset(-1.9F, 12.0F, 0.0F));
	      //Same rotation point as right arm, calibrated in tabula
	      partdefinition.addOrReplaceChild("bell", CubeListBuilder.create().texOffs(0, 32).addBox(-4, 5, 2, 6, 6, 7), PartPose.offset(-5, 2, 0));
	      return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return Iterables.concat(super.bodyParts(), ImmutableList.of(bell));
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
	}

}
