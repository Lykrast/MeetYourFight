package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

// Made with Blockbench 3.7.4
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
public class SwampjawModel extends EntityModel<SwampjawEntity> {
	private final ModelRenderer bodyMain;
	private final ModelRenderer finRight;
	private final ModelRenderer finLeft;
	private final ModelRenderer tailfinBottom;
	private final ModelRenderer tailfinTop;
	private final ModelRenderer tailOuter;
	private final ModelRenderer tailInner;
	private final ModelRenderer jaw;
	private final ModelRenderer head;
	
	//Blockbench constant
	private static final float TAILFIN_PITCH = 0.2618F;
	
	private float tailYaw, tailPitch;

	public SwampjawModel() {
		textureWidth = 128;
		textureHeight = 64;
		
		//Blockbench whyyyyy you grouping stuff
		//Had to rotate all the things I wanted separate by a little bit so that I got a second export where they're separate
		bodyMain = new ModelRenderer(this);
		bodyMain.setRotationPoint(0.0F, 24.0F, 0.0F);
		bodyMain.setTextureOffset(40, 0).addBox(-6.0F, -10.0F, -6.0F, 12.0F, 10.0F, 12.0F, 0.0F, false);

		finRight = new ModelRenderer(this);
		finRight.setRotationPoint(-6.0F, -5.0F, 0.0F);
		bodyMain.addChild(finRight);
		setRotationAngle(finRight, 0.0F, 0.0F, -0.4363F);
		finRight.setTextureOffset(0, 28).addBox(-8.0F, 0.0F, -2.0F, 8.0F, 1.0F, 4.0F, 0.0F, true);

		finLeft = new ModelRenderer(this);
		finLeft.setRotationPoint(6.0F, -5.0F, 0.0F);
		bodyMain.addChild(finLeft);
		setRotationAngle(finLeft, 0.0F, 0.0F, 0.4363F);
		finLeft.setTextureOffset(0, 28).addBox(0.0F, 0.0F, -2.0F, 8.0F, 1.0F, 4.0F, 0.0F, false);

		tailInner = new ModelRenderer(this);
		tailInner.setRotationPoint(0.0F, -10.0F, 6.0F);
		bodyMain.addChild(tailInner);
		tailInner.setTextureOffset(40, 22).addBox(-5.0F, 0.0F, 0.0F, 10.0F, 8.0F, 8.0F, 0.0F, false);

		tailOuter = new ModelRenderer(this);
		tailOuter.setRotationPoint(0, 0, 8);
		tailInner.addChild(tailOuter);
		tailOuter.setTextureOffset(40, 38).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 2.0F, 6.0F, 0.0F, false);

		tailfinTop = new ModelRenderer(this);
		tailfinTop.setRotationPoint(0, 1, 5);
		tailOuter.addChild(tailfinTop);
		setRotationAngle(tailfinTop, -TAILFIN_PITCH, 0.0F, 0.0F);
		tailfinTop.setTextureOffset(0, 33).addBox(-0.5F, -10.0F, 0.0F, 1.0F, 10.0F, 5.0F, 0.0F, false);

		tailfinBottom = new ModelRenderer(this);
		tailfinBottom.setRotationPoint(0, 1, 5);
		tailOuter.addChild(tailfinBottom);
		setRotationAngle(tailfinBottom, TAILFIN_PITCH, 0.0F, 0.0F);
		tailfinBottom.setTextureOffset(12, 33).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 10.0F, 5.0F, -0.1F, false);

		head = new ModelRenderer(this);
		head.setRotationPoint(0.0F, -5.0F, -6.0F);
		bodyMain.addChild(head);
		head.setTextureOffset(0, 0).addBox(-5.0F, -4.0F, -10.0F, 10.0F, 6.0F, 10.0F, 0.0F, false);

		jaw = new ModelRenderer(this);
		jaw.setRotationPoint(0, 2, 0);
		head.addChild(jaw);
		jaw.setTextureOffset(0, 16).addBox(-5.0F, 0.0F, -10.0F, 10.0F, 2.0F, 10.0F, 0.0F, false);
	}

	@Override
	public void setLivingAnimations(SwampjawEntity entity, float limbSwing, float limbSwingAmount, float partialTick) {
		super.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTick);
		tailYaw = MathHelper.wrapSubtractDegrees(entity.rotationYaw, entity.getTailYaw(partialTick)) / 3F;
		tailPitch = -MathHelper.wrapSubtractDegrees(entity.rotationPitch, entity.getTailPitch(partialTick)) / 1.5F;
		tailYaw *= (float)Math.PI / 180F;
		tailPitch *= (float)Math.PI / 180F;
	}

	@Override
	public void setRotationAngles(SwampjawEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		//bodyMain.rotateAngleX = entity.rotationPitch * ((float) Math.PI / 180F);
		
		//Head
		head.rotateAngleX = headPitch * ((float) Math.PI / 180F);
		head.rotateAngleY = netHeadYaw * ((float) Math.PI / 180F);
		
		//Flap fins
		finLeft.rotateAngleY = MathHelper.cos(limbSwing * 0.2F) * 0.8F * limbSwingAmount;
		finRight.rotateAngleY = MathHelper.cos(limbSwing * 0.2F + (float)Math.PI) * 0.8F * limbSwingAmount;
		
		//Rotate tail
		tailInner.rotateAngleX = tailPitch;
		tailInner.rotateAngleY = tailYaw + MathHelper.cos(limbSwing * 0.35F) * 0.15F * limbSwingAmount;
		tailOuter.rotateAngleX = tailPitch;
		tailOuter.rotateAngleY = tailYaw + MathHelper.cos(limbSwing * 0.35F + (float)Math.PI / 3F) * 0.15F * limbSwingAmount;
		tailfinTop.rotateAngleY = tailYaw + MathHelper.cos(limbSwing * 0.35F + 2F * (float)Math.PI / 3F) * 0.15F * limbSwingAmount;
		tailfinBottom.rotateAngleY = tailfinTop.rotateAngleY;
	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		bodyMain.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}