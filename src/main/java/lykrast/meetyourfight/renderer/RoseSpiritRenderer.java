package lykrast.meetyourfight.renderer;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.RoseSpiritEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class RoseSpiritRenderer extends MobRenderer<RoseSpiritEntity, RoseSpiritModel> {
	private static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/rose_spirit.png");
	//Copied from Ender Dragon
	private static final ResourceLocation BEAM_TEXTURE = new ResourceLocation("textures/entity/end_crystal/end_crystal_beam.png");
	private static final RenderType BEAM = RenderType.entitySmoothCutout(BEAM_TEXTURE);

	public RoseSpiritRenderer(Context context) {
		super(context, new RoseSpiritModel(context.bakeLayer(RoseSpiritModel.MODEL)), 0.5F);
		addLayer(new RoseSpiritGlowLayer(this));
	}
	@Override
	public void render(RoseSpiritEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		//copied from end crystals
		LivingEntity owner = entityIn.getOwner();
		if (owner != null) {
			float f3 = (float) owner.getX();
			float f4 = (float) owner.getY();
			float f5 = (float) owner.getZ();
			float f6 = (float) (f3 - entityIn.getX());
			float f7 = (float) (f4 - entityIn.getY());
			float f8 = (float) (f5 - entityIn.getZ());
			renderCrystalBeams(f6, f7, f8, partialTicks, entityIn.tickCount, matrixStackIn, bufferIn, packedLightIn);
		}
	}

	//Copied then modified from Ender Dragon
	public static void renderCrystalBeams(float x, float y, float z, float partialTicks, int ticks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		float dist2D = Mth.sqrt(x * x + z * z);
		float dist = Mth.sqrt(x * x + y * y + z * z);
		matrixStackIn.pushPose();
		matrixStackIn.translate(0, 1, 0);
		matrixStackIn.mulPose(Axis.YP.rotation((float) (-Math.atan2(z, x) - Math.PI / 2F)));
		matrixStackIn.mulPose(Axis.XP.rotation((float) (-Math.atan2(dist2D, y) - Math.PI / 2F)));
		VertexConsumer vertexconsumer = bufferIn.getBuffer(BEAM);
		float f2 = 0.0F - ((float) ticks + partialTicks) * 0.01F;
		float f3 = Mth.sqrt(x * x + y * y + z * z) / 32.0F - ((float) ticks + partialTicks) * 0.01F;
		float f4 = 0.0F;
		float f5 = 0.75F;
		float f6 = 0.0F;
		PoseStack.Pose posestack$pose = matrixStackIn.last();
		Matrix4f matrix4f = posestack$pose.pose();
		Matrix3f matrix3f = posestack$pose.normal();
		float endScale = 0.3f;
		float startScale = 0.2f;

		for (int j = 1; j <= 8; ++j) {
			float f7 = Mth.sin((float) (j * (Math.PI * 2F) / 8F)) * 0.75F;
			float f8 = Mth.cos((float) (j * (Math.PI * 2F) / 8F)) * 0.75F;
			float f9 = j / 8F;
			vertexconsumer.vertex(matrix4f, f4 * startScale, f5 * startScale, 0.0F).color(205, 112, 255, 255).uv(f6, f2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLightIn).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
			vertexconsumer.vertex(matrix4f, f4 * endScale, f5 * endScale, dist).color(0, 0, 0, 255).uv(f6, f3).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLightIn).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
			vertexconsumer.vertex(matrix4f, f7 * endScale, f8 * endScale, dist).color(0, 0, 0, 255).uv(f9, f3).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLightIn).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
			vertexconsumer.vertex(matrix4f, f7 * startScale, f8 * startScale, 0.0F).color(205, 112, 255, 255).uv(f9, f2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLightIn).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
			f4 = f7;
			f5 = f8;
			f6 = f9;
		}

		matrixStackIn.popPose();
	}

	@Override
	public ResourceLocation getTextureLocation(RoseSpiritEntity entity) {
		return TEXTURE;
	}

	@Override
	public boolean shouldRender(RoseSpiritEntity entity, Frustum frustrum, double p_114171_, double p_114172_, double p_114173_) {
		//Copied from End Crystals, should do the trick since I don't expect too many of them to be in world without a rosalyne
		return super.shouldRender(entity, frustrum, p_114171_, p_114172_, p_114173_) || entity.getOwner() != null;
	}

}
