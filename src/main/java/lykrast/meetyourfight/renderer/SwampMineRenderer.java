package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.SwampMineEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class SwampMineRenderer extends EntityRenderer<SwampMineEntity> {
	private static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/swampmine.png");
	private final EntityModel<SwampMineEntity> model = new SwampMineModel();

	public SwampMineRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
		shadowSize = 0.5F;
	}

	@Override
	public void render(SwampMineEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		matrixStackIn.push();
		float f = MathHelper.interpolateAngle(partialTicks, entityIn.prevRotationYaw, entityIn.rotationYaw);
		float f1 = MathHelper.lerp(partialTicks, entityIn.prevRotationPitch, entityIn.rotationPitch);
		matrixStackIn.translate(0, -0.5, 0);
		model.setRotationAngles(entityIn, 0, 0, 0, f, f1);
		IVertexBuilder ivertexbuilder = bufferIn.getBuffer(model.getRenderType(TEXTURE));
		//From TNT rendering
		int overlay = entityIn.ticksExisted / 5 % 2 == 0 ? OverlayTexture.getPackedUV(OverlayTexture.getU(1.0F), 10) : OverlayTexture.NO_OVERLAY;
		model.render(matrixStackIn, ivertexbuilder, packedLightIn, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
		matrixStackIn.pop();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}

	@Override
	public ResourceLocation getEntityTexture(SwampMineEntity entity) {
		return TEXTURE;
	}

}
