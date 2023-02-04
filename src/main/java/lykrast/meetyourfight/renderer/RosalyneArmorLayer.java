package lykrast.meetyourfight.renderer;

import lykrast.meetyourfight.entity.RosalyneEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class RosalyneArmorLayer extends EnergySwirlLayer<RosalyneEntity, RosalyneModel> {
	//TODO texture
	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/wither/wither_armor.png");
	//private static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/rosalyne.png");
	private final RosalyneModel model;

	public RosalyneArmorLayer(RenderLayerParent<RosalyneEntity, RosalyneModel> parent, EntityModelSet modelSet) {
		super(parent);
		model = new RosalyneModel(modelSet.bakeLayer(RosalyneModel.MODEL_ARMOR));
	}

	@Override
	protected float xOffset(float ticks) {
		//Copied from the WitherArmor
		return Mth.cos(ticks * 0.02F) * 3;
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
