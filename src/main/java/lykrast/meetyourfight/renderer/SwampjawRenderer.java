package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;

import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.PhantomEyesLayer;
import net.minecraft.client.renderer.entity.model.PhantomModel;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class SwampjawRenderer extends MobRenderer<SwampjawEntity, PhantomModel<SwampjawEntity>> {
	private static final ResourceLocation PHANTOM_LOCATION = new ResourceLocation("textures/entity/phantom.png");

	public SwampjawRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn, new PhantomModel<>(), 0.75F);
		this.addLayer(new PhantomEyesLayer<>(this));
	}

	@Override
	public ResourceLocation getEntityTexture(SwampjawEntity entity) {
		return PHANTOM_LOCATION;
	}

	@Override
	protected void preRenderCallback(SwampjawEntity entitylivingbaseIn, MatrixStack matrixStackIn, float partialTickTime) {
		matrixStackIn.translate(0.0D, 1.3125D, 0.1875D);
	}

	@Override
	protected void applyRotations(SwampjawEntity entityLiving, MatrixStack matrixStackIn, float ageInTicks, float rotationYaw, float partialTicks) {
		super.applyRotations(entityLiving, matrixStackIn, ageInTicks, rotationYaw, partialTicks);
		matrixStackIn.rotate(Vector3f.XP.rotationDegrees(entityLiving.rotationPitch));
	}

}
