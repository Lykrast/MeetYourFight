package lykrast.meetyourfight.renderer;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.DameFortunaEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.util.ResourceLocation;

public class DameFortunaRenderer extends BipedRenderer<DameFortunaEntity, BipedModel<DameFortunaEntity>> {
	private static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/bellringer.png");

	public DameFortunaRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn, new BipedModel<>(RenderType::getEntityCutoutNoCull, 0, 0, 64, 64), 0.5F);
	}

	@Override
	public ResourceLocation getEntityTexture(DameFortunaEntity entity) {
		return TEXTURE;
	}

}
