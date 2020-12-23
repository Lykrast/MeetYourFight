package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class SwampjawRenderer extends MobRenderer<SwampjawEntity, SwampjawModel> {
	private static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/swampjaw.png");

	public SwampjawRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn, new SwampjawModel(), 0.75F);
	}

	@Override
	public ResourceLocation getEntityTexture(SwampjawEntity entity) {
		return TEXTURE;
	}

	@Override
	protected void preRenderCallback(SwampjawEntity entitylivingbaseIn, MatrixStack matrixStackIn, float partialTickTime) {
		//Vinny's idea to make him bigger, and he do be lookin better
		matrixStackIn.scale(2, 2, 2);
		//matrixStackIn.translate(0.0D, 1.3125D, 0.1875D);
	}

	@Override
	protected void applyRotations(SwampjawEntity entityLiving, MatrixStack matrixStackIn, float ageInTicks, float rotationYaw, float partialTicks) {
		super.applyRotations(entityLiving, matrixStackIn, ageInTicks, rotationYaw, partialTicks);
		matrixStackIn.rotate(Vector3f.XP.rotationDegrees(entityLiving.rotationPitch));
	}

}
