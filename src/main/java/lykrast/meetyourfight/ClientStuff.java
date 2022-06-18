package lykrast.meetyourfight;

import lykrast.meetyourfight.registry.CompatGWRItems;
import lykrast.meetyourfight.registry.ModEntities;
import lykrast.meetyourfight.registry.ModItems;
import lykrast.meetyourfight.renderer.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MeetYourFight.MODID, value = Dist.CLIENT)
public class ClientStuff {
	
    @SubscribeEvent
    public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
		//Entities
    	event.registerEntityRenderer(ModEntities.BELLRINGER, (context) -> new BellringerRenderer(context));
    	event.registerEntityRenderer(ModEntities.DAME_FORTUNA, (context) -> new DameFortunaRenderer(context));
    	event.registerEntityRenderer(ModEntities.SWAMPJAW, (context) -> new SwampjawRenderer(context));
    	//event.registerEntityRenderer(ModEntities.VELA, (context) -> new VelaRenderer(context));
		
    	event.registerEntityRenderer(ModEntities.PROJECTILE_LINE, (context) -> new ProjectileLineRenderer(context));
		event.registerEntityRenderer(ModEntities.SWAMP_MINE, (context) -> new SwampMineRenderer(context));
		//event.registerEntityRenderer(ModEntities.WATER_BOULDER, (context) -> new WaterBoulderRenderer(context));
		//event.registerEntityRenderer(ModEntities.VELA_VORTEX, (context) -> new VelaVortexRenderer(context));
    }
    
    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
    	event.registerLayerDefinition(BellringerModel.MODEL, BellringerModel::createBodyLayer);
    	event.registerLayerDefinition(DameFortunaModel.MODEL, DameFortunaModel::createBodyLayer);
    	event.registerLayerDefinition(SwampjawModel.MODEL, SwampjawModel::createBodyLayer);
    	event.registerLayerDefinition(VelaModel.MODEL, VelaModel::createBodyLayer);

    	event.registerLayerDefinition(ProjectileLineModel.MODEL, ProjectileLineModel::createBodyLayer);
    	event.registerLayerDefinition(SwampMineModel.MODEL, SwampMineModel::createBodyLayer);
    	event.registerLayerDefinition(WaterBoulderModel.MODEL, WaterBoulderModel::createBodyLayer);
    	event.registerLayerDefinition(VelaVortexModel.MODEL, VelaVortexModel::createBodyLayer);
    }

	@SubscribeEvent
	public static void clientStuff(final FMLClientSetupEvent event) {
		//Items
		ItemColors icol = Minecraft.getInstance().getItemColors();
		icol.register((s, t) -> t == 1 ? Mth.hsvToRgb(((Util.getMillis() / 1000) % 360) / 360f, 1, 1) : -1, ModItems.cocktailCutlass);
		if (MeetYourFight.loadedGunsWithoutRoses()) icol.register((s, t) -> t == 1 ? Mth.hsvToRgb(((Util.getMillis() / 1000) % 360) / 360f, 0.75f, 0.75f) : -1, CompatGWRItems.cocktailShotgun);

		//Same as Bow
		ItemProperties.register(ModItems.depthStar, MeetYourFight.rl("charge"),
				(stack, world, entity, someint) -> entity == null || entity.getUseItem() != stack ? 0 : (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / 20.0F);
		ItemProperties.register(ModItems.depthStar, MeetYourFight.rl("charging"),
				(stack, world, entity, someint) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1 : 0);
	}

}
