package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.VelaVortexEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class VelaVortexRenderer extends EntityRenderer<VelaVortexEntity> {
	private static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/vortex.png");
	private final EntityModel<VelaVortexEntity> model;

	public VelaVortexRenderer(Context context) {
		super(context);
		model = new VelaVortexModel(context.bakeLayer(VelaVortexModel.MODEL));
		shadowRadius = 0.5F;
	}

	@Override
	public void render(VelaVortexEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		matrixStackIn.pushPose();
		float f = Mth.rotLerp(partialTicks, entityIn.yRotO, entityIn.getYRot());
		float f1 = Mth.lerp(partialTicks, entityIn.xRotO, entityIn.getXRot());
		float scale = 2.5f;
		if (entityIn.tickCount < VelaVortexEntity.ACTIVATION) scale = (entityIn.tickCount + partialTicks)*2.5f / VelaVortexEntity.ACTIVATION;
		matrixStackIn.scale(scale, 1, scale);
		matrixStackIn.translate(0, -1, 0);
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees((entityIn.tickCount + partialTicks) * 0.25F * 180.0F));
		model.setupAnim(entityIn, 0, 0, 0, f, f1);
		VertexConsumer ivertexbuilder = bufferIn.getBuffer(model.renderType(TEXTURE));
		model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		matrixStackIn.popPose();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}

	@Override
	public ResourceLocation getTextureLocation(VelaVortexEntity entity) {
		return TEXTURE;
	}

}
