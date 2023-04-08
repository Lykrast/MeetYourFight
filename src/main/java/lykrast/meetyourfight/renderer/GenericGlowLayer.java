package lykrast.meetyourfight.renderer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class GenericGlowLayer<T extends Entity, M extends EntityModel<T>> extends EyesLayer<T, M> {
	private final RenderType TYPE;

	public GenericGlowLayer(RenderLayerParent<T, M> parent, ResourceLocation texture) {
		super(parent);
		TYPE = RenderType.entityTranslucentEmissive(texture, false);
		//I the emissive one requires actual transparency but I think it looks better, cause eyes makes it too bright
		//TYPE = RenderType.eyes(texture);
	}

	@Override
	public RenderType renderType() {
		return TYPE;
	}

}
