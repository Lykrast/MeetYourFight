package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import lykrast.meetyourfight.entity.SwampMineEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

// Made with Blockbench 3.7.4
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
public class SwampMineModel extends EntityModel<SwampMineEntity> {
	private final ModelPart spikes;
	private final ModelPart bottombackright_r1;
	private final ModelPart bottombackleft_r1;
	private final ModelPart bottomfrontright_r1;
	private final ModelPart bottomfrontleft_r1;
	private final ModelPart topbackright_r1;
	private final ModelPart topbackleft_r1;
	private final ModelPart topfrontright_r1;
	private final ModelPart topfrontleft_r1;
	private final ModelPart bb_main;

	public SwampMineModel() {
		texWidth = 64;
		texHeight = 32;

		spikes = new ModelPart(this);
		spikes.setPos(-6.0F, 10.0F, 6.0F);
		

		bottombackright_r1 = new ModelPart(this);
		bottombackright_r1.setPos(0.0F, 12.0F, 0.0F);
		spikes.addChild(bottombackright_r1);
		setRotationAngle(bottombackright_r1, -2.3562F, -0.7854F, 0.0F);
		bottombackright_r1.texOffs(0, 0).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 2.0F, 0.0F, false);

		bottombackleft_r1 = new ModelPart(this);
		bottombackleft_r1.setPos(12.0F, 12.0F, 0.0F);
		spikes.addChild(bottombackleft_r1);
		setRotationAngle(bottombackleft_r1, -2.3562F, 0.7854F, 0.0F);
		bottombackleft_r1.texOffs(0, 0).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 2.0F, 0.0F, false);

		bottomfrontright_r1 = new ModelPart(this);
		bottomfrontright_r1.setPos(0.0F, 12.0F, -12.0F);
		spikes.addChild(bottomfrontright_r1);
		setRotationAngle(bottomfrontright_r1, 2.3562F, 0.7854F, 0.0F);
		bottomfrontright_r1.texOffs(0, 0).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 2.0F, 0.0F, false);

		bottomfrontleft_r1 = new ModelPart(this);
		bottomfrontleft_r1.setPos(12.0F, 12.0F, -12.0F);
		spikes.addChild(bottomfrontleft_r1);
		setRotationAngle(bottomfrontleft_r1, 2.3562F, -0.7854F, 0.0F);
		bottomfrontleft_r1.texOffs(0, 0).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 2.0F, 0.0F, false);

		topbackright_r1 = new ModelPart(this);
		topbackright_r1.setPos(0.0F, 0.0F, 0.0F);
		spikes.addChild(topbackright_r1);
		setRotationAngle(topbackright_r1, -0.7854F, -0.7854F, 0.0F);
		topbackright_r1.texOffs(0, 0).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 2.0F, 0.0F, false);

		topbackleft_r1 = new ModelPart(this);
		topbackleft_r1.setPos(12.0F, 0.0F, 0.0F);
		spikes.addChild(topbackleft_r1);
		setRotationAngle(topbackleft_r1, -0.7854F, 0.7854F, 0.0F);
		topbackleft_r1.texOffs(0, 0).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 2.0F, 0.0F, false);

		topfrontright_r1 = new ModelPart(this);
		topfrontright_r1.setPos(0.0F, 0.0F, -12.0F);
		spikes.addChild(topfrontright_r1);
		setRotationAngle(topfrontright_r1, 0.7854F, 0.7854F, 0.0F);
		topfrontright_r1.texOffs(0, 0).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 2.0F, 0.0F, false);

		topfrontleft_r1 = new ModelPart(this);
		topfrontleft_r1.setPos(12.0F, 0.0F, -12.0F);
		spikes.addChild(topfrontleft_r1);
		setRotationAngle(topfrontleft_r1, 0.7854F, -0.7854F, 0.0F);
		topfrontleft_r1.texOffs(0, 0).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 2.0F, 0.0F, false);

		bb_main = new ModelPart(this);
		bb_main.setPos(0.0F, 24.0F, 0.0F);
		bb_main.texOffs(0, 0).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 16.0F, 0.0F, false);
	}

	@Override
	public void setupAnim(SwampMineEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){
		//previously the render function, render code was moved to a method below
	}

	@Override
	public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		spikes.render(matrixStack, buffer, packedLight, packedOverlay);
		bb_main.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}