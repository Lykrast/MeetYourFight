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
	//TODO NOT WORKING
	private final LivingEntity boss;

	public BossMusic(LivingEntity boss, SoundEvent sound) {
		super(sound, SoundCategory.RECORDS);
		//MeetYourFight.LOG.info("hello this is new ound");
		this.boss = boss;
		x = boss.getPosX();
		y = boss.getPosY();
		z = boss.getPosZ();
		//repeat = true;
	}

	@Override
	public void tick() {
		if (boss.isAlive()) {
			//MeetYourFight.LOG.info("sound goes tick");
			x = boss.getPosX();
			y = boss.getPosY();
			z = boss.getPosZ();
		}
		else func_239509_o_();
	}

}
