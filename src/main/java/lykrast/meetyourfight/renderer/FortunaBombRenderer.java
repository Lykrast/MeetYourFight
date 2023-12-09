package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.FortunaBombEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class FortunaBombRenderer extends EntityRenderer<FortunaBombEntity> {
	private static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/fortuna_dice.png");
	private final FortunaBombModel<FortunaBombEntity> model;

	public FortunaBombRenderer(Context context) {
		super(context);
		model = new FortunaBombModel<>(context.bakeLayer(FortunaBombModel.MODEL));
	}

	@Override
	protected int getBlockLightLevel(FortunaBombEntity entityIn, BlockPos partialTicks) {
		return 15;
	}

	@Override
	public void render(FortunaBombEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		matrixStackIn.pushPose();
		float f = Mth.rotLerp(partialTicks, entityIn.yRotO, entityIn.getYRot());
		float f1 = Mth.lerp(partialTicks, entityIn.xRotO, entityIn.getXRot());
		//matrixStackIn.translate(0, 0.15, 0);
		model.setupAnim(entityIn, 0, 0, 0, f, f1);
		VertexConsumer ivertexbuilder = bufferIn.getBuffer(model.renderType(TEXTURE));

		//Copied from tnt
		int i = entityIn.getFuse();
		if ((float) i - partialTicks + 1.0F < 10.0F) {
			float f2 = 1.0F - ((float) i - partialTicks + 1.0F) / 10.0F;
			f2 = Mth.clamp(f2, 0.0F, 1.0F);
			f2 *= f2;
			f2 *= f2;
			float f3 = 1.0F + f2*2;
			matrixStackIn.scale(f3, f3, f3);
		}

		model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		matrixStackIn.popPose();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}

	@Override
	public ResourceLocation getTextureLocation(FortunaBombEntity entity) {
		return TEXTURE;
	}

}
