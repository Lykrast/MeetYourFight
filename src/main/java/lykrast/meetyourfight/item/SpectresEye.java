package lykrast.meetyourfight.item;

import java.util.List;

import lykrast.meetyourfight.misc.MYFConstants;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class SpectresEye extends CurioBaseItem {

	public SpectresEye(Properties properties) {
		super(properties, true, MYFConstants.SPECTRES_EYE_RANGE);
	}
	
	@Override
	public void curioTick(SlotContext slotContext, ItemStack stack) {
		LivingEntity livingEntity = slotContext.entity();
		if (livingEntity.tickCount % 60 != 0 || !(livingEntity instanceof Player)) return;
		
		List<LivingEntity> list = livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(MYFConstants.SPECTRES_EYE_RANGE), e -> e instanceof Enemy);
		for (LivingEntity e : list) {
			e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100));
		}
	}

}
