package lykrast.meetyourfight;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod(MeetYourFight.MODID)
public class MeetYourFight {
	public static final String MODID = "meetyourfight";
	
	public static final Logger LOG = LogManager.getLogger();
	
	public MeetYourFight() {
		//Configs one day?
	}
	
	public static ResourceLocation rl(String name) {
		return new ResourceLocation(MODID, name);
	}
	
	//This check is done in multiple places, and just in case I don't want to load the real compat class cause it calls GWR classes
	public static boolean loadedGunsWithoutRoses() {
		return ModList.get().isLoaded("gunswithoutroses");
	}
}
