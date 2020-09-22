package lykrast.meetyourfight.registry;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.GhostLineEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MeetYourFight.MODID)
public class ModEntities {
	public static final EntityType<GhostLineEntity> GHOST_LINE = EntityType.Builder
			.<GhostLineEntity>create(GhostLineEntity::new, EntityClassification.MISC)
			.size(0.3125f, 0.3125f).setUpdateInterval(10).setTrackingRange(64).setShouldReceiveVelocityUpdates(true)
			.build(MeetYourFight.MODID + ":bullet");

	@SubscribeEvent
	public static void regsiterEntities(final RegistryEvent.Register<EntityType<?>> event) {
		IForgeRegistry<EntityType<?>> reg = event.getRegistry();
		GHOST_LINE.setRegistryName(MeetYourFight.MODID, "ghost_line");
		reg.register(GHOST_LINE);
	}
}
