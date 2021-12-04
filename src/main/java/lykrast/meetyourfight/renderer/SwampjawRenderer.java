package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SwampjawRenderer extends MobRenderer<SwampjawEntity, SwampjawModel> {
	private static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/swampjaw.png");

	public SwampjawRenderer(Context context) {
		super(context, new SwampjawModel(context.bakeLayer(SwampjawModel.MODEL)), 0.75F);
	}

	@Override
	public ResourceLocation getTextureLocation(SwampjawEntity entity) {
		return TEXTURE;
	}

	@Override
	protected void scale(SwampjawEntity entitylivingbaseIn, PoseStack matrixStackIn, float partialTickTime) {
		//Vinny's idea to make him bigger, and he do be lookin better
		matrixStackIn.scale(2, 2, 2);
		//matrixStackIn.translate(0.0D, 1.3125D, 0.1875D);
	}

	@Override
	protected void setupRotations(SwampjawEntity entityLiving, PoseStack matrixStackIn, float ageInTicks, float rotationYaw, float partialTicks) {
		super.setupRotations(entityLiving, matrixStackIn, ageInTicks, rotationYaw, partialTicks);
		matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(entityLiving.getXRot()));
	}

}
