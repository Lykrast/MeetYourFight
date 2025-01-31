package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.FortunaCardEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class FortunaCardRenderer extends EntityRenderer<FortunaCardEntity> {
	private static final ResourceLocation[] TEXTURES = {
			MeetYourFight.rl("textures/entity/fortuna_card_club.png"),
			MeetYourFight.rl("textures/entity/fortuna_card_heart.png"),
			MeetYourFight.rl("textures/entity/fortuna_card_diamond.png"),
			MeetYourFight.rl("textures/entity/fortuna_card_spade.png"),
			MeetYourFight.rl("textures/entity/fortuna_card_amogus.png") //shh don't tell anyone
	};
	private static final RenderType[] TEXTURES_OVERLAY = {
			RenderType.entityTranslucentEmissive(MeetYourFight.rl("textures/entity/fortuna_card_ask_club.png"), false),
			RenderType.entityTranslucentEmissive(MeetYourFight.rl("textures/entity/fortuna_card_ask_heart.png"), false),
			RenderType.entityTranslucentEmissive(MeetYourFight.rl("textures/entity/fortuna_card_ask_diamond.png"), false),
			RenderType.entityTranslucentEmissive(MeetYourFight.rl("textures/entity/fortuna_card_ask_spade.png"), false),
			RenderType.entityTranslucentEmissive(MeetYourFight.rl("textures/entity/fortuna_card_ask_amogus.png"), false) //shh don't tell anyone
		};
	private static final ResourceLocation TEXTURE_HIDDEN = MeetYourFight.rl("textures/entity/fortuna_card_hidden.png");
	private final FortunaCardModel model;
	//So addlayer is for living entities, so uh guess I gotta readd this manually

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
		//copied that from wither projectile, cause the texture is fucking upside down and mirrored
		matrixStackIn.scale(-1.0F, -1.0F, 1.0F);
		float yaw = Mth.rotLerp(partialTicks, entityIn.yRotO, entityIn.getYRot());
		if (anim == FortunaCardEntity.ANIM_HIDE) {
			float progress = (FortunaCardEntity.ANIM_APPEAR_DUR - entityIn.animTimer + partialTicks)/(float)FortunaCardEntity.ANIM_APPEAR_DUR;
			yaw = Mth.wrapDegrees(yaw + progress*360);
		}
		else if (anim == FortunaCardEntity.ANIM_REVEAL) {
			float progress = (FortunaCardEntity.ANIM_REVEAL_DUR - entityIn.animTimer + partialTicks)/(float)FortunaCardEntity.ANIM_REVEAL_DUR;
			yaw = Mth.wrapDegrees(yaw + progress*360);
		}
		else if (anim == FortunaCardEntity.ANIM_HINT) {
			//this whole thing smells of past me, but too lazy to clean up I'm just gonna like add that here
			float progress = (FortunaCardEntity.ANIM_HINT_DUR - entityIn.animTimer + partialTicks)/(float)FortunaCardEntity.ANIM_HINT_DUR;
			yaw = Mth.wrapDegrees(yaw + Mth.sin(progress*3*Mth.PI)*30);
		}
		float pitch = Mth.lerp(partialTicks, entityIn.xRotO, entityIn.getXRot());
		matrixStackIn.translate(0, -1.5, 0);
		model.setupAnim(entityIn, 0, 0, 0, yaw, pitch);
		if (anim == FortunaCardEntity.ANIM_APPEAR) {
			float scale = (FortunaCardEntity.ANIM_APPEAR_DUR - entityIn.animTimer + partialTicks) / (float)FortunaCardEntity.ANIM_APPEAR_DUR;
			if (scale > 1) scale = 1;
			scale *= scale;
			scale *= scale;
			matrixStackIn.scale(scale, 1, scale);
		}
		VertexConsumer ivertexbuilder = bufferIn.getBuffer(model.renderType(getTextureLocation(entityIn)));
		model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		//addlayer is on the mob rendered so whoops gotta redo it hope it works
		if (entityIn.clientAnim == FortunaCardEntity.ANIM_IDLE_QUESTION) {
			ivertexbuilder = bufferIn.getBuffer(TEXTURES_OVERLAY[Mth.clamp(entityIn.getVariantQuestion(), 0, TEXTURES_OVERLAY.length-1)]);
			model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		}
		matrixStackIn.popPose();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}
	
	private static final int HIDE_HALF = FortunaCardEntity.ANIM_APPEAR_DUR / 2, REVEAL_HALF = FortunaCardEntity.ANIM_REVEAL_DUR / 2;

	@Override
	public ResourceLocation getTextureLocation(FortunaCardEntity entity) {
		int anim = entity.clientAnim;
		if (anim == FortunaCardEntity.ANIM_IDLE_HIDDEN || anim == FortunaCardEntity.ANIM_IDLE_QUESTION || (anim == FortunaCardEntity.ANIM_HIDE && entity.animTimer <= HIDE_HALF) || (anim == FortunaCardEntity.ANIM_REVEAL && entity.animTimer >= REVEAL_HALF)) return TEXTURE_HIDDEN;
		return TEXTURES[Mth.clamp(entity.getVariant(), 0, TEXTURES.length-1)];
	}

}
