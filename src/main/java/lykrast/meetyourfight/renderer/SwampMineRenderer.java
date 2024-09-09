package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.SwampMineEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SwampMineRenderer extends EntityRenderer<SwampMineEntity> {
	private static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/swampmine.png");
	private final EntityModel<SwampMineEntity> model;

	public SwampMineRenderer(Context context) {
		super(context);
		model = new SwampMineModel(context.bakeLayer(SwampMineModel.MODEL));
		shadowRadius = 0.5F;
	}

	@Override
	public void render(SwampMineEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		matrixStackIn.pushPose();
		float f = Mth.rotLerp(partialTicks, entityIn.yRotO, entityIn.getYRot());
		float f1 = Mth.lerp(partialTicks, entityIn.xRotO, entityIn.getXRot());
		matrixStackIn.translate(0, -0.5, 0);
		//From TNT rendering
		int i = entityIn.getFuse();
		if ((float) i - partialTicks + 1.0F < 10.0F) {
			float interp = 1.0F - ((float) i - partialTicks + 1.0F) / 10.0F;
			interp = Mth.clamp(interp, 0.0F, 1.0F);
			interp *= interp;
			interp *= interp;
			float finalscale = 1 + interp * 0.3F;
			matrixStackIn.scale(finalscale, finalscale, finalscale);
		}
		model.setupAnim(entityIn, 0, 0, 0, f, f1);
		VertexConsumer ivertexbuilder = bufferIn.getBuffer(model.renderType(TEXTURE));
		//From TNT rendering
		int overlay = entityIn.tickCount / 5 % 2 == 0 ? OverlayTexture.pack(OverlayTexture.u(1.0F), 10) : OverlayTexture.NO_OVERLAY;
		model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
		matrixStackIn.popPose();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}

	@Override
	public ResourceLocation getTextureLocation(SwampMineEntity entity) {
		return TEXTURE;
	}

}
