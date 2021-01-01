package lykrast.meetyourfight.registry;

import lykrast.meetyourfight.MeetYourFight;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MeetYourFight.MODID)
public class ModSounds {
	public static SoundEvent bellringerIdle, bellringerHurt, bellringerDeath;
	public static SoundEvent dameFortunaIdle, dameFortunaHurt, dameFortunaDeath, dameFortunaShoot;
	public static SoundEvent swampjawIdle, swampjawHurt, swampjawDeath, swampjawCharge, swampjawBomb;
	public static SoundEvent slicersDiceProc, aceOfIronProc, cagedHeartProc;
	public static SoundEvent musicBellringer, musicDameFortuna, musicSwampjaw;
	
	@SubscribeEvent
	public static void registerSounds(final RegistryEvent.Register<SoundEvent> event) {
		IForgeRegistry<SoundEvent> reg = event.getRegistry();
		bellringerIdle = initSound(reg, "entity.bellringer.idle");
		bellringerHurt = initSound(reg, "entity.bellringer.hurt");
		bellringerDeath = initSound(reg, "entity.bellringer.death");
		
		dameFortunaIdle = initSound(reg, "entity.dame_fortuna.idle");
		dameFortunaHurt = initSound(reg, "entity.dame_fortuna.hurt");
		dameFortunaDeath = initSound(reg, "entity.dame_fortuna.death");
		dameFortunaShoot = initSound(reg, "entity.dame_fortuna.shoot");
		
		swampjawIdle = initSound(reg, "entity.swampjaw.idle");
		swampjawHurt = initSound(reg, "entity.swampjaw.hurt");
		swampjawDeath = initSound(reg, "entity.swampjaw.death");
		swampjawCharge = initSound(reg, "entity.swampjaw.charge");
		swampjawBomb = initSound(reg, "entity.swampjaw.bomb");

		slicersDiceProc = initSound(reg, "item.proc.slicers_dice");
		aceOfIronProc = initSound(reg, "item.proc.ace_of_iron");
		cagedHeartProc = initSound(reg, "item.proc.caged_heart");
		
		musicBellringer = initSound(reg, "music.bellringer");
		musicDameFortuna = initSound(reg, "music.dame_fortuna");
		musicSwampjaw = initSound(reg, "music.swampjaw");
	}

	public static SoundEvent initSound(IForgeRegistry<SoundEvent> reg, String name) {
		ResourceLocation loc = MeetYourFight.rl(name);
		SoundEvent se = new SoundEvent(loc).setRegistryName(loc);
		reg.register(se);
		return se;
	}
}
