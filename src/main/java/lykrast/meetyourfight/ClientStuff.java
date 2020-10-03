package lykrast.meetyourfight;

import lykrast.meetyourfight.registry.ModEntities;
import lykrast.meetyourfight.renderer.BellringerRenderer;
import lykrast.meetyourfight.renderer.DameFortunaRenderer;
import lykrast.meetyourfight.renderer.ProjectileLineRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MeetYourFight.MODID, value = Dist.CLIENT)
public class ClientStuff {

	@SubscribeEvent
	public static void clientStuff(final FMLClientSetupEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(ModEntities.BELLRINGER, (manager) -> new BellringerRenderer(manager));
		RenderingRegistry.registerEntityRenderingHandler(ModEntities.DAME_FORTUNA, (manager) -> new DameFortunaRenderer(manager));
		RenderingRegistry.registerEntityRenderingHandler(ModEntities.PROJECTILE_LINE, (manager) -> new ProjectileLineRenderer(manager));
	}

}
