package lykrast.meetyourfight.registry;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.BellringerEntity;
import lykrast.meetyourfight.entity.DameFortunaEntity;
import lykrast.meetyourfight.entity.ProjectileLineEntity;
import lykrast.meetyourfight.entity.SwampMineEntity;
import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MeetYourFight.MODID)
public class ModEntities {
	//Bosses
	public static EntityType<BellringerEntity> BELLRINGER;
	public static EntityType<DameFortunaEntity> DAME_FORTUNA;
	public static EntityType<SwampjawEntity> SWAMPJAW;
	
	//Projectiles
	public static EntityType<ProjectileLineEntity> PROJECTILE_LINE;
	public static EntityType<SwampMineEntity> SWAMP_MINE;

	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public static void regsiterEntities(final RegistryEvent.Register<EntityType<?>> event) {
		IForgeRegistry<EntityType<?>> reg = event.getRegistry();
		//Ductape round 2 for the 1.18.2 port, because apparently we were not supposed to initialize stuff in static
		reg.registerAll(
					EntityType.Builder.<BellringerEntity>of(BellringerEntity::new, MobCategory.MONSTER)
					.sized(0.6f, 1.95f).setUpdateInterval(2).setTrackingRange(128).setShouldReceiveVelocityUpdates(true)
					.build("").setRegistryName(MeetYourFight.MODID, "bellringer"),
					EntityType.Builder.<DameFortunaEntity>of(DameFortunaEntity::new, MobCategory.MONSTER)
					.sized(0.6f, 2.325f).setUpdateInterval(2).setTrackingRange(128).setShouldReceiveVelocityUpdates(true)
					.build("").setRegistryName(MeetYourFight.MODID, "dame_fortuna"),
					EntityType.Builder.<SwampjawEntity>of(SwampjawEntity::new, MobCategory.MONSTER)
					.sized(2.6f, 1.6f).setUpdateInterval(2).setTrackingRange(128).setShouldReceiveVelocityUpdates(true)
					.build("").setRegistryName(MeetYourFight.MODID, "swampjaw"),
					EntityType.Builder
					.<ProjectileLineEntity>of(ProjectileLineEntity::new, MobCategory.MISC)
					.sized(0.3125f, 0.3125f).setUpdateInterval(1).setTrackingRange(64).setShouldReceiveVelocityUpdates(true)
					.build("").setRegistryName(MeetYourFight.MODID, "projectile_line"),
					EntityType.Builder.<SwampMineEntity>of(SwampMineEntity::new, MobCategory.MISC)
					.sized(1, 1).setUpdateInterval(1).setTrackingRange(64).setShouldReceiveVelocityUpdates(true)
					.build("").setRegistryName(MeetYourFight.MODID, "swamp_mine")
				);
		BELLRINGER = (EntityType<BellringerEntity>) reg.getValue(MeetYourFight.rl("bellringer"));
		DAME_FORTUNA = (EntityType<DameFortunaEntity>) reg.getValue(MeetYourFight.rl("dame_fortuna"));
		SWAMPJAW = (EntityType<SwampjawEntity>) reg.getValue(MeetYourFight.rl("swampjaw"));
		PROJECTILE_LINE = (EntityType<ProjectileLineEntity>) reg.getValue(MeetYourFight.rl("projectile_line"));
		SWAMP_MINE = (EntityType<SwampMineEntity>) reg.getValue(MeetYourFight.rl("swamp_mine"));
	}

	@SubscribeEvent
	public static void registerEntityAttributes(final EntityAttributeCreationEvent event) {
		event.put(BELLRINGER, BellringerEntity.createAttributes().build());
		event.put(DAME_FORTUNA, DameFortunaEntity.createAttributes().build());
		event.put(SWAMPJAW, SwampjawEntity.createAttributes().build());
	}
}
