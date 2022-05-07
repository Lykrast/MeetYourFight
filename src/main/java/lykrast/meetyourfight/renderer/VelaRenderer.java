package lykrast.meetyourfight.renderer;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.VelaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class VelaRenderer extends HumanoidMobRenderer<VelaEntity, VelaModel> {
	//TODO PLACEHOLDER
	private static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/vela.png");

	public VelaRenderer(Context context) {
		super(context, new VelaModel(context.bakeLayer(VelaModel.MODEL)), 0.5F);
	}

	@Override
	protected int getBlockLightLevel(VelaEntity entityIn, BlockPos partialTicks) {
		return 15;
	}

	@Override
	public ResourceLocation getTextureLocation(VelaEntity entity) {
		return TEXTURE;
	}

}
