package lykrast.meetyourfight.renderer;

import lykrast.meetyourfight.entity.BellringerEntity;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class BellringerRenderer extends BipedRenderer<BellringerEntity, BipedModel<BellringerEntity>> {
	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/steve.png");

	public BellringerRenderer(EntityRendererManager renderManagerIn) {
	      super(renderManagerIn, new BipedModel<>(1), 0.5F);
	   }

	@Override
	protected int getBlockLight(BellringerEntity entityIn, BlockPos partialTicks) {
		return 15;
	}

	@Override
	public ResourceLocation getEntityTexture(BellringerEntity entity) {
		return TEXTURE;
	}

}
