package lykrast.meetyourfight.renderer;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.RosalyneEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RosalyneRenderer extends HumanoidMobRenderer<RosalyneEntity, RosalyneModel> {
	//TODO PLACEHOLDER
	private static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/rosalyne.png");

	public RosalyneRenderer(Context context) {
		super(context, new RosalyneModel(context.bakeLayer(RosalyneModel.MODEL)), 0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(RosalyneEntity entity) {
		return TEXTURE;
	}

}
