package lykrast.meetyourfight.renderer;

import com.mojang.math.Vector3f;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.RosalyneEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.util.Mth;

public class RosalyneModel extends HumanoidModel<RosalyneEntity> {
	//TODO PLACEHOLDER
	public static final ModelLayerLocation MODEL = new ModelLayerLocation(MeetYourFight.rl("rosalyne"), "main");
	//x, y, z rotations for the right arm for each animation
	private Vector3f[] animRotations = {
			new Vector3f(0,0,0), 
			new Vector3f(0,0,2.3561945F), 
			new Vector3f(-15*Mth.DEG_TO_RAD,0,75*Mth.DEG_TO_RAD), 
			new Vector3f(-135*Mth.DEG_TO_RAD,0,75*Mth.DEG_TO_RAD)
			};
	private float animProgress;

	public RosalyneModel(ModelPart modelPart) {
		super(modelPart);
	}

	public static LayerDefinition createBodyLayer() {
	      MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
	      return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void prepareMobModel(RosalyneEntity entityIn, float limbSwing, float limbSwingAmount, float partialTick) {
		animProgress = entityIn.getAnimProgress(partialTick);
		if (entityIn.clientAnim != RosalyneEntity.ANIM_NEUTRAL) {
			//Fast initial then slows down
			animProgress = 1-animProgress;
			animProgress *= animProgress;
			animProgress *= animProgress;
			animProgress = 1-animProgress;
		}
		super.prepareMobModel(entityIn, limbSwing, limbSwingAmount, partialTick);
	}
	
	@Override
	public void setupAnim(RosalyneEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		rightArm.z = 0.0F;
		rightArm.x = -5.0F;
		Vector3f anim = animRotations[entityIn.clientAnim];
		if (animProgress >= 0.99) {
			rightArm.xRot = anim.x();
			rightArm.yRot = anim.y();
			rightArm.zRot = anim.z();
		}
		else {
			Vector3f prev = animRotations[entityIn.prevAnim];
			rightArm.xRot = rotlerpRad(animProgress, prev.x(), anim.x());
			rightArm.yRot = rotlerpRad(animProgress, prev.y(), anim.y());
			rightArm.zRot = rotlerpRad(animProgress, prev.z(), anim.z());
		}
	}

}
