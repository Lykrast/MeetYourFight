package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.DameFortunaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class DameFortunaRenderer extends HumanoidMobRenderer<DameFortunaEntity, DameFortunaModel> {
	private static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/dame_fortuna.png"), GLOW = MeetYourFight.rl("textures/entity/dame_fortuna_glow.png");

	public DameFortunaRenderer(Context context) {
		super(context, new DameFortunaModel(context.bakeLayer(DameFortunaModel.MODEL)), 0.5F);
		addLayer(new GenericGlowLayer<>(this, GLOW));
		addLayer(new FortunaArmorLayer(this, context.getModelSet()));
	}
	
	@Override
	protected void setupRotations(DameFortunaEntity entity, PoseStack stack, float ageInTicks, float rotationYaw, float partialTicks) {
		rotationYaw = Mth.wrapDegrees(rotationYaw + entity.getSpinAngle(partialTicks));
		super.setupRotations(entity, stack, ageInTicks, rotationYaw, partialTicks);
	}

	@Override
	public ResourceLocation getTextureLocation(DameFortunaEntity entity) {
		return TEXTURE;
	}

}
