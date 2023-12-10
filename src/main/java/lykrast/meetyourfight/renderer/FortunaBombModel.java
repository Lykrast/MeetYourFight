package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import lykrast.meetyourfight.MeetYourFight;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class FortunaBombModel<T extends Entity> extends EntityModel<T> {
	public static final ModelLayerLocation MODEL = new ModelLayerLocation(MeetYourFight.rl("fortuna_bomb"), "main");
	private final ModelPart renderer;

	public FortunaBombModel(ModelPart modelPart) {
		super(RenderType::entityCutoutNoCull);
		renderer = modelPart.getChild("main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(-4, -4, -4, 8, 8, 8), PartPose.ZERO);
		return LayerDefinition.create(meshdefinition, 32, 16);
	}

	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		renderer.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}

	@Override
	public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		renderer.yRot = netHeadYaw * Mth.DEG_TO_RAD;
		renderer.xRot = headPitch * Mth.DEG_TO_RAD;
	}

}
