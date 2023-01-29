package lykrast.meetyourfight.renderer;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.RoseSpiritEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RoseSpiritRenderer extends MobRenderer<RoseSpiritEntity, RoseSpiritModel> {
	private static final ResourceLocation TEXTURE = MeetYourFight.rl("textures/entity/rose_spirit.png");
	private static final ResourceLocation TEXTURE_SHOOT = MeetYourFight.rl("textures/entity/rose_spirit_shooting.png");
	private static final ResourceLocation TEXTURE_HURT = MeetYourFight.rl("textures/entity/rose_spirit_hurt.png");

	public RoseSpiritRenderer(Context context) {
		super(context, new RoseSpiritModel(context.bakeLayer(RoseSpiritModel.MODEL)), 0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(RoseSpiritEntity entity) {
		int status = entity.getStatus();
		if (status == RoseSpiritEntity.ATTACKING) return TEXTURE_SHOOT;
		else if (status == RoseSpiritEntity.HURT || status == RoseSpiritEntity.RECTRACTING_HURT) return TEXTURE_HURT;
		else return TEXTURE;
	}

}
