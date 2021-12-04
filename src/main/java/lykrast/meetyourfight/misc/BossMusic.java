package lykrast.meetyourfight.misc;

import net.minecraft.client.audio.TickableSound;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BossMusic extends TickableSound {
	//TickableSound is client only so I think it's appropriate to mark that too
	//Also it's from Botania https://github.com/Vazkii/Botania/blob/master/src/main/java/vazkii/botania/common/entity/EntityDoppleganger.java#L1014
	//and a bit from the bees
	private final LivingEntity boss;

	public BossMusic(LivingEntity boss, SoundEvent sound) {
		super(sound, SoundCategory.RECORDS);
		this.boss = boss;
		x = boss.getX();
		y = boss.getY();
		z = boss.getZ();
		looping = true;
	}

	@Override
	public void tick() {
		if (boss.isAlive()) {
			x = boss.getX();
			y = boss.getY();
			z = boss.getZ();
		}
		else stop();
	}

}
