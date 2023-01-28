package lykrast.meetyourfight.entity;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.ai.MoveAroundTargetOrthogonal;
import lykrast.meetyourfight.entity.ai.StationaryAttack;
import lykrast.meetyourfight.entity.ai.VexMoveRandomGoal;
import lykrast.meetyourfight.entity.movement.VexMovementController;
import lykrast.meetyourfight.registry.ModEntities;
import lykrast.meetyourfight.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class VelaEntity extends BossEntity {
	//Copied from Fortuna, because 2 attack and 3 phases
	//0x0000RRAA with R for rage (0-2) and A for attack (0-2)
	private static final EntityDataAccessor<Byte> STATUS = SynchedEntityData.defineId(VelaEntity.class, EntityDataSerializers.BYTE);
	public static final int NO_ATTACK = 0, WATER_ATTACK = 1, AIR_ATTACK = 2;
	private static final int ATTACK_MASK = 0b11, RAGE_MASK = ~ATTACK_MASK;
	
	public int waterCooldown, airCooldown;
	private int rage;
	
	public VelaEntity(EntityType<? extends VelaEntity> type, Level worldIn) {
		super(type, worldIn);
		moveControl = new VexMovementController(this);
		xpReward = 150;
		rage = 0;
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(2, new WaterAttack(this));
		goalSelector.addGoal(3, new AirAttack(this));
		goalSelector.addGoal(7, new MoveAroundTargetOrthogonal(this));
		goalSelector.addGoal(8, new VexMoveRandomGoal(this));
		goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
		goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
		targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
		targetSelector.addGoal(2, new HurtByTargetGoal(this));
	}
	
	public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 500).add(Attributes.ARMOR, 5).add(Attributes.FOLLOW_RANGE, 64);
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
		VelaEntity dame = ModEntities.VELA.get().create(world);
		dame.moveTo(player.getX() + rand.nextInt(5) - 2, player.getY() + rand.nextInt(3) + 3, player.getZ() + rand.nextInt(5) - 2, rand.nextFloat() * 360 - 180, 0);
		dame.waterCooldown = 100;
		dame.airCooldown = 150;
		if (!player.getAbilities().instabuild) dame.setTarget(player);
		dame.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 2));

		dame.finalizeSpawn((ServerLevel) world, world.getCurrentDifficultyAt(dame.blockPosition()), MobSpawnType.EVENT, null, null);
		world.addFreshEntity(dame);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(STATUS, (byte)0);
	}
	
	public int getAttack() {
		return entityData.get(STATUS) & ATTACK_MASK;
	}
	
	public void setAttack(int attack) {
		int rage = entityData.get(STATUS) & RAGE_MASK;
		entityData.set(STATUS, (byte)(rage | attack));
	}
	
	public int getRage() {
		return (entityData.get(STATUS) & RAGE_MASK) >> 2;
	}
	
	public void setRage(int rage) {
		int attack = entityData.get(STATUS) & ATTACK_MASK;
		entityData.set(STATUS, (byte)((rage << 2) | attack));
	}
	
	private int getRageTarget() {
		//+1 rage every 1/3 of life lost
		float health = getHealth();
		float third = getMaxHealth() / 3f;
		if (health <= third) return 2;
		else if (health >= third * 2) return 0;
		else return 1;
	}
	
	@Override
	public void customServerAiStep() {
		if (waterCooldown > 0) waterCooldown--;
		if (airCooldown > 0) airCooldown--;
		int newrage = getRageTarget();
		if (newrage > rage) {
			rage = newrage;
			setRage(rage);
		}
		super.customServerAiStep();
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("WCooldown")) waterCooldown = compound.getInt("WCooldown");
		if (compound.contains("ACooldown")) airCooldown = compound.getInt("ACooldown");
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putInt("WCooldown", waterCooldown);
		compound.putInt("ACooldown", airCooldown);
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
		return MeetYourFight.rl("vela");
	}
	
	private WaterBoulderEntity readyBoulder(Entity target) {
		WaterBoulderEntity boulder = new WaterBoulderEntity(level, this, target);
		boulder.setOwner(this);
		boulder.setPos(getX(), getY() + 1, getZ());
		return boulder;
	}
	
	private VelaVortexEntity readyVortex(double x, double y, double z) {
		VelaVortexEntity vortex = new VelaVortexEntity(level, this);
		vortex.setOwner(this);
		vortex.setPos(x, y, z);
		return vortex;
	}
	
	private static class AirAttack extends StationaryAttack {
		private VelaEntity vela;
		private LivingEntity target;
		private int attackDelay, interval, remaining, pattern;
		//Pattern: each digit is one attack, 0 for all 3, 1 for left, 2 for middle, 3 for right, 4 for left + right
		//And we divide so it's right to left :(

		public AirAttack(VelaEntity vela) {
			super(vela);
			this.vela = vela;
		}

		@Override
		public boolean canUse() {
			return vela.airCooldown <= 0 && vela.getTarget() != null && vela.getTarget().isAlive() && !vela.getMoveControl().hasWanted();
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void start() {
			super.start();
			vela.airCooldown = 2;
			attackDelay = 20;
			target = vela.getTarget();
			switch (vela.rage) {
				default:
				case 0:
					//0
					remaining = 2;
					pattern = 0;
					interval = 30;
					break;
				case 1:
					switch (vela.random.nextInt(3)) {
						case 0:
							remaining = 3;
							pattern = 0;
							interval = 20;
							break;
						case 1:
							remaining = 9;
							pattern = 123123123;
							interval = 7;
							break;
						case 2:
							remaining = 6;
							pattern = 242422;
							interval = 10;
							break;
					}
					break;
			}
			vela.setAttack(AIR_ATTACK);
		}

		@Override
		public void tick() {
			super.tick();
			vela.airCooldown = 2;
			if (vela.waterCooldown <= 2) vela.waterCooldown = 2;
			attackDelay--;
			if (attackDelay <= 0) {
				performAttack(pattern % 10);
				remaining--;
				if (remaining <= 0) stop();
				else {
					attackDelay = interval;
					pattern /= 10;
				}
			}
		}
		
		private void performAttack(int attack) {
			//BlockPos rounds values so +0.5 for center of block
			BlockPos tgt = target.blockPosition();
			double tx = tgt.getX() + 0.5;
			double tz = tgt.getZ() + 0.5;
			double ty = tgt.getY() + 0.1;
			Vec3 perp = new Vec3(vela.getX() - tx, 0, vela.getZ() - tz).normalize().yRot((float)Math.PI / 2F).scale(8);
			//Prevents lines being unjumpable if an attack is launched mid jump
			if (!target.isOnGround() && !target.isInWater() && !vela.level.getBlockState(tgt.below()).getMaterial().blocksMotion()) ty -= 1;

			//0 for all 3, 1 for left, 2 for middle, 3 for right, 4 for left + right
			for (int i = -1; i <= 1; i++) {
				//Yep it's ad hoc and ugly
				if (i == -1 && (attack == 2 || attack == 3)) continue;
				if (i == 0 && (attack == 1 || attack == 3 || attack == 4)) continue;
				if (i == 1 && (attack == 1 || attack == 2)) continue;
				VelaVortexEntity vortex = vela.readyVortex(vela.getX(), vela.getY() + 2, vela.getZ());
				vortex.setUpTowards(tx + i*perp.x, ty + 0.5 + i*0.1, tz + i*perp.z, 1.5);
				vela.level.addFreshEntity(vortex);
			}
		}

		@Override
		public void stop() {
			vela.airCooldown = 60 + vela.random.nextInt(21);
			vela.setAttack(NO_ATTACK);
		}

		@Override
		public boolean canContinueToUse() {
			return attackDelay > 0 && target.isAlive();
		}
		
	}
	
	private static class WaterAttack extends StationaryAttack {
		private VelaEntity vela;
		private LivingEntity target;
		private int attackDelay, chosenAttack;

		public WaterAttack(VelaEntity vela) {
			super(vela);
			this.vela = vela;
		}

		@Override
		public boolean canUse() {
			return vela.waterCooldown <= 0 && vela.getTarget() != null && vela.getTarget().isAlive() && !vela.getMoveControl().hasWanted();
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void start() {
			super.start();
			vela.waterCooldown = 2;
			if (vela.airCooldown <= 2) vela.airCooldown = 2;
			attackDelay = 20;
			target = vela.getTarget();
			chosenAttack = vela.random.nextInt(2);
			vela.setAttack(WATER_ATTACK);
		}

		@Override
		public void tick() {
			super.tick();
			vela.waterCooldown = 2;
			attackDelay--;
			if (attackDelay <= 0) {
				performAttack();
				stop();
			}
		}
		
		private void performAttack() {
			switch (chosenAttack) {
				case 0:
					//Vertical attack
					WaterBoulderEntity boulder = vela.readyBoulder(target);
					boulder.setUp(60, 0, -0.5, 0, 0, 5, 0);
					vela.level.addFreshEntity(boulder);
					break;
				case 1:
					//Horizontal attack
					Direction dir = Direction.getNearest(target.getX() - vela.getX(), 0, target.getZ() - vela.getZ());
					//Can have any rotation except backwards
					switch (vela.random.nextInt(3)) {
						case 1:
							dir = dir.getClockWise();
							break;
						case 2:
							dir = dir.getCounterClockWise();
							break;
					}
					horizontalBoulder(dir, 0);
					//When rage do that but with an easier wind attack
					//int skip = vela.random.nextInt(3) - 1;
					//for (int i = -1; i <= 1; i++) {
					//	if (i != skip) horizontalBoulder(dir, i);
					//}
					break;
			}
		}
		
		private void horizontalBoulder(Direction dir, int offset) {
			Direction perp = dir.getClockWise();
			WaterBoulderEntity boulder = vela.readyBoulder(target);
			boulder.setUp(60, dir.getStepX() * 0.5, 0, dir.getStepZ() * 0.5, perp.getStepX()*offset*3 -dir.getStepX() * 5, 0, perp.getStepZ()*offset*3 -dir.getStepZ() * 5);
			vela.level.addFreshEntity(boulder);
		}

		@Override
		public void stop() {
			vela.waterCooldown = 100 + vela.random.nextInt(41);
			vela.setAttack(NO_ATTACK);
		}

		@Override
		public boolean canContinueToUse() {
			return attackDelay > 0 && target.isAlive();
		}
		
	}

}
