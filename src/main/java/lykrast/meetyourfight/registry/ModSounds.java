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
	public static SoundEvent slicersDiceProc, aceOfIronProc;
	public static SoundEvent musicBellringer, musicDameFortuna;
	
	@SubscribeEvent
	public static void registerSounds(final RegistryEvent.Register<SoundEvent> event) {
		IForgeRegistry<SoundEvent> reg = event.getRegistry();

		slicersDiceProc = initSound(reg, "slicers_dice.proc");
		aceOfIronProc = initSound(reg, "ace_of_iron.proc");
		
		musicBellringer = initSound(reg, "music.bellringer");
		musicDameFortuna = initSound(reg, "music.dame_fortuna");
	}

	public static SoundEvent initSound(IForgeRegistry<SoundEvent> reg, String name) {
		ResourceLocation loc = MeetYourFight.rl(name);
		SoundEvent se = new SoundEvent(loc).setRegistryName(loc);
		reg.register(se);
		return se;
	}
}
