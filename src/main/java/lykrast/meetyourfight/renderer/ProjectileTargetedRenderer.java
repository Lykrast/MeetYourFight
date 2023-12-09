package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.ProjectileTargetedEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ProjectileTargetedRenderer extends EntityRenderer<ProjectileTargetedEntity> {
	private static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/fortuna_chips.png");
	private final ProjectileChipsModel<ProjectileTargetedEntity> model;

	public ProjectileTargetedRenderer(Context context) {
		super(context);
		model = new ProjectileChipsModel<>(context.bakeLayer(ProjectileChipsModel.MODEL));
	}

	@Override
	protected int getBlockLightLevel(ProjectileTargetedEntity entityIn, BlockPos partialTicks) {
		return 15;
	}

	@Override
	public void render(ProjectileTargetedEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		matrixStackIn.pushPose();
		float f = Mth.rotLerp(partialTicks, entityIn.yRotO, entityIn.getYRot());
		float f1 = Mth.lerp(partialTicks, entityIn.xRotO, entityIn.getXRot());
		//matrixStackIn.translate(0, 0.15, 0);
		model.setupAnim(entityIn, 0, 0, 0, f, f1);
		VertexConsumer ivertexbuilder = bufferIn.getBuffer(model.renderType(TEXTURE));
		model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		matrixStackIn.popPose();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}

	@Override
	public ResourceLocation getTextureLocation(ProjectileTargetedEntity entity) {
		return TEXTURE;
	}

}
