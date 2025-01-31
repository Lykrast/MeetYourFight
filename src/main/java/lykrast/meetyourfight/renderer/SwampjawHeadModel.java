package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class SwampjawHeadModel extends SkullModelBase {
	private final ModelPart root;
	protected final ModelPart head, jaw;

	public SwampjawHeadModel(ModelPart part) {
		root = part;
		head = part.getChild("head");
		jaw = head.getChild("jaw");
	}

	public static LayerDefinition createLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		//adapted from the human skull, so jaw won't rotate properly with that setup, but am not animating it for now
		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-5, -8, -5, 10, 6, 10), PartPose.ZERO);
		head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(0, 16).addBox(-5, -2, -5, 10, 2, 10), PartPose.ZERO);
		return LayerDefinition.create(meshdefinition, 128, 64);
	}

	@Override
	public void setupAnim(float p_170950_, float p_170951_, float p_103813_) {
		head.yRot = p_170951_ * Mth.DEG_TO_RAD;
		head.xRot = p_103813_ * Mth.DEG_TO_RAD;
	}

	@Override
	public void renderToBuffer(PoseStack p_103815_, VertexConsumer p_103816_, int p_103817_, int p_103818_, float p_103819_, float p_103820_, float p_103821_, float p_103822_) {
		root.render(p_103815_, p_103816_, p_103817_, p_103818_, p_103819_, p_103820_, p_103821_, p_103822_);
	}

}
