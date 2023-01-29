package lykrast.meetyourfight.renderer;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.RosalyneEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;

public class RosalyneModel extends HumanoidModel<RosalyneEntity> {
	//TODO PLACEHOLDER
	public static final ModelLayerLocation MODEL = new ModelLayerLocation(MeetYourFight.rl("rosalyne"), "main");

	public RosalyneModel(ModelPart modelPart) {
		super(modelPart);
	}

	public static LayerDefinition createBodyLayer() {
	      MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
	      return LayerDefinition.create(meshdefinition, 64, 64);
	}

}
