package lykrast.meetyourfight.entity;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.ai.MoveAroundTarget;
import lykrast.meetyourfight.entity.ai.VexMoveRandomGoal;
import lykrast.meetyourfight.entity.movement.VexMovementController;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RoseSpiritEntity extends Monster {
	private static final EntityDataAccessor<Byte> STATUS = SynchedEntityData.defineId(RoseSpiritEntity.class, EntityDataSerializers.BYTE);
	public static final int HIDING = 0, RISING = 1, OUT = 2, ATTACKING = 3, RETRACTING = 4, HURT = 5, RETRACTING_HURT = 6;
	
	public int attackCooldown;
	//Client side animation
	public int prevStatus, animDur, animProg;
	
	public RoseSpiritEntity(EntityType<? extends RoseSpiritEntity> type, Level worldIn) {
		super(type, worldIn);
		moveControl = new VexMovementController(this);
		xpReward = 5;
		prevStatus = getStatus();
		animDur = 1;
		animProg = 1;
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(2, new HideAfterHit(this));
		goalSelector.addGoal(3, new BurstAttack(this));
		goalSelector.addGoal(7, new MoveAroundTarget(this, 0.5));
		goalSelector.addGoal(8, new VexMoveRandomGoal(this));
		goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
		goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
		targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
		targetSelector.addGoal(2, new HurtByTargetGoal(this));
	}
	
	public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 40).add(Attributes.ARMOR, 5).add(Attributes.FOLLOW_RANGE, 64);
    }

	@Override
	public void move(MoverType typeIn, Vec3 pos) {
		super.move(typeIn, pos);
		checkInsideBlocks();
	}
	
	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (source != DamageSource.OUT_OF_WORLD && getStatus() == HIDING && amount > 0) {
			playSound(SoundEvents.ANVIL_LAND, 1, 1);
			return false;
		}
		if (super.hurt(source, amount)) {
			if (amount >= 3) setStatus(HURT);
			return true;
		}
		return false;
	}

	@Override
	public void tick() {
		noPhysics = true;
		super.tick();
		noPhysics = false;
		setNoGravity(true);
		
		//Animations
		if (level.isClientSide) {
			if (prevStatus != getStatus()) {
				prevStatus = getStatus();
				animProg = 0;
				if (prevStatus == RISING || prevStatus == RETRACTING) animDur = 10;
				else if (prevStatus == RETRACTING_HURT) animDur = 5;
			}
			else if (animProg < animDur) animProg++;
		}
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
	public void customServerAiStep() {
		if (attackCooldown > 0) attackCooldown--;
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
		return SoundEvents.VEX_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.VEX_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.VEX_DEATH;
	}
	
	@Override
	protected ResourceLocation getDefaultLootTable() {
		return MeetYourFight.rl("rose_spirit");
	}
	
	private ProjectileLineEntity readyAttack() {
		ProjectileLineEntity ghost = new ProjectileLineEntity(level, this, 0, 0, 0);
		ghost.setOwner(this);
		ghost.setPos(getX(), getY()+0.625, getZ());
		ghost.setVariant(ProjectileLineEntity.VAR_ROSE);
		return ghost;
	}
	
	private static class HideAfterHit extends Goal {
		private RoseSpiritEntity mob;
		private int timer;
		
		public HideAfterHit(RoseSpiritEntity mob) {
			this.mob = mob;
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void start() {
			mob.attackCooldown = 100 + mob.random.nextInt(61);
			timer = 45;
		}
		
		@Override
		public void tick() {
			timer--;
			if (timer <= 0) mob.setStatus(HIDING);
			else if (timer == 5) mob.setStatus(RETRACTING_HURT);
		}
		
		@Override
		public boolean canUse() {
			return mob.getStatus() == HURT || mob.getStatus() == RETRACTING_HURT;
		}
	}
	
	//The regular attacks
	private static class BurstAttack extends Goal {
		private RoseSpiritEntity mob;
		private LivingEntity target;
		private int attackRemaining, attackDelay, phase;
		//0 = rise, 1 = wait a bit, 2 = shoot, 3 = wait a bit, 4 = retract, 5 = finished

		public BurstAttack(RoseSpiritEntity mob) {
			this.mob = mob;
		}

		@Override
		public boolean canUse() {
			return mob.attackCooldown <= 0 && mob.getTarget() != null && mob.getTarget().isAlive();
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void start() {
			mob.attackCooldown = 2;
			attackDelay = 10;
			attackRemaining = 3 + mob.random.nextInt(8);
			target = mob.getTarget();
			phase = 0;
			mob.setStatus(RISING);
		}

		@Override
		public void tick() {
			mob.attackCooldown = 2;
			attackDelay--;
			if (attackDelay <= 0) {
				switch (phase) {
					case 0:
						mob.setStatus(OUT);
						phase = 1;
						attackDelay = 20 + mob.random.nextInt(41);
						break;
					case 1:
						mob.setStatus(ATTACKING);
						phase = 2;
						attackDelay = 20;
						mob.playSound(SoundEvents.GHAST_WARN, 1, 1);
						break;
					case 2:
						attackDelay = 20;
						attackRemaining--;
						performAttack();
						if (attackRemaining <= 0) {
							phase = 3;
							mob.setStatus(OUT);
							attackDelay = 20 + mob.random.nextInt(41);
						}
						break;
					case 3:
						phase = 4;
						attackDelay = 10;
						mob.setStatus(RETRACTING);
						break;
					case 4:
						phase = 5;
						mob.setStatus(HIDING);
						stop();
						break;
				}
			}
		}
		
		private void performAttack() {
			double sx = mob.getX();
			double sy = mob.getY();
			double sz = mob.getZ();

			Vec3 dir = new Vec3(target.getX() - sx,  target.getY()+1 - sy, target.getZ() - sz).normalize().scale(2);
			ProjectileLineEntity ghost = mob.readyAttack();
			ghost.setUp(1, dir.x, dir.y, dir.z, sx, sy, sz);
			mob.level.addFreshEntity(ghost);
			mob.playSound(SoundEvents.GHAST_SHOOT, 1, 1);
		}

		@Override
		public void stop() {
			mob.attackCooldown = 60 + mob.random.nextInt(41);
		}

		@Override
		public boolean canContinueToUse() {
			return phase <= 4 && target.isAlive() && mob.getStatus() != HURT && mob.getStatus() != RETRACTING_HURT;
		}
		
	}

}
