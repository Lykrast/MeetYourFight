package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.DameFortunaEntity;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class DameFortunaRenderer extends BipedRenderer<DameFortunaEntity, DameFortunaModel> {
	private static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/dame_fortuna.png");

	public DameFortunaRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn, new DameFortunaModel(), 0.5F);
	}
	
	@Override
	protected void applyRotations(DameFortunaEntity entity, MatrixStack stack, float ageInTicks, float rotationYaw, float partialTicks) {
		//Copied/changed this bit from the Mourned from Defiled Lands, that I think copied from endermen
		//(and it don't want to show me the endermen code)
		int rage = entity.getRage();
		if (rage >= 1) {
			rotationYaw += (float)(Math.cos((ageInTicks + partialTicks) * 3.25) * Math.PI * rage);
		}
		super.applyRotations(entity, stack, ageInTicks, rotationYaw, partialTicks);
	}

	@Override
	public ResourceLocation getEntityTexture(DameFortunaEntity entity) {
		return TEXTURE;
	}

}
