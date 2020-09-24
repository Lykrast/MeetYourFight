package lykrast.meetyourfight.renderer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import lykrast.meetyourfight.entity.BellringerEntity;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

public class BellringerModel extends BipedModel<BellringerEntity> {
	// Mostly copied from Vex
	private final ModelRenderer bell;

	public BellringerModel() {
		super(0.0F, 0.0F, 64, 64);
		bipedLeftLeg.showModel = false;
		bipedHeadwear.showModel = false;
		bipedRightLeg = new ModelRenderer(this, 32, 0);
		bipedRightLeg.addBox(-1.0F, -1.0F, -2.0F, 6.0F, 10.0F, 4.0F, 0.0F);
		bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
		//Bell
		//Same rotation point as right arm, calibrated in tabula
		bell = new ModelRenderer(this, 0, 32);
		bell.setRotationPoint(-5, 2, 0);
		bell.addBox(-4, 5, 2, 6, 6, 7);
	}

	@Override
	protected Iterable<ModelRenderer> getBodyParts() {
		return Iterables.concat(super.getBodyParts(), ImmutableList.of(bell));
	}

	@Override
	public void setRotationAngles(BellringerEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		bipedRightArm.rotateAngleX = (float) (-Math.PI / 2);
		if (swingProgress > 0) {
			// It's currently unmapped, but it's from func_230486_a_ to swing the arm
			float f = 1 - swingProgress;
			f = f * f;
			f = f * f;
			f = 1 - f;
			float f1 = MathHelper.sin(f * (float) Math.PI);
			float f2 = MathHelper.sin(swingProgress * (float) Math.PI) * -(bipedHead.rotateAngleX - 0.7F) * 0.75F;
			bipedRightArm.rotateAngleX -= f1 * 1.2F + f2;
		}
		bell.copyModelAngles(bipedRightArm);
	}

}
