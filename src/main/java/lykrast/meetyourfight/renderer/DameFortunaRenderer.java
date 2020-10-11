package lykrast.meetyourfight.renderer;

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
	public ResourceLocation getEntityTexture(DameFortunaEntity entity) {
		return TEXTURE;
	}

}
