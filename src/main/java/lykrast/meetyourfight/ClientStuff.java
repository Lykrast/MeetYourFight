package lykrast.meetyourfight;

import lykrast.meetyourfight.registry.ModEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.SpriteRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MeetYourFight.MODID, value = Dist.CLIENT)
public class ClientStuff {

	@SubscribeEvent
	public static void clientStuff(final FMLClientSetupEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(ModEntities.GHOST_LINE, (manager) -> new SpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
	}

}
