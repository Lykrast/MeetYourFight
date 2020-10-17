package lykrast.meetyourfight.renderer;

import lykrast.meetyourfight.entity.DameFortunaEntity;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.util.math.MathHelper;

public class DameFortunaModel extends BipedModel<DameFortunaEntity> {
	public DameFortunaModel() {
		super(0, 0, 64, 64);
	}

	@Override
	public void setRotationAngles(DameFortunaEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
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
