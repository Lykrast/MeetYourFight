package lykrast.meetyourfight.renderer;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import lykrast.meetyourfight.entity.JupiterLightningEntity;
import net.minecraft.client.model.EvokerFangsModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public class JupiterLightningRenderer extends EntityRenderer<JupiterLightningEntity> {
	//TODO for now just copied evoker fangs
	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/illager/evoker_fangs.png");//MeetYourFight.rl("textures/entity/swampmine.png");
	private final EvokerFangsModel<JupiterLightningEntity> model;

	public JupiterLightningRenderer(Context context) {
		super(context);
		model = new EvokerFangsModel<>(context.bakeLayer(ModelLayers.EVOKER_FANGS));
	}

	@Override
	public void render(JupiterLightningEntity entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
		float progress = entity.getAnimationProgress(partialTicks);
		if (progress != 0) {
			//evoker fang rendering
			//TODO real warning
			float fangScale = 2;
			if (progress > 0.9F) {
				fangScale *= (1 - progress) / 0.1F;
			}

			matrixStack.pushPose();
			matrixStack.mulPose(Axis.YP.rotationDegrees(90.0F - entity.getYRot()));
			matrixStack.scale(-fangScale, -fangScale, fangScale);
			matrixStack.translate(0.0D, -0.626D, 0.0D);
			matrixStack.scale(0.5F, 0.5F, 0.5F);
			//scale to area
			matrixStack.scale(2,2,2);
			this.model.setupAnim(entity, progress, 0.0F, 0.0F, entity.getYRot(), entity.getXRot());
			VertexConsumer vertexconsumer = buffer.getBuffer(this.model.renderType(TEXTURE));
			this.model.renderToBuffer(matrixStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			matrixStack.popPose();
			super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
			if (entity.lightningTime()) {
				//lightning rendering
				//TODO finish reverse engineering that then adjusting to area size
				float[] afloat = new float[8];
				float[] afloat1 = new float[8];
				float f = 0.0F;
				float f1 = 0.0F;
				RandomSource pathrandom = RandomSource.create(entity.seed);

				for (int i = 7; i >= 0; --i) {
					afloat[i] = f;
					afloat1[i] = f1;
					f += pathrandom.nextInt(3) - 1;
					f1 += pathrandom.nextInt(3) - 1;
				}

				VertexConsumer consumerLightning = buffer.getBuffer(RenderType.lightning());
				Matrix4f matrix4f = matrixStack.last().pose();

				for (int ring = 0; ring < 4; ++ring) {
					RandomSource ringrandom = RandomSource.create(entity.seed);

					for (int branch = 0; branch < 3; ++branch) {
						int maxY = 7;
						int minY = 0;
						if (branch > 0) {
							maxY = 7 - branch;
							minY = maxY - 2;
						}

						float x = afloat[maxY] - f;
						float z = afloat1[maxY] - f1;

						for (int yStep = maxY; yStep >= minY; --yStep) {
							float prevx = x;
							float prevz = z;
							if (branch == 0) {
								x += ringrandom.nextInt(3) - 1;
								z += ringrandom.nextInt(3) - 1;
							}
							else {
								x += ringrandom.nextInt(7) - 3;
								z += ringrandom.nextInt(7) - 3;
							}

							float f6 = 0.5F;
							float f7 = 0.45F;
							float f8 = 0.45F;
							float f9 = 0.5F;
							float prevSize = 0.1F + (float) ring * 0.2F;
							if (branch == 0) {
								prevSize *= (float) yStep * 0.1F + 1.0F;
							}

							float size = 0.1F + (float) ring * 0.2F;
							if (branch == 0) {
								size *= ((float) yStep - 1.0F) * 0.1F + 1.0F;
							}
							//scale to area
							prevSize *= 2;
							size *= 2;

							quad(matrix4f, consumerLightning, x, z, yStep, prevx, prevz, 0.45F, 0.45F, 0.5F, prevSize, size, false, false, true, false);
							quad(matrix4f, consumerLightning, x, z, yStep, prevx, prevz, 0.45F, 0.45F, 0.5F, prevSize, size, true, false, true, true);
							quad(matrix4f, consumerLightning, x, z, yStep, prevx, prevz, 0.45F, 0.45F, 0.5F, prevSize, size, true, true, false, true);
							quad(matrix4f, consumerLightning, x, z, yStep, prevx, prevz, 0.45F, 0.45F, 0.5F, prevSize, size, false, true, false, false);
						}
					}
				}
			}
		}
	}

	//from lightning
	private static void quad(Matrix4f matrix, VertexConsumer consumer, float x, float z, int yStep, float prevx, float prevz, float r, float g, float b, float prevSize, float size, boolean p_115285_,
			boolean p_115286_, boolean p_115287_, boolean p_115288_) {
		consumer.vertex(matrix, x + (p_115285_ ? size : -size), yStep * 4, z + (p_115286_ ? size : -size)).color(r, g, b, 0.3F).endVertex();
		consumer.vertex(matrix, prevx + (p_115285_ ? prevSize : -prevSize), (yStep + 1) * 4, prevz + (p_115286_ ? prevSize : -prevSize)).color(r, g, b, 0.3F).endVertex();
		consumer.vertex(matrix, prevx + (p_115287_ ? prevSize : -prevSize), (yStep + 1) * 4, prevz + (p_115288_ ? prevSize : -prevSize)).color(r, g, b, 0.3F).endVertex();
		consumer.vertex(matrix, x + (p_115287_ ? size : -size), yStep * 4, z + (p_115288_ ? size : -size)).color(r, g, b, 0.3F).endVertex();
	}

	@Override
	public ResourceLocation getTextureLocation(JupiterLightningEntity entity) {
		return TEXTURE;
	}

}
