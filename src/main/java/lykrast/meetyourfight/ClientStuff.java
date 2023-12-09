package lykrast.meetyourfight;

import lykrast.meetyourfight.registry.CompatGWRItems;
import lykrast.meetyourfight.registry.ModEntities;
import lykrast.meetyourfight.registry.ModItems;
import lykrast.meetyourfight.renderer.*;
import net.minecraft.Util;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MeetYourFight.MODID, value = Dist.CLIENT)
public class ClientStuff {
	
    @SubscribeEvent
    public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
		//Entities
    	event.registerEntityRenderer(ModEntities.BELLRINGER.get(), (context) -> new BellringerRenderer(context));
    	event.registerEntityRenderer(ModEntities.DAME_FORTUNA.get(), (context) -> new DameFortunaRenderer(context));
    	event.registerEntityRenderer(ModEntities.SWAMPJAW.get(), (context) -> new SwampjawRenderer(context));
    	event.registerEntityRenderer(ModEntities.ROSALYNE.get(), (context) -> new RosalyneRenderer(context));
    	event.registerEntityRenderer(ModEntities.ROSE_SPIRIT.get(), (context) -> new RoseSpiritRenderer(context));
    	//TODO Vela
    	//event.registerEntityRenderer(ModEntities.VELA.get(), (context) -> new VelaRenderer(context));
		
    	event.registerEntityRenderer(ModEntities.PROJECTILE_LINE.get(), (context) -> new ProjectileLineRenderer(context));
    	event.registerEntityRenderer(ModEntities.PROJECTILE_TARGETED.get(), (context) -> new ProjectileTargetedRenderer(context));
    	event.registerEntityRenderer(ModEntities.FORTUNA_BOMB.get(), (context) -> new FortunaBombRenderer(context));
		event.registerEntityRenderer(ModEntities.SWAMP_MINE.get(), (context) -> new SwampMineRenderer(context));
		//event.registerEntityRenderer(ModEntities.WATER_BOULDER.get(), (context) -> new WaterBoulderRenderer(context));
		//event.registerEntityRenderer(ModEntities.VELA_VORTEX.get(), (context) -> new VelaVortexRenderer(context));
    }
    
    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
    	event.registerLayerDefinition(BellringerModel.MODEL, BellringerModel::createBodyLayer);
    	event.registerLayerDefinition(DameFortunaModel.MODEL, DameFortunaModel::createBodyLayer);
    	event.registerLayerDefinition(SwampjawModel.MODEL, SwampjawModel::createBodyLayer);
    	event.registerLayerDefinition(RosalyneModel.MODEL, () -> RosalyneModel.createBodyLayer(CubeDeformation.NONE, true));
    	event.registerLayerDefinition(RosalyneModel.MODEL_ARMOR, () -> RosalyneModel.createBodyLayer(LayerDefinitions.INNER_ARMOR_DEFORMATION, false));
    	event.registerLayerDefinition(RoseSpiritModel.MODEL, RoseSpiritModel::createBodyLayer);
    	//TODO Vela
    	//event.registerLayerDefinition(VelaModel.MODEL, VelaModel::createBodyLayer);

    	event.registerLayerDefinition(ProjectileLineModel.MODEL, ProjectileLineModel::createBodyLayer);
    	event.registerLayerDefinition(ProjectileChipsModel.MODEL, ProjectileChipsModel::createBodyLayer);
    	event.registerLayerDefinition(FortunaBombModel.MODEL, FortunaBombModel::createBodyLayer);
    	event.registerLayerDefinition(SwampMineModel.MODEL, SwampMineModel::createBodyLayer);
    	//event.registerLayerDefinition(WaterBoulderModel.MODEL, WaterBoulderModel::createBodyLayer);
    	//event.registerLayerDefinition(VelaVortexModel.MODEL, VelaVortexModel::createBodyLayer);
    }

	@SubscribeEvent
    public static void itemColors(final RegisterColorHandlersEvent.Item event) {
		event.register((s, t) -> t == 1 ? Mth.hsvToRgb(((Util.getMillis() / 1000) % 360) / 360f, 1, 1) : -1, ModItems.cocktailCutlass.get());
		if (MeetYourFight.loadedGunsWithoutRoses()) event.register((s, t) -> t == 1 ? Mth.hsvToRgb(((Util.getMillis() / 1000) % 360) / 360f, 0.75f, 0.75f) : -1, CompatGWRItems.cocktailShotgun.get());
    }

	@SubscribeEvent
	public static void clientStuff(final FMLClientSetupEvent event) {

		//Same as Bow
		ItemProperties.register(ModItems.depthStar.get(), MeetYourFight.rl("charge"),
				(stack, world, entity, someint) -> entity == null || entity.getUseItem() != stack ? 0 : (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / 20.0F);
		ItemProperties.register(ModItems.depthStar.get(), MeetYourFight.rl("charging"),
				(stack, world, entity, someint) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1 : 0);
	}

}
