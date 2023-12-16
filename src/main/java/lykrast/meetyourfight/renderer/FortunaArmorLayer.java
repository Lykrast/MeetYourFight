package lykrast.meetyourfight.renderer;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.DameFortunaEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class FortunaArmorLayer extends EnergySwirlLayer<DameFortunaEntity, DameFortunaModel> {
	private static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/dame_fortuna_armor.png");
	private final DameFortunaModel model;

	public FortunaArmorLayer(RenderLayerParent<DameFortunaEntity, DameFortunaModel> parent, EntityModelSet modelSet) {
		super(parent);
		model = new DameFortunaModel(modelSet.bakeLayer(DameFortunaModel.MODEL_ARMOR));
	}

	@Override
	protected float xOffset(float ticks) {
		//Copied and adapted from the WitherArmor
		return Mth.cos(ticks * 0.02F) * 2;
	}

	@Override
	protected ResourceLocation getTextureLocation() {
		return TEXTURE;
	}

	@Override
	protected EntityModel<DameFortunaEntity> model() {
		return model;
	}

}
