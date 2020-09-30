package lykrast.meetyourfight.item;

import java.util.List;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;

public class SpectresEye extends CurioBaseItem {

	public SpectresEye(Properties properties) {
		super(properties, true);
	}
	
	@Override
	public void curioTick(String identifier, int index, LivingEntity livingEntity) {
		if (livingEntity.ticksExisted % 60 != 0 || !(livingEntity instanceof PlayerEntity)) return;
		
		List<LivingEntity> list = livingEntity.world.getEntitiesWithinAABB(LivingEntity.class, livingEntity.getBoundingBox().grow(20), e -> e instanceof IMob);
		for (LivingEntity e : list) {
			e.addPotionEffect(new EffectInstance(Effects.GLOWING, 100));
		}
	}

}
