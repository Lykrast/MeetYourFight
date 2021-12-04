package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ProjectileLineModel<T extends Entity> extends EntityModel<T> {
	// This is mostly the shulker bullet but copy pasted then inlined
	private final ModelRenderer renderer;

	public ProjectileLineModel() {
		super(RenderType::entityCutoutNoCull);
		texWidth = 64;
		texHeight = 32;
		renderer = new ModelRenderer(this);
		renderer.texOffs(0, 0).addBox(-4, -4, -4, 8, 8, 8, 0);
		renderer.setPos(0, 0, 0);
	}

	@Override
	public void renderToBuffer(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		renderer.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}

	@Override
	public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		//TODO great it's backwards when facing any axis except one of the horizontal ones
		renderer.yRot = netHeadYaw * ((float) Math.PI / 180);
		renderer.xRot = headPitch * ((float) Math.PI / 180);
	}

}
