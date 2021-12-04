package lykrast.meetyourfight.renderer;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.BellringerEntity;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

public class BellringerRenderer extends HumanoidMobRenderer<BellringerEntity, BellringerModel> {
	private static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/bellringer.png");

	public BellringerRenderer(EntityRenderDispatcher renderManagerIn) {
		super(renderManagerIn, new BellringerModel(), 0.5F);
	}

	@Override
	protected int getBlockLightLevel(BellringerEntity entityIn, BlockPos partialTicks) {
		return 15;
	}

	@Override
	public ResourceLocation getTextureLocation(BellringerEntity entity) {
		return TEXTURE;
	}

}
