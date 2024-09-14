package lykrast.meetyourfight.renderer;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.DameFortunaEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class DameFortunaModel extends HumanoidModel<DameFortunaEntity> {
	public static final ModelLayerLocation MODEL = new ModelLayerLocation(MeetYourFight.rl("dame_fortuna"), "main");
	public static final ModelLayerLocation MODEL_ARMOR = new ModelLayerLocation(MeetYourFight.rl("dame_fortuna"), "armor");
	public float headProgress;
	private float animProgress;
	
	public DameFortunaModel(ModelPart modelPart) {
		super(modelPart);
	}

	public static LayerDefinition createBodyLayer(CubeDeformation deform) {
		//MeshDefinition meshdefinition = HumanoidModel.createMesh(deform, 0);
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4, -4, -4, 8, 8, 8, deform), PartPose.offset(0, -8, 0));
		partdefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, deform.extend(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));
		partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, deform), PartPose.offset(0.0F, 0.0F, 0.0F));
		partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform), PartPose.offset(-5.0F, 2.0F, 0.0F));
		partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform), PartPose.offset(5.0F, 2.0F, 0.0F));
		partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform), PartPose.offset(-1.9F, 12.0F, 0.0F));
		partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform), PartPose.offset(1.9F, 12.0F, 0.0F));
		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void prepareMobModel(DameFortunaEntity entity, float limbSwing, float limbSwingAmount, float partialTick) {
		super.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
		headProgress = entity.getHeadRotationProgress(partialTick);
		animProgress = entity.getAnimProgress(partialTick);
	}

	@Override
	public void setupAnim(DameFortunaEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		//Save previous head angles for animations before super overrides them so we override them back
		float headX = head.xRot;
		float headY = head.yRot;
		float headZ = head.zRot;
		
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		hat.skipDraw = true;
		//Hey guess what this gets overridden in the super too
		//We'll do something fancy with it, should cycle every 100 ticks (5 seconds)
        head.y = -8 + Mth.sin(ageInTicks * (float)Math.PI / 50f);
		
		//Head animation
		head.xRot = rotlerpRad(headProgress, headX, entity.headTargetPitch * Mth.HALF_PI);
		head.yRot = rotlerpRad(headProgress, headY, entity.headTargetYaw * Mth.HALF_PI);
		head.zRot = rotlerpRad(headProgress, headZ, entity.headTargetRoll * Mth.HALF_PI);
		
		//Same pose as Illagers casting spells
		//1 is normal attack, 2 is big attack
		if (entity.clientAnim == DameFortunaEntity.ANIM_ATTACK_1) {
			leftArm.z = 0.0F;
			leftArm.x = 5.0F;
			leftArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.25F;
			leftArm.zRot = -2.3561945F;
			leftArm.yRot = 0.0F;
		}
		else if (entity.clientAnim == DameFortunaEntity.ANIM_ATTACK_2) {
			rightArm.z = 0.0F;
			rightArm.x = -5.0F;
			rightArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.25F;
			rightArm.zRot = 2.3561945F;
			rightArm.yRot = 0.0F;
		}
		else if (entity.clientAnim == DameFortunaEntity.ANIM_SPIN) {
			rightArm.xRot = 0;
			rightArm.zRot = 2.3561945F;
			rightArm.yRot = 0.0F;
			leftArm.xRot = 0;
			leftArm.zRot = -2.3561945F;
			leftArm.yRot = 0.0F;
		}
	}

}
