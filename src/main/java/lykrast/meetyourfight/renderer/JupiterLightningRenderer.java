package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import lykrast.meetyourfight.entity.JupiterLightningEntity;
import net.minecraft.client.model.EvokerFangsModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class JupiterLightningRenderer extends EntityRenderer<JupiterLightningEntity> {
	//TODO for now just copied evoker fangs
	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/illager/evoker_fangs.png");//MeetYourFight.rl("textures/entity/swampmine.png");
	private final EvokerFangsModel<JupiterLightningEntity> model;

	public JupiterLightningRenderer(Context context) {
		super(context);
		model = new EvokerFangsModel<>(context.bakeLayer(ModelLayers.EVOKER_FANGS));
	}

	@Override
	public void render(JupiterLightningEntity entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
		float f = entity.getAnimationProgress(partialTicks);
		if (f != 0.0F) {
			float f1 = 2.0F;
			if (f > 0.9F) {
				f1 *= (1.0F - f) / 0.1F;
			}

			matrixStack.pushPose();
			matrixStack.mulPose(Axis.YP.rotationDegrees(90.0F - entity.getYRot()));
			matrixStack.scale(-f1, -f1, f1);
			matrixStack.translate(0.0D, -0.626D, 0.0D);
			matrixStack.scale(0.5F, 0.5F, 0.5F);
			this.model.setupAnim(entity, f, 0.0F, 0.0F, entity.getYRot(), entity.getXRot());
			VertexConsumer vertexconsumer = buffer.getBuffer(this.model.renderType(TEXTURE));
			this.model.renderToBuffer(matrixStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			matrixStack.popPose();
			super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
		}
	}

	@Override
	public ResourceLocation getTextureLocation(JupiterLightningEntity entity) {
		return TEXTURE;
	}

}
