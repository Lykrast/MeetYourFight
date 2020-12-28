package lykrast.meetyourfight;

import lykrast.meetyourfight.registry.CompatGWRItems;
import lykrast.meetyourfight.registry.ModEntities;
import lykrast.meetyourfight.registry.ModItems;
import lykrast.meetyourfight.renderer.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MeetYourFight.MODID, value = Dist.CLIENT)
public class ClientStuff {

	@SubscribeEvent
	public static void clientStuff(final FMLClientSetupEvent event) {
		//Items
		ItemColors icol = Minecraft.getInstance().getItemColors();
		icol.register((s, t) -> t == 1 ? MathHelper.hsvToRGB(((Util.milliTime() / 1000) % 360) / 360f, 1, 1) : -1, ModItems.cocktailCutlass);
		if (MeetYourFight.loadedGunsWithoutRoses()) icol.register((s, t) -> t == 1 ? MathHelper.hsvToRGB(((Util.milliTime() / 1000) % 360) / 360f, 0.75f, 0.75f) : -1, CompatGWRItems.cocktailShotgun);

		//Same as Bow
		ItemModelsProperties.func_239418_a_(ModItems.depthStar, MeetYourFight.rl("charge"),
				(stack, world, entity) -> entity == null || entity.getActiveItemStack() != stack ? 0 : (stack.getUseDuration() - entity.getItemInUseCount()) / 20.0F);
		ItemModelsProperties.func_239418_a_(ModItems.depthStar, MeetYourFight.rl("charging"),
				(stack, world, entity) -> entity != null && entity.isHandActive() && entity.getActiveItemStack() == stack ? 1 : 0);
		
		//Entities
		RenderingRegistry.registerEntityRenderingHandler(ModEntities.BELLRINGER, (manager) -> new BellringerRenderer(manager));
		RenderingRegistry.registerEntityRenderingHandler(ModEntities.DAME_FORTUNA, (manager) -> new DameFortunaRenderer(manager));
		RenderingRegistry.registerEntityRenderingHandler(ModEntities.SWAMPJAW, (manager) -> new SwampjawRenderer(manager));
		
		RenderingRegistry.registerEntityRenderingHandler(ModEntities.PROJECTILE_LINE, (manager) -> new ProjectileLineRenderer(manager));
		RenderingRegistry.registerEntityRenderingHandler(ModEntities.SWAMP_MINE, (manager) -> new SwampMineRenderer(manager));
	}

}
