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
	private static final ResourceLocation[] TEXTURES = {
			MeetYourFight.rl("textures/entity/projectile_bellringer.png"),
			MeetYourFight.rl("textures/entity/projectile_dame_fortuna.png")
			};
	private static final RenderType[] OVERLAYS;
	static {
		OVERLAYS = new RenderType[TEXTURES.length];
		for (int i = 0; i < OVERLAYS.length; i++) OVERLAYS[i] = RenderType.getEntityTranslucent(TEXTURES[i]);
	}
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
		//So I just made the texture not have any direction
		matrixStackIn.push();
		float f = MathHelper.interpolateAngle(partialTicks, entityIn.prevRotationYaw, entityIn.rotationYaw);
		float f1 = MathHelper.lerp(partialTicks, entityIn.prevRotationPitch, entityIn.rotationPitch);
		matrixStackIn.translate(0, 0.15, 0);
		model.setRotationAngles(entityIn, 0, 0, 0, f, f1);
		IVertexBuilder ivertexbuilder = bufferIn.getBuffer(model.getRenderType(TEXTURES[clampVariant(entityIn)]));
		model.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		matrixStackIn.scale(1.5F, 1.5F, 1.5F);
		IVertexBuilder ivertexbuilder1 = bufferIn.getBuffer(OVERLAYS[clampVariant(entityIn)]);
		model.render(matrixStackIn, ivertexbuilder1, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 0.15F);
		matrixStackIn.pop();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}

	@Override
	public ResourceLocation getEntityTexture(ProjectileLineEntity entity) {
		return TEXTURES[clampVariant(entity)];
	}
	
	private int clampVariant(ProjectileLineEntity entity) {
		return MathHelper.clamp(entity.getVariant(), 0, TEXTURES.length);
	}

}
