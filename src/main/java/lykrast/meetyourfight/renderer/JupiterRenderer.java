package lykrast.meetyourfight.renderer;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.JupiterEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class JupiterRenderer extends MobRenderer<JupiterEntity, JupiterModel> {
	public static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/jupiter.png"), GLOW = MeetYourFight.rl("textures/entity/jupiter_glow.png");

	public JupiterRenderer(Context context) {
		super(context, new JupiterModel(context.bakeLayer(JupiterModel.MODEL)), 0.5F);
		addLayer(new GenericGlowLayer<>(this, GLOW));
	}

	@Override
	public ResourceLocation getTextureLocation(JupiterEntity entity) {
		return TEXTURE;
	}

}
