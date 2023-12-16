package lykrast.meetyourfight.renderer;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.DameFortunaEntity;
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

public class DameFortunaModel extends HumanoidModel<DameFortunaEntity> {
	public static final ModelLayerLocation MODEL = new ModelLayerLocation(MeetYourFight.rl("dame_fortuna"), "main");
	public float headProgress;
	
	public DameFortunaModel(ModelPart modelPart) {
		super(modelPart);
	}

	public static LayerDefinition createBodyLayer() {
	      MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
	      PartDefinition partdefinition = meshdefinition.getRoot();
	      partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4, -4, -4, 8, 8, 8), PartPose.offset(0, -8, 0));
	      return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void prepareMobModel(DameFortunaEntity entityIn, float limbSwing, float limbSwingAmount, float partialTick) {
		headProgress = entityIn.getHeadRotationProgress(partialTick);
		super.prepareMobModel(entityIn, limbSwing, limbSwingAmount, partialTick);
	}

	@Override
	public void setupAnim(DameFortunaEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		//Save previous head angles for animations before super overrides them so we override them back
		float headX = head.xRot;
		float headY = head.yRot;
		float headZ = head.zRot;
		
		super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		//Hey guess what this gets overridden in the super too
		//We'll do something fancy with it, should cycle every 100 ticks (5 seconds)
        head.y = -8 + Mth.sin(ageInTicks * (float)Math.PI / 50f);
		
		//Head animation
		head.xRot = rotlerpRad(headProgress, headX, entityIn.headTargetPitch * Mth.HALF_PI);
		head.yRot = rotlerpRad(headProgress, headY, entityIn.headTargetYaw * Mth.HALF_PI);
		head.zRot = rotlerpRad(headProgress, headZ, entityIn.headTargetRoll * Mth.HALF_PI);
		
		int attack = entityIn.getAnimation();
		//Same pose as Illagers casting spells
		//1 is normal attack, 2 is big attack
		if (attack == DameFortunaEntity.PROJ_ATTACK) {
			leftArm.z = 0.0F;
			leftArm.x = 5.0F;
			leftArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.25F;
			leftArm.zRot = -2.3561945F;
			leftArm.yRot = 0.0F;
		}
		else if (attack == DameFortunaEntity.CLAW_ATTACK) {
			rightArm.z = 0.0F;
			rightArm.x = -5.0F;
			rightArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.25F;
			rightArm.zRot = 2.3561945F;
			rightArm.yRot = 0.0F;
		}
	}

}
