package lykrast.meetyourfight.misc;

import lykrast.meetyourfight.entity.DameFortunaEntity;
import lykrast.meetyourfight.registry.MYFSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FortunaSpinSound extends AbstractTickableSoundInstance {
	//TickableSound is client only so I think it's appropriate to mark that too
	private final DameFortunaEntity dame;

	public FortunaSpinSound(DameFortunaEntity dame) {
		super(MYFSounds.dameFortunaSpinLoop.get(), dame.getSoundSource(), SoundInstance.createUnseededRandom());
		this.dame = dame;
		x = dame.getX();
		y = dame.getY();
		z = dame.getZ();
		looping = true;
	}

	@Override
	public void tick() {
		if (dame.isAlive()) {
			x = dame.getX();
			y = dame.getY();
			z = dame.getZ();
			//I don't know how to start a looping sound at any time without custom packets
			//so until then this will just be here the whole fight and be muted when she not spinning
			if (dame.getAnimation() == DameFortunaEntity.ANIM_SPIN) volume = 2;
			else volume = 0;
		}
		else stop();
	}

	@Override
	public boolean canStartSilent() {
		return true;
	}

}
