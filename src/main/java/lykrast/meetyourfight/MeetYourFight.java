package lykrast.meetyourfight;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lykrast.meetyourfight.registry.ModEntities;
import lykrast.meetyourfight.registry.ModItems;
import lykrast.meetyourfight.registry.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MeetYourFight.MODID)
public class MeetYourFight {
	public static final String MODID = "meetyourfight";
	
	public static final Logger LOG = LogManager.getLogger();
	
	public MeetYourFight() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		//You know giving them generic names like that was a bad idea cause it kept getting mixed up with the GWR ones...
		ModItems.REG.register(bus);
		bus.addListener(ModItems::makeCreativeTab);
		ModEntities.REG.register(bus);
		ModSounds.REG.register(bus);
	}
	
	public static ResourceLocation rl(String name) {
		return new ResourceLocation(MODID, name);
	}
	
	//This check is done in multiple places, and just in case I don't want to load the real compat class cause it calls GWR classes
	public static boolean loadedGunsWithoutRoses() {
		return ModList.get().isLoaded("gunswithoutroses");
	}
}
