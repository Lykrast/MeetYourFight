package lykrast.meetyourfight.registry;

import lykrast.meetyourfight.MeetYourFight;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
	public static final DeferredRegister<SoundEvent> REG = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MeetYourFight.MODID);
	//Bosses
	public static RegistryObject<SoundEvent> bellringerIdle, bellringerHurt, bellringerDeath;
	public static RegistryObject<SoundEvent> dameFortunaIdle, dameFortunaHurt, dameFortunaDeath, dameFortunaAttack, dameFortunaShoot;
	public static RegistryObject<SoundEvent> swampjawIdle, swampjawHurt, swampjawDeath, swampjawCharge, swampjawBomb;
	public static RegistryObject<SoundEvent> rosalyneHurt, rosalyneDeath, rosalyneCrack;
	//Items
	public static RegistryObject<SoundEvent> slicersDiceProc, aceOfIronProc, cagedHeartProc;
	//Music
	public static RegistryObject<SoundEvent> musicMagnum;
	
	static {
		//Bosses
		bellringerIdle = initSound("entity.bellringer.idle");
		bellringerHurt = initSound("entity.bellringer.hurt");
		bellringerDeath = initSound("entity.bellringer.death");
		
		dameFortunaIdle = initSound("entity.dame_fortuna.idle");
		dameFortunaHurt = initSound("entity.dame_fortuna.hurt");
		dameFortunaDeath = initSound("entity.dame_fortuna.death");
		dameFortunaAttack = initSound("entity.dame_fortuna.attack");
		dameFortunaShoot = initSound("entity.dame_fortuna.shoot");
		
		swampjawIdle = initSound("entity.swampjaw.idle");
		swampjawHurt = initSound("entity.swampjaw.hurt");
		swampjawDeath = initSound("entity.swampjaw.death");
		swampjawCharge = initSound("entity.swampjaw.charge");
		swampjawBomb = initSound("entity.swampjaw.bomb");
		
		rosalyneHurt = initSound("entity.rosalyne.hurt");
		rosalyneDeath = initSound("entity.rosalyne.death");
		rosalyneCrack = initSound("entity.rosalyne.crack");

		//Items
		slicersDiceProc = initSound("item.proc.slicers_dice");
		aceOfIronProc = initSound("item.proc.ace_of_iron");
		cagedHeartProc = initSound("item.proc.caged_heart");

		//Music
		musicMagnum = initSound("music.magnum");
	}
	public static RegistryObject<SoundEvent> initSound(String name) {
		return REG.register(name, () -> new SoundEvent(MeetYourFight.rl(name)));
	}
}
