package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.FortunaCardEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class FortunaCardRenderer extends EntityRenderer<FortunaCardEntity> {
	private static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/fortuna_card_circle.png");
	private static final ResourceLocation TEXTURE_HIDDEN = MeetYourFight.rl("textures/entity/fortuna_card_hidden.png");
	private final FortunaCardModel model;

	public FortunaCardRenderer(Context context) {
		super(context);
		model = new FortunaCardModel(context.bakeLayer(FortunaCardModel.MODEL));
	}

	@Override
	protected int getBlockLightLevel(FortunaCardEntity entityIn, BlockPos partialTicks) {
		return 15;
	}

	@Override
	public void render(FortunaCardEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		int anim = entityIn.clientAnim;
		if (anim == FortunaCardEntity.ANIM_NOTHERE) return;
		matrixStackIn.pushPose();
		float yaw = Mth.rotLerp(partialTicks, entityIn.yRotO, entityIn.getYRot());
		if (anim == FortunaCardEntity.ANIM_HIDE) {
			float progress = (FortunaCardEntity.ANIM_APPEAR_DUR - entityIn.animTimer + partialTicks)/(float)FortunaCardEntity.ANIM_APPEAR_DUR;
			yaw = Mth.wrapDegrees(yaw + progress*360);
		}
		float pitch = Mth.lerp(partialTicks, entityIn.xRotO, entityIn.getXRot());
		matrixStackIn.translate(0, 1, 0);
		model.setupAnim(entityIn, 0, 0, 0, yaw, pitch);
		VertexConsumer ivertexbuilder = bufferIn.getBuffer(model.renderType(getTextureLocation(entityIn)));
		if (anim == FortunaCardEntity.ANIM_APPEAR) {
			float scale = (FortunaCardEntity.ANIM_APPEAR_DUR - entityIn.animTimer + partialTicks) / (float)FortunaCardEntity.ANIM_APPEAR_DUR;
			if (scale > 1) scale = 1;
			scale *= scale;
			scale *= scale;
			matrixStackIn.scale(scale, 1, scale);
		}
		model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		matrixStackIn.popPose();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}
	
	private static final int HIDE_HALF = FortunaCardEntity.ANIM_APPEAR_DUR / 2;

	@Override
	public ResourceLocation getTextureLocation(FortunaCardEntity entity) {
		int anim = entity.clientAnim;
		if (anim == FortunaCardEntity.ANIM_IDLE_HIDDEN || (anim == FortunaCardEntity.ANIM_HIDE && entity.animTimer <= HIDE_HALF)) return TEXTURE_HIDDEN;
		return TEXTURE;
	}

}
