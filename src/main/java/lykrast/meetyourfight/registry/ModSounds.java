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
	public static RegistryObject<SoundEvent> dameFortunaChipsStart, dameFortunaChipsFire, dameFortunaCardStart, dameFortunaCardRight, dameFortunaCardWrong;
	public static RegistryObject<SoundEvent> dameFortunaSpinStart, dameFortunaSpinLoop, dameFortunaSpinStop, dameFortunaSnap, dameFortunaClap;
	public static RegistryObject<SoundEvent> swampjawIdle, swampjawHurt, swampjawDeath, swampjawCharge, swampjawBomb, swampjawStun;
	public static RegistryObject<SoundEvent> rosalyneHurt, rosalyneDeath, rosalyneCrack, rosalyneSwing, rosalyneSwingPrepare;
	public static RegistryObject<SoundEvent> roseSpiritIdle, roseSpiritHurt, roseSpiritHurtBig, roseSpiritDeath, roseSpiritWarn, roseSpiritShoot;
	//Items
	public static RegistryObject<SoundEvent> slicersDiceProc, aceOfIronProc, cagedHeartProc;
	//Music
	public static RegistryObject<SoundEvent> musicMagnum, musicFrogPunch;
	
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
		dameFortunaChipsStart = initSound("entity.dame_fortuna.chips.start");
		dameFortunaChipsFire = initSound("entity.dame_fortuna.chips.fire");
		dameFortunaCardStart = initSound("entity.dame_fortuna.card.start");
		dameFortunaCardRight = initSound("entity.dame_fortuna.card.right");
		dameFortunaCardWrong = initSound("entity.dame_fortuna.card.wrong");
		dameFortunaSpinStart = initSound("entity.dame_fortuna.spin.start");
		dameFortunaSpinLoop = initSound("entity.dame_fortuna.spin.loop");
		dameFortunaSpinStop = initSound("entity.dame_fortuna.spin.stop");
		dameFortunaSnap = initSound("entity.dame_fortuna.snap");
		dameFortunaClap = initSound("entity.dame_fortuna.clap");
		
		swampjawIdle = initSound("entity.swampjaw.idle");
		swampjawHurt = initSound("entity.swampjaw.hurt");
		swampjawDeath = initSound("entity.swampjaw.death");
		swampjawCharge = initSound("entity.swampjaw.charge");
		swampjawBomb = initSound("entity.swampjaw.bomb");
		swampjawStun = initSound("entity.swampjaw.stun");
		
		rosalyneHurt = initSound("entity.rosalyne.hurt");
		rosalyneDeath = initSound("entity.rosalyne.death");
		rosalyneCrack = initSound("entity.rosalyne.crack");
		rosalyneSwing = initSound("entity.rosalyne.swing");
		rosalyneSwingPrepare = initSound("entity.rosalyne.swing.prepare");
		
		roseSpiritIdle = initSound("entity.rosespirit.idle");
		roseSpiritHurt = initSound("entity.rosespirit.hurt");
		roseSpiritHurtBig = initSound("entity.rosespirit.hurt.big");
		roseSpiritDeath = initSound("entity.rosespirit.death");
		roseSpiritWarn = initSound("entity.rosespirit.warn");
		roseSpiritShoot = initSound("entity.rosespirit.shoot");

		//Items
		slicersDiceProc = initSound("item.proc.slicers_dice");
		aceOfIronProc = initSound("item.proc.ace_of_iron");
		cagedHeartProc = initSound("item.proc.caged_heart");

		//Music
		musicMagnum = initSound("music.magnum");
		musicFrogPunch = initSound("music.frog_punch");
	}
	public static RegistryObject<SoundEvent> initSound(String name) {
		return REG.register(name, () -> new SoundEvent(MeetYourFight.rl(name)));
	}
}
