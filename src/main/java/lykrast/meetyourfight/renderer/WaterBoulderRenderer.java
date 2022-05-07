package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.WaterBoulderEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class WaterBoulderRenderer extends EntityRenderer<WaterBoulderEntity> {
	private static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/water_boulder.png");
	private final EntityModel<WaterBoulderEntity> model;

	public WaterBoulderRenderer(Context context) {
		super(context);
		model = new WaterBoulderModel(context.bakeLayer(WaterBoulderModel.MODEL));
		shadowRadius = 0.5F;
	}

	@Override
	public void render(WaterBoulderEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		matrixStackIn.pushPose();
		float f = Mth.rotLerp(partialTicks, entityIn.yRotO, entityIn.getYRot());
		float f1 = Mth.lerp(partialTicks, entityIn.xRotO, entityIn.getXRot());
		float scale = 3;
		if (entityIn.tickCount < 40) scale = (entityIn.tickCount + partialTicks)*3 / 40F;
		matrixStackIn.scale(scale, scale, scale);
		matrixStackIn.translate(0, -0.5, 0);
		model.setupAnim(entityIn, 0, 0, 0, f, f1);
		VertexConsumer ivertexbuilder = bufferIn.getBuffer(model.renderType(TEXTURE));
		model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		matrixStackIn.popPose();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}

	@Override
	public ResourceLocation getTextureLocation(WaterBoulderEntity entity) {
		return TEXTURE;
	}

}
