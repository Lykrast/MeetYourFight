package lykrast.meetyourfight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.RosalyneEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RosalyneRenderer extends MobRenderer<RosalyneEntity, RosalyneModel> {
	public static final ResourceLocation BASE = MeetYourFight.rl("textures/entity/rosalyne.png"), 
			COFFIN = MeetYourFight.rl("textures/entity/rosalyne_coffin.png"), 
			CRACKED = MeetYourFight.rl("textures/entity/rosalyne_cracked.png");

	public RosalyneRenderer(Context context) {
		super(context, new RosalyneModel(context.bakeLayer(RosalyneModel.MODEL)), 0.5F);
		addLayer(new RosalyneGlowLayer(this));
		addLayer(new RosalyneArmorLayer(this, context.getModelSet()));
	}
	
	@Override
	protected void setupRotations(RosalyneEntity entity, PoseStack stack, float ageInTicks, float rotationYaw, float partialTicks) {
		int phase = entity.getPhase();
		if (phase == RosalyneEntity.BREAKING_OUT || phase == RosalyneEntity.MADDENING) {
			//Lifted from the being frozen part of living render
			rotationYaw += (float)(Math.cos(entity.tickCount * 3.25) * Math.PI * 0.8);
		}
		super.setupRotations(entity, stack, ageInTicks, rotationYaw, partialTicks);
	}

	@Override
	public ResourceLocation getTextureLocation(RosalyneEntity entity) {
		int phase = entity.getPhase();
		if (phase == RosalyneEntity.ENCASED || phase == RosalyneEntity.BREAKING_OUT) return COFFIN;
		else if (phase == RosalyneEntity.PHASE_3) return CRACKED;
		else return BASE;
	}

}
