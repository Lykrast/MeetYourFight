package lykrast.meetyourfight.renderer;

import lykrast.meetyourfight.entity.DameFortunaEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public class DameFortunaModel extends HumanoidModel<DameFortunaEntity> {
	public float headProgress;
	
	public DameFortunaModel() {
		super(0, 0, 64, 64);
		head = new ModelPart(this, 0, 0);
		head.addBox(-4, -4, -4, 8, 8, 8, 0);
		head.setPos(0, -8, 0);
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
		head.xRot = rotlerpRad(headProgress, headX, entityIn.headTargetPitch * ((float)Math.PI / 2f));
		head.yRot = rotlerpRad(headProgress, headY, entityIn.headTargetYaw * ((float)Math.PI / 2f));
		head.zRot = rotlerpRad(headProgress, headZ, entityIn.headTargetRoll * ((float)Math.PI / 2f));
		
		int attack = entityIn.getAttack();
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
