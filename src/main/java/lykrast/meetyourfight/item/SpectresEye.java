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
		if (livingEntity.tickCount % 60 != 0 || !(livingEntity instanceof PlayerEntity)) return;
		
		List<LivingEntity> list = livingEntity.level.getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(20), e -> e instanceof IMob);
		for (LivingEntity e : list) {
			e.addEffect(new EffectInstance(Effects.GLOWING, 100));
		}
	}

}
