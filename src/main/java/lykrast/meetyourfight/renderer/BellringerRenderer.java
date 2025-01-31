package lykrast.meetyourfight.renderer;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.BellringerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BellringerRenderer extends HumanoidMobRenderer<BellringerEntity, BellringerModel> {
	public static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/bellringer.png"), GLOW = MeetYourFight.rl("textures/entity/bellringer_glow.png");

	public BellringerRenderer(Context context) {
		super(context, new BellringerModel(context.bakeLayer(BellringerModel.MODEL)), 0.5F);
		addLayer(new GenericGlowLayer<>(this, GLOW));
	}

	@Override
	public ResourceLocation getTextureLocation(BellringerEntity entity) {
		return TEXTURE;
	}

}
