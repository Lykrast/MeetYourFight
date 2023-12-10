package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

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
		model.setupAnim(entityIn, 0, 0, 0, f, f1);
		VertexConsumer ivertexbuilder = bufferIn.getBuffer(model.renderType(TEXTURE));

		//Copied from tnt
		int i = entityIn.getFuse();
		int overlay = OverlayTexture.NO_OVERLAY;
		if (i - partialTicks + 1 < 10) {
			float f2 = 1.0F - ((float) i - partialTicks + 1.0F) / 10.0F;
			f2 = Mth.clamp(f2, 0.0F, 1.0F);
			f2 *= f2;
			f2 *= f2;
			float f3 = 1.0F + f2*2;
			matrixStackIn.scale(f3, f3, f3);
			if (i - partialTicks + 1 < 5) overlay = OverlayTexture.pack(OverlayTexture.u(1), 10);
		}
		
		//Copied from shulker bullet
		float f2 = (float) entityIn.tickCount + partialTicks;
		matrixStackIn.translate(0, 0.15, 0);
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(Mth.sin(f2 * 0.1F) * 180.0F));
		matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(Mth.cos(f2 * 0.1F) * 180.0F));
		matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(Mth.sin(f2 * 0.15F) * 360.0F));

		model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
		matrixStackIn.popPose();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}

	@Override
	public ResourceLocation getTextureLocation(FortunaBombEntity entity) {
		return TEXTURE;
	}

}
