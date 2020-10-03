package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.ProjectileLineEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class ProjectileLineRenderer extends EntityRenderer<ProjectileLineEntity> {
	//Right now it's just the shulker bullet render copy pasted
	private static final ResourceLocation SHULKER_SPARK_TEXTURE = MeetYourFight.rl("textures/entity/ghost.png");
	private static final RenderType field_229123_e_ = RenderType.getEntityTranslucent(SHULKER_SPARK_TEXTURE);
	private final ProjectileLineModel<ProjectileLineEntity> model = new ProjectileLineModel<>();

	public ProjectileLineRenderer(EntityRendererManager renderManager) {
		super(renderManager);
	}

	@Override
	protected int getBlockLight(ProjectileLineEntity entityIn, BlockPos partialTicks) {
		return 15;
	}

	@Override
	public void render(ProjectileLineEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		//TODO great it's backwards when facing any axis except one of the horizontal ones
		matrixStackIn.push();
		float f = MathHelper.interpolateAngle(partialTicks, entityIn.prevRotationYaw, entityIn.rotationYaw);
		float f1 = MathHelper.lerp(partialTicks, entityIn.prevRotationPitch, entityIn.rotationPitch);
		matrixStackIn.translate(0, 0.15, 0);
		model.setRotationAngles(entityIn, 0, 0, 0, f, f1);
		IVertexBuilder ivertexbuilder = bufferIn.getBuffer(model.getRenderType(SHULKER_SPARK_TEXTURE));
		model.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		matrixStackIn.scale(1.5F, 1.5F, 1.5F);
		IVertexBuilder ivertexbuilder1 = bufferIn.getBuffer(field_229123_e_);
		model.render(matrixStackIn, ivertexbuilder1, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 0.15F);
		matrixStackIn.pop();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}

	@Override
	public ResourceLocation getEntityTexture(ProjectileLineEntity entity) {
		return SHULKER_SPARK_TEXTURE;
	}

}
