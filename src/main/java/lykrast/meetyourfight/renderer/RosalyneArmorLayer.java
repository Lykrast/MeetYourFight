package lykrast.meetyourfight.renderer;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.RosalyneEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class RosalyneArmorLayer extends EnergySwirlLayer<RosalyneEntity, RosalyneModel> {
	private static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/rosalyne_armor.png");
	private final RosalyneModel model;

	public RosalyneArmorLayer(RenderLayerParent<RosalyneEntity, RosalyneModel> parent, EntityModelSet modelSet) {
		super(parent);
		model = new RosalyneModel(modelSet.bakeLayer(RosalyneModel.MODEL_ARMOR));
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
	protected EntityModel<RosalyneEntity> model() {
		return model;
	}

}
