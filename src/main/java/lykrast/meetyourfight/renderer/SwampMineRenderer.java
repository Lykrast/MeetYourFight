package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;

import lykrast.meetyourfight.entity.SwampMineEntity;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.TNTMinecartRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class SwampMineRenderer extends EntityRenderer<SwampMineEntity> {
	// TODO For now it's just copy pasted TNT

	public SwampMineRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
		this.shadowSize = 0.5F;
	}

	@Override
	public void render(SwampMineEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		matrixStackIn.push();
		matrixStackIn.translate(0.0D, 0.5D, 0.0D);

		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-90.0F));
		matrixStackIn.translate(-0.5D, -0.5D, 0.5D);
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(90.0F));
		TNTMinecartRenderer.renderTntFlash(Blocks.TNT.getDefaultState(), matrixStackIn, bufferIn, packedLightIn, false);
		matrixStackIn.pop();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}

	@Override
	public ResourceLocation getEntityTexture(SwampMineEntity entity) {
		return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
	}

}
