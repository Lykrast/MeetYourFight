package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.RosalyneEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class RosalyneGlowLayer extends RenderLayer<RosalyneEntity, RosalyneModel> {
	// I need to copy paste the render method anyway so that it's aware of Rosalyne's phases, so no need to extend the actual EyesLayer
	private static final RenderType COFFIN_GLOW = RenderType.eyes(MeetYourFight.rl("textures/entity/rosalyne_coffin_glow.png")), 
			BASE_GLOW = RenderType.eyes(MeetYourFight.rl("textures/entity/rosalyne_glow.png"));

	public RosalyneGlowLayer(RenderLayerParent<RosalyneEntity, RosalyneModel> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int p_117351_, RosalyneEntity entity, float p_117353_, float p_117354_, float p_117355_, float p_117356_, float p_117357_, float p_117358_) {
		int phase = entity.getPhase();
		//Copied from EyesLayer, so lots of numbers I don't know what they mean
		VertexConsumer vertexconsumer = buffer.getBuffer((phase == RosalyneEntity.ENCASED || phase == RosalyneEntity.BREAKING_OUT) ? COFFIN_GLOW : BASE_GLOW);
		getParentModel().renderToBuffer(poseStack, vertexconsumer, 15728640, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
	}

}
