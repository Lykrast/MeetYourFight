package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.DameFortunaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DameFortunaRenderer extends HumanoidMobRenderer<DameFortunaEntity, DameFortunaModel> {
	private static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/dame_fortuna.png"), GLOW = MeetYourFight.rl("textures/entity/dame_fortuna_glow.png");

	public DameFortunaRenderer(Context context) {
		super(context, new DameFortunaModel(context.bakeLayer(DameFortunaModel.MODEL)), 0.5F);
		addLayer(new GenericGlowLayer<>(this, GLOW));
		addLayer(new FortunaArmorLayer(this, context.getModelSet()));
	}
	
	@Override
	protected void setupRotations(DameFortunaEntity entity, PoseStack stack, float ageInTicks, float rotationYaw, float partialTicks) {
		//TODO change
		//Copied/changed this bit from the Mourned from Defiled Lands, that I think copied from endermen
		//(and it don't want to show me the endermen code)
		int rage = entity.getPhase();
		if (rage >= 1) {
			rotationYaw += (float)(Math.cos((ageInTicks + partialTicks) * 3.25) * Math.PI * rage);
		}
		super.setupRotations(entity, stack, ageInTicks, rotationYaw, partialTicks);
	}

	@Override
	public ResourceLocation getTextureLocation(DameFortunaEntity entity) {
		return TEXTURE;
	}

}
