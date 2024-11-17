package lykrast.meetyourfight.registry;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.*;
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
	public static RegistryObject<EntityType<RosalyneEntity>> ROSALYNE;
	//Summons
	public static RegistryObject<EntityType<RoseSpiritEntity>> ROSE_SPIRIT;
	
	//Projectiles
	public static RegistryObject<EntityType<ProjectileLineEntity>> PROJECTILE_LINE;
	public static RegistryObject<EntityType<ProjectileTargetedEntity>> PROJECTILE_TARGETED;
	public static RegistryObject<EntityType<FortunaBombEntity>> FORTUNA_BOMB;
	public static RegistryObject<EntityType<FortunaCardEntity>> FORTUNA_CARD;
	public static RegistryObject<EntityType<SwampMineEntity>> SWAMP_MINE;

	static {
		BELLRINGER = REG.register("bellringer", () -> EntityType.Builder.<BellringerEntity>of(BellringerEntity::new, MobCategory.MONSTER).sized(0.6f, 1.95f)
				.setUpdateInterval(1).setTrackingRange(128).setShouldReceiveVelocityUpdates(true).build(""));
		DAME_FORTUNA = REG.register("dame_fortuna", () -> EntityType.Builder.<DameFortunaEntity>of(DameFortunaEntity::new, MobCategory.MONSTER).sized(0.6f, 2.325f)
				.setUpdateInterval(1).setTrackingRange(128).setShouldReceiveVelocityUpdates(true).build(""));
		SWAMPJAW = REG.register("swampjaw", () -> EntityType.Builder.<SwampjawEntity>of(SwampjawEntity::new, MobCategory.MONSTER).sized(2.6f, 1.6f)
				.setUpdateInterval(1).setTrackingRange(128).setShouldReceiveVelocityUpdates(true).build(""));
		ROSALYNE = REG.register("rosalyne", () -> EntityType.Builder.<RosalyneEntity>of(RosalyneEntity::new, MobCategory.MONSTER).sized(0.6f, 1.95f)
				.setUpdateInterval(1).setTrackingRange(128).setShouldReceiveVelocityUpdates(true).build(""));
		ROSE_SPIRIT = REG.register("rose_spirit", () -> EntityType.Builder.<RoseSpiritEntity>of(RoseSpiritEntity::new, MobCategory.MONSTER).sized(0.75f, 1.3125f)
				.setUpdateInterval(1).setTrackingRange(64).setShouldReceiveVelocityUpdates(true).build(""));

		PROJECTILE_LINE = REG.register("projectile_line", () -> EntityType.Builder.<ProjectileLineEntity>of(ProjectileLineEntity::new, MobCategory.MISC).sized(0.3125f, 0.3125f)
				.setUpdateInterval(1).setTrackingRange(64).setShouldReceiveVelocityUpdates(true).build(""));
		PROJECTILE_TARGETED = REG.register("projectile_targeted", () -> EntityType.Builder.<ProjectileTargetedEntity>of(ProjectileTargetedEntity::new, MobCategory.MISC).sized(0.3125f, 0.3125f)
				.setUpdateInterval(1).setTrackingRange(64).setShouldReceiveVelocityUpdates(true).build(""));
		FORTUNA_BOMB = REG.register("fortuna_bomb", () -> EntityType.Builder.<FortunaBombEntity>of(FortunaBombEntity::new, MobCategory.MISC).sized(0.3125f, 0.3125f)
				.setUpdateInterval(1).setTrackingRange(64).setShouldReceiveVelocityUpdates(true).build(""));
		FORTUNA_CARD = REG.register("fortuna_card", () -> EntityType.Builder.<FortunaCardEntity>of(FortunaCardEntity::new, MobCategory.MISC).sized(1.75f, 2.5f)
				.setUpdateInterval(1).setTrackingRange(64).setShouldReceiveVelocityUpdates(true).build(""));
		SWAMP_MINE = REG.register("swamp_mine", () -> EntityType.Builder.<SwampMineEntity>of(SwampMineEntity::new, MobCategory.MISC).sized(1, 1).setUpdateInterval(1)
				.setTrackingRange(64).setShouldReceiveVelocityUpdates(true).build(""));
	}

	@SubscribeEvent
	public static void registerEntityAttributes(final EntityAttributeCreationEvent event) {
		event.put(BELLRINGER.get(), BellringerEntity.createAttributes().build());
		event.put(DAME_FORTUNA.get(), DameFortunaEntity.createAttributes().build());
		event.put(SWAMPJAW.get(), SwampjawEntity.createAttributes().build());
		event.put(ROSALYNE.get(), RosalyneEntity.createAttributes().build());
		event.put(ROSE_SPIRIT.get(), RoseSpiritEntity.createAttributes().build());
	}
}
