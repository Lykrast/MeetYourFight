package lykrast.meetyourfight.renderer;

import lykrast.meetyourfight.entity.DameFortunaEntity;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

public class DameFortunaModel extends BipedModel<DameFortunaEntity> {
	public float headProgress;
	
	public DameFortunaModel() {
		super(0, 0, 64, 64);
		bipedHead = new ModelRenderer(this, 0, 0);
		bipedHead.addBox(-4, -4, -4, 8, 8, 8, 0);
		bipedHead.setRotationPoint(0, -8, 0);
	}

	@Override
	public void setLivingAnimations(DameFortunaEntity entityIn, float limbSwing, float limbSwingAmount, float partialTick) {
		headProgress = entityIn.getHeadRotationProgress(partialTick);
		super.setLivingAnimations(entityIn, limbSwing, limbSwingAmount, partialTick);
	}

	@Override
	public void setRotationAngles(DameFortunaEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		//Save previous head angles for animations before super overrides them so we override them back
		float headX = bipedHead.rotateAngleX;
		float headY = bipedHead.rotateAngleY;
		float headZ = bipedHead.rotateAngleZ;
		
		super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		//Hey guess what this gets overridden in the super too
		//We'll do something fancy with it, should cycle every 100 ticks (5 seconds)
        bipedHead.rotationPointY = -8 + MathHelper.sin(ageInTicks * (float)Math.PI / 50f);
		
		//Head animation
		bipedHead.rotateAngleX = rotLerpRad(headProgress, headX, entityIn.headTargetPitch * ((float)Math.PI / 2f));
		bipedHead.rotateAngleY = rotLerpRad(headProgress, headY, entityIn.headTargetYaw * ((float)Math.PI / 2f));
		bipedHead.rotateAngleZ = rotLerpRad(headProgress, headZ, entityIn.headTargetRoll * ((float)Math.PI / 2f));
		
		int attack = entityIn.getAttack();
		//Same pose as Illagers casting spells
		//1 is normal attack, 2 is big attack
		if (attack == DameFortunaEntity.PROJ_ATTACK) {
			bipedLeftArm.rotationPointZ = 0.0F;
			bipedLeftArm.rotationPointX = 5.0F;
			bipedLeftArm.rotateAngleX = MathHelper.cos(ageInTicks * 0.6662F) * 0.25F;
			bipedLeftArm.rotateAngleZ = -2.3561945F;
			bipedLeftArm.rotateAngleY = 0.0F;
		}
		else if (attack == DameFortunaEntity.CLAW_ATTACK) {
			bipedRightArm.rotationPointZ = 0.0F;
			bipedRightArm.rotationPointX = -5.0F;
			bipedRightArm.rotateAngleX = MathHelper.cos(ageInTicks * 0.6662F) * 0.25F;
			bipedRightArm.rotateAngleZ = 2.3561945F;
			bipedRightArm.rotateAngleY = 0.0F;
		}
	}

}
