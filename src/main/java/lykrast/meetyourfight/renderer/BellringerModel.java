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
		super(0, 0, 64, 64);
		leftLeg.visible = false;
		hat.visible = false;
		rightLeg = new ModelRenderer(this, 32, 0);
		rightLeg.addBox(-1.0F, -1.0F, -2.0F, 6.0F, 10.0F, 4.0F, 0.0F);
		rightLeg.setPos(-1.9F, 12.0F, 0.0F);
		//Bell
		//Same rotation point as right arm, calibrated in tabula
		bell = new ModelRenderer(this, 0, 32);
		bell.setPos(-5, 2, 0);
		bell.addBox(-4, 5, 2, 6, 6, 7);
	}

	@Override
	protected Iterable<ModelRenderer> bodyParts() {
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
			float f1 = MathHelper.sin(f * (float) Math.PI);
			float f2 = MathHelper.sin(attackTime * (float) Math.PI) * -(head.xRot - 0.7F) * 0.75F;
			rightArm.xRot -= f1 * 1.2F + f2;
		}
		bell.copyFrom(rightArm);
	}

}
