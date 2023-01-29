package lykrast.meetyourfight.entity;

import java.util.List;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.ai.MoveFrontOfTarget;
import lykrast.meetyourfight.entity.ai.StationaryAttack;
import lykrast.meetyourfight.entity.ai.VexMoveRandomGoal;
import lykrast.meetyourfight.entity.movement.VexMovementController;
import lykrast.meetyourfight.registry.ModEntities;
import lykrast.meetyourfight.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RosalyneEntity extends BossEntity {
	//I'll figure out later how to encode stuff, for now it's just phase
	private static final EntityDataAccessor<Byte> STATUS = SynchedEntityData.defineId(RosalyneEntity.class, EntityDataSerializers.BYTE);
	public static final int ENCASED = 0, BREAKING_OUT = 1, PHASE_1 = 2, SUMMONING = 3, PHASE_2 = 4, MADDENING = 5, PHASE_3 = 6;
	private final TargetingConditions spiritCountTargeting = TargetingConditions.forNonCombat().range(32).ignoreLineOfSight().ignoreInvisibilityTesting();
	
	public int attackCooldown;
	private int phase;
	
	public RosalyneEntity(EntityType<? extends RosalyneEntity> type, Level worldIn) {
		super(type, worldIn);
		moveControl = new VexMovementController(this);
		xpReward = 200;
		phase = 0;
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new PhaseTransition(this));
		//goalSelector.addGoal(2, new WaterAttack(this));
		goalSelector.addGoal(7, new MoveFrontOfTarget(this, 0.5));
		goalSelector.addGoal(8, new VexMoveRandomGoal(this, 0.25));
		goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
		goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
		targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
		targetSelector.addGoal(2, new HurtByTargetGoal(this));
	}
	
	public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 600)
        		.add(Attributes.ARMOR, 10).add(Attributes.ARMOR_TOUGHNESS, 4)
        		.add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
        		.add(Attributes.FOLLOW_RANGE, 64);
    }

	@Override
	public void move(MoverType typeIn, Vec3 pos) {
		super.move(typeIn, pos);
		checkInsideBlocks();
	}

	@Override
	public void tick() {
		noPhysics = true;
		super.tick();
		noPhysics = false;
		setNoGravity(true);
	}
	
	public static void spawn(Player player, Level world) {
		RandomSource rand = player.getRandom();
		RosalyneEntity dame = ModEntities.ROSALYNE.get().create(world);
		dame.moveTo(player.getX() + rand.nextInt(5) - 2, player.getY() + rand.nextInt(3) + 3, player.getZ() + rand.nextInt(5) - 2, rand.nextFloat() * 360 - 180, 0);
		dame.attackCooldown = 100;
		if (!player.getAbilities().instabuild) dame.setTarget(player);
		dame.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 2));

		dame.finalizeSpawn((ServerLevel) world, world.getCurrentDifficultyAt(dame.blockPosition()), MobSpawnType.EVENT, null, null);
		world.addFreshEntity(dame);
		dame.createSpirits();
	}
	
	private void createSpirits() {
		for (int i = 0; i < 4; i++) {
			RoseSpiritEntity spirit = ModEntities.ROSE_SPIRIT.get().create(level);
			spirit.moveTo(getX() + (i/2)*4 - 2, getY(), getZ() + (i%2)*4 - 2);
			spirit.setOwner(this);
			spirit.attackCooldown = 80 + 60*i;
			spirit.finalizeSpawn((ServerLevel)level, level.getCurrentDifficultyAt(blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
			level.addFreshEntity(spirit);
		}
	}
	
	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (source != DamageSource.OUT_OF_WORLD && getStatus() != PHASE_1 && getStatus() != PHASE_3) {
			if (amount > 0) playSound(SoundEvents.ANVIL_LAND, 1, 1);
			return false;
		}
		return super.hurt(source, amount);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(STATUS, (byte)0);
	}
	
	public int getStatus() {
		return entityData.get(STATUS);
	}
	
	public void setStatus(int status) {
		entityData.set(STATUS, (byte)status);
	}
	
	@Override
	public void aiStep() {
		super.aiStep();
		if ((phase == ENCASED || phase == PHASE_2) && tickCount % 20 == 0) {
			List<RoseSpiritEntity> list = level.getNearbyEntities(RoseSpiritEntity.class, spiritCountTargeting, this, getBoundingBox().inflate(32));
			for (RoseSpiritEntity spirit : list) {
				spirit.setOwner(this);
			}
			if (!list.isEmpty()) setHealth(getHealth() + 1);
		}
	}
	
	@Override
	public void customServerAiStep() {
		if (attackCooldown > 0) attackCooldown--;
		if (phase != getStatus()) phase = getStatus();
		//Start phase transitions
		if ((phase == ENCASED || phase == PHASE_2) && tickCount % 10 == 0) {
			if (level.getNearbyEntities(RoseSpiritEntity.class, spiritCountTargeting, this, getBoundingBox().inflate(32)).isEmpty()) {
				if (phase == ENCASED) {
					setStatus(BREAKING_OUT);
					phase = BREAKING_OUT;
				}
				else if (phase == PHASE_2) {
					setStatus(MADDENING);
					phase = MADDENING;
				}
			}
		}
		else if (phase == PHASE_1 && getHealth() < getMaxHealth() / 2) {
			setStatus(SUMMONING);
			phase = SUMMONING;
		}
		super.customServerAiStep();
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("Cooldown")) attackCooldown = compound.getInt("Cooldown");
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putInt("Cooldown", attackCooldown);
	}
	
	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.WITHER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.WITHER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.WITHER_DEATH;
	}

	@Override
	protected SoundEvent getMusic() {
		return ModSounds.musicMagnum.get();
	}
	
	@Override
	protected ResourceLocation getDefaultLootTable() {
		return MeetYourFight.rl("rosalyne");
	}
	
	private static class PhaseTransition extends StationaryAttack {
		private RosalyneEntity rosalyne;
		private int timer;

		public PhaseTransition(RosalyneEntity rosalyne) {
			super(rosalyne);
			this.rosalyne = rosalyne;
		}
		
		@Override
		public void start() {
			super.start();
			timer = 60;
		}

		@Override
		public void tick() {
			super.tick();
			timer--;
			if (timer <= 0) {
				switch (rosalyne.phase) {
					case BREAKING_OUT:
						rosalyne.setStatus(PHASE_1);
						rosalyne.level.explode(rosalyne, rosalyne.getX(), rosalyne.getY(), rosalyne.getZ(), 6, Explosion.BlockInteraction.NONE);
						break;
					case SUMMONING:
						rosalyne.setStatus(PHASE_2);
						rosalyne.createSpirits();
						break;
					case MADDENING:
						rosalyne.setStatus(PHASE_3);
						break;
				}
				rosalyne.attackCooldown = 100;
			}
		}

		@Override
		public boolean canUse() {
			return rosalyne.phase == BREAKING_OUT || rosalyne.phase == SUMMONING || rosalyne.phase == MADDENING;
		}
		
	}

}
