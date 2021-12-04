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
		shadowRadius = 0.5F;
	}

	@Override
	public void render(SwampMineEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		matrixStackIn.pushPose();
		float f = MathHelper.rotLerp(partialTicks, entityIn.yRotO, entityIn.yRot);
		float f1 = MathHelper.lerp(partialTicks, entityIn.xRotO, entityIn.xRot);
		matrixStackIn.translate(0, -0.5, 0);
		model.setupAnim(entityIn, 0, 0, 0, f, f1);
		IVertexBuilder ivertexbuilder = bufferIn.getBuffer(model.renderType(TEXTURE));
		//From TNT rendering
		int overlay = entityIn.tickCount / 5 % 2 == 0 ? OverlayTexture.pack(OverlayTexture.u(1.0F), 10) : OverlayTexture.NO_OVERLAY;
		model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
		matrixStackIn.popPose();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}

	@Override
	public ResourceLocation getTextureLocation(SwampMineEntity entity) {
		return TEXTURE;
	}

}
