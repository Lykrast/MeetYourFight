package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.FortunaCardEntity;
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

public class FortunaCardModel extends EntityModel<FortunaCardEntity> {
	// Made with Blockbench 4.1.5 but like I manually imported the stuff
	public static final ModelLayerLocation MODEL = new ModelLayerLocation(MeetYourFight.rl("fortuna_card"), "main");
	private final ModelPart renderer;

	public FortunaCardModel(ModelPart modelPart) {
		super(RenderType::entityCutoutNoCull);
		renderer = modelPart.getChild("main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(-14, -40, -0.5f, 28, 40, 1), PartPose.offset(0, 24, 0));
		
		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		renderer.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}

	@Override
	public void setupAnim(FortunaCardEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {		
		//WHY THE FUCK DO I HAVE TO DO 180-YAW FOR IT TO BE GOOD??
		renderer.yRot = (180 - netHeadYaw) * Mth.DEG_TO_RAD;
		renderer.xRot = headPitch * Mth.DEG_TO_RAD;
	}

}
