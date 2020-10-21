package lykrast.meetyourfight.registry;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.item.CocktailCutlass;
import lykrast.meetyourfight.item.compat.CocktailShotgun;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MeetYourFight.MODID)
public class CommonSetup {	
	@SubscribeEvent
	public static void commonSetup(final FMLCommonSetupEvent event) {
		CocktailCutlass.initEffects();
		if (MeetYourFight.loadedGunsWithoutRoses()) CocktailShotgun.initEffects();
	}
}
