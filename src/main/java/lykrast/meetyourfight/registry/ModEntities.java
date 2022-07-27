package lykrast.meetyourfight.registry;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.BellringerEntity;
import lykrast.meetyourfight.entity.DameFortunaEntity;
import lykrast.meetyourfight.entity.ProjectileLineEntity;
import lykrast.meetyourfight.entity.SwampMineEntity;
import lykrast.meetyourfight.entity.SwampjawEntity;
import lykrast.meetyourfight.entity.VelaEntity;
import lykrast.meetyourfight.entity.VelaVortexEntity;
import lykrast.meetyourfight.entity.WaterBoulderEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MeetYourFight.MODID)
public class ModEntities {
	public static final DeferredRegister<EntityType<?>> REG = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MeetYourFight.MODID);
	//Bosses
	public static RegistryObject<EntityType<BellringerEntity>> BELLRINGER;
	public static RegistryObject<EntityType<DameFortunaEntity>> DAME_FORTUNA;
	public static RegistryObject<EntityType<SwampjawEntity>> SWAMPJAW;
	public static RegistryObject<EntityType<VelaEntity>> VELA;
	
	//Projectiles
	public static RegistryObject<EntityType<ProjectileLineEntity>> PROJECTILE_LINE;
	public static RegistryObject<EntityType<SwampMineEntity>> SWAMP_MINE;
	public static RegistryObject<EntityType<WaterBoulderEntity>> WATER_BOULDER;
	public static RegistryObject<EntityType<VelaVortexEntity>> VELA_VORTEX;

	static {
		BELLRINGER = REG.register("bellringer", () -> EntityType.Builder.<BellringerEntity>of(BellringerEntity::new, MobCategory.MONSTER).sized(0.6f, 1.95f)
				.setUpdateInterval(2).setTrackingRange(128).setShouldReceiveVelocityUpdates(true).build(""));
		DAME_FORTUNA = REG.register("dame_fortuna", () -> EntityType.Builder.<DameFortunaEntity>of(DameFortunaEntity::new, MobCategory.MONSTER).sized(0.6f, 2.325f)
				.setUpdateInterval(2).setTrackingRange(128).setShouldReceiveVelocityUpdates(true).build(""));
		SWAMPJAW = REG.register("swampjaw", () -> EntityType.Builder.<SwampjawEntity>of(SwampjawEntity::new, MobCategory.MONSTER).sized(2.6f, 1.6f)
				.setUpdateInterval(2).setTrackingRange(128).setShouldReceiveVelocityUpdates(true).build(""));
		VELA = REG.register("vela", () -> EntityType.Builder.<VelaEntity>of(VelaEntity::new, MobCategory.MONSTER).sized(0.6f, 2.325f)
				.setUpdateInterval(2).setTrackingRange(128).setShouldReceiveVelocityUpdates(true).build(""));

		PROJECTILE_LINE = REG.register("projectile_line", () -> EntityType.Builder.<ProjectileLineEntity>of(ProjectileLineEntity::new, MobCategory.MISC).sized(0.3125f, 0.3125f)
				.setUpdateInterval(1).setTrackingRange(64).setShouldReceiveVelocityUpdates(true).build(""));
		SWAMP_MINE = REG.register("swamp_mine", () -> EntityType.Builder.<SwampMineEntity>of(SwampMineEntity::new, MobCategory.MISC).sized(1, 1).setUpdateInterval(1)
				.setTrackingRange(64).setShouldReceiveVelocityUpdates(true).build(""));
		WATER_BOULDER = REG.register("water_boulder", () -> EntityType.Builder.<WaterBoulderEntity>of(WaterBoulderEntity::new, MobCategory.MISC).sized(3,3).setUpdateInterval(1)
				.setTrackingRange(64).setShouldReceiveVelocityUpdates(true).build(""));
		VELA_VORTEX = REG.register("vela_vortex", () -> EntityType.Builder.<VelaVortexEntity>of(VelaVortexEntity::new, MobCategory.MISC).sized(2.5f, 0.5f).setUpdateInterval(1)
				.setTrackingRange(64).setShouldReceiveVelocityUpdates(true).build(""));
	}

	@SubscribeEvent
	public static void registerEntityAttributes(final EntityAttributeCreationEvent event) {
		event.put(BELLRINGER.get(), BellringerEntity.createAttributes().build());
		event.put(DAME_FORTUNA.get(), DameFortunaEntity.createAttributes().build());
		event.put(SWAMPJAW.get(), SwampjawEntity.createAttributes().build());
		event.put(VELA.get(), VelaEntity.createAttributes().build());
	}
}
