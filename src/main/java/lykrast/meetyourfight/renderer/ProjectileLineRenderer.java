package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.ProjectileLineEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ProjectileLineRenderer extends EntityRenderer<ProjectileLineEntity> {
	private static final ResourceLocation[] TEXTURES = {
			MeetYourFight.rl("textures/entity/projectile_bellringer.png"),
			MeetYourFight.rl("textures/entity/projectile_dame_fortuna.png"),
			MeetYourFight.rl("textures/entity/projectile_rose.png")
			};
	private static final RenderType[] OVERLAYS;
	static {
		OVERLAYS = new RenderType[TEXTURES.length];
		for (int i = 0; i < OVERLAYS.length; i++) OVERLAYS[i] = RenderType.entityTranslucent(TEXTURES[i]);
	}
	private final ProjectileLineModel<ProjectileLineEntity> model;

	public ProjectileLineRenderer(Context context) {
		super(context);
		model = new ProjectileLineModel<>(context.bakeLayer(ProjectileLineModel.MODEL));
	}

	@Override
	protected int getBlockLightLevel(ProjectileLineEntity entityIn, BlockPos partialTicks) {
		return 15;
	}

	@Override
	public void render(ProjectileLineEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		//TODO great it's backwards when facing any axis except one of the horizontal ones
		//So I just made the texture not have any direction
		matrixStackIn.pushPose();
		float f = Mth.rotLerp(partialTicks, entityIn.yRotO, entityIn.getYRot());
		float f1 = Mth.lerp(partialTicks, entityIn.xRotO, entityIn.getXRot());
		matrixStackIn.translate(0, 0.15, 0);
		model.setupAnim(entityIn, 0, 0, 0, f, f1);
		VertexConsumer ivertexbuilder = bufferIn.getBuffer(model.renderType(TEXTURES[clampVariant(entityIn)]));
		model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		matrixStackIn.scale(1.5F, 1.5F, 1.5F);
		VertexConsumer ivertexbuilder1 = bufferIn.getBuffer(OVERLAYS[clampVariant(entityIn)]);
		model.renderToBuffer(matrixStackIn, ivertexbuilder1, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 0.15F);
		matrixStackIn.popPose();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}

	@Override
	public ResourceLocation getTextureLocation(ProjectileLineEntity entity) {
		return TEXTURES[clampVariant(entity)];
	}
	
	private int clampVariant(ProjectileLineEntity entity) {
		return Mth.clamp(entity.getVariant(), 0, TEXTURES.length);
	}

}
