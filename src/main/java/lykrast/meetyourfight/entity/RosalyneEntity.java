package lykrast.meetyourfight.entity;

import java.util.EnumSet;
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
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RosalyneEntity extends BossEntity implements PowerableMob {
	//Phase is lowest 3 bits, rest is animation 0xAAAAAPPP
	private static final EntityDataAccessor<Byte> STATUS = SynchedEntityData.defineId(RosalyneEntity.class, EntityDataSerializers.BYTE);
	public static final int ENCASED = 0, BREAKING_OUT = 1, PHASE_1 = 2, SUMMONING = 3, PHASE_2 = 4, MADDENING = 5, PHASE_3 = 6;
	public static final int ANIM_NEUTRAL = 0, ANIM_ARM_OUT_UP = 1, ANIM_ARM_IN_UP = 2, ANIM_ARM_OUT_DN = 3, ANIM_ARM_IN_DN = 4, ANIM_PREPARE_DASH = 5,
			ANIM_BROKE_OUT = 6, ANIM_SUMMONING = 7, ANIM_MADDENING = 8, ANIM_PREPARE_CRASH = 9, ANIM_SWING_CRASH = 10;
	private static final int PHASE_MASK = 0b111, ANIMATION_MASK = ~PHASE_MASK;
	private final TargetingConditions spiritCountTargeting = TargetingConditions.forNonCombat().range(32).ignoreLineOfSight().ignoreInvisibilityTesting();
	
	public int attackCooldown;
	private int phase;
	private int nextAttack;
	
	public int clientAnim, prevAnim, animProg, animDur;
	
	public RosalyneEntity(EntityType<? extends RosalyneEntity> type, Level worldIn) {
		super(type, worldIn);
		moveControl = new VexMovementController(this).slowdown(0.1);
		xpReward = 200;
		phase = 0;
		clientAnim = ANIM_NEUTRAL;
		prevAnim = ANIM_NEUTRAL;
		animProg = 1;
		animDur = 1;
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new PhaseTransition(this));
		goalSelector.addGoal(2, new AdvanceAndSwingAttack(this));
		goalSelector.addGoal(2, new CircleAndDashAttack(this));
		goalSelector.addGoal(2, new VerticalCrashAttack(this));
		goalSelector.addGoal(7, new MoveFrontOfTarget(this, 0.5));
		goalSelector.addGoal(8, new VexMoveRandomGoal(this, 0.25));
		goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
		goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
		targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
		targetSelector.addGoal(2, new HurtByTargetGoal(this));
	}
	
	public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 500)
        		.add(Attributes.ARMOR, 8).add(Attributes.ARMOR_TOUGHNESS, 4)
        		.add(Attributes.KNOCKBACK_RESISTANCE, 1)
        		.add(Attributes.ATTACK_DAMAGE, 24)
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
		
		if (level.isClientSide()) {
			int newanim = getAnimation();
			if (clientAnim != newanim) {
				prevAnim = clientAnim;
				clientAnim = newanim;
				animProg = 0;
				animDur = 5;
				//Animations with slower transitions
				if (prevAnim == ANIM_BROKE_OUT) animDur = 60;
				else if (clientAnim == ANIM_SUMMONING) animDur = 10;
				else if (prevAnim == ANIM_SUMMONING) animDur = 20;
			}
			else if (animProg < animDur) animProg++;
		}
	}
	
	public float getAnimProgress(float partial) {
		return Mth.clamp((animProg + partial) / animDur, 0, 1);
	}
	
	public static void spawn(Player player, Level world) {
		RandomSource rand = player.getRandom();
		RosalyneEntity dame = ModEntities.ROSALYNE.get().create(world);
		dame.moveTo(player.getX() + rand.nextInt(5) - 2, player.getY() + rand.nextInt(3) + 3, player.getZ() + rand.nextInt(5) - 2, rand.nextFloat() * 360 - 180, 0);
		dame.attackCooldown = 100;
		if (!player.getAbilities().instabuild) dame.setTarget(player);

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
		if (!source.isBypassInvul() && getPhase() != PHASE_1 && getPhase() != PHASE_3) {
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
	
	public int getPhase() {
		return entityData.get(STATUS) & PHASE_MASK;
	}
	
	public void setPhase(int phase) {
		int anim = entityData.get(STATUS) & ANIMATION_MASK;
		entityData.set(STATUS, (byte)(anim | phase));
	}
	
	public int getAnimation() {
		return (entityData.get(STATUS) & ANIMATION_MASK) >> 3;
	}
	
	public void setAnimation(int anim) {
		int phase = entityData.get(STATUS) & PHASE_MASK;
		entityData.set(STATUS, (byte)((anim << 3) | phase));
	}

	@Override
	public boolean isPowered() {
		//For the EnergySwirl (wither armor) layer
		int phase = getPhase();
		return phase == SUMMONING || phase == PHASE_2 || phase == MADDENING;
	}
	
	public void swing() {
        for(LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(2, 0.2, 2))) {
        	if (!(entity instanceof RosalyneEntity) && !(entity instanceof RoseSpiritEntity) && entity.isAlive()) doHurtTarget(entity);
        }
        playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1, 1);
        //attack cycle goes OUTDN, INUP, OUTUP, INDN, repeat
        switch (getAnimation()) {
            //special case for the vertical swing of the crash attack
        	case ANIM_PREPARE_CRASH:
        		setAnimation(ANIM_SWING_CRASH);
        		break;
        	case ANIM_ARM_OUT_DN:
        		setAnimation(ANIM_ARM_IN_UP);
        		break;
        	case ANIM_ARM_IN_UP:
        		setAnimation(ANIM_ARM_OUT_UP);
        		break;
        	case ANIM_ARM_OUT_UP:
        		setAnimation(ANIM_ARM_IN_DN);
        		break;
        	default:
        		setAnimation(ANIM_ARM_OUT_DN);
        		break;
        }
	}
	
	@Override
	public void aiStep() {
		//That's where the ender dragon detects crystals so copying that
		super.aiStep();
		if ((phase == ENCASED || phase == PHASE_2) && tickCount % 20 == 0) {
			List<RoseSpiritEntity> list = level.getNearbyEntities(RoseSpiritEntity.class, spiritCountTargeting, this, getBoundingBox().inflate(32));
			for (RoseSpiritEntity spirit : list) {
				spirit.setOwner(this);
			}
			//Healing can mess up a /kill so putting a check here
			if (!list.isEmpty() && !isRemoved()) setHealth(getHealth() + 1);
		}
	}
	
	@Override
	public void customServerAiStep() {
		if (attackCooldown > 0) attackCooldown--;
		if (phase != getPhase()) phase = getPhase();
		//Start phase transitions
		if ((phase == ENCASED || phase == PHASE_2) && tickCount % 10 == 0) {
			if (level.getNearbyEntities(RoseSpiritEntity.class, spiritCountTargeting, this, getBoundingBox().inflate(32)).isEmpty()) {
				if (phase == ENCASED) {
					setPhase(BREAKING_OUT);
					phase = BREAKING_OUT;
				}
				else if (phase == PHASE_2) {
					setPhase(MADDENING);
					phase = MADDENING;
				}
			}
		}
		else if (phase == PHASE_1 && getHealth() < getMaxHealth() / 2) {
			setPhase(SUMMONING);
			phase = SUMMONING;
		}
		super.customServerAiStep();
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("Cooldown")) attackCooldown = compound.getInt("Cooldown");
		if (compound.contains("Phase")) setPhase(compound.getInt("Phase"));
		if (compound.contains("NxtAt")) nextAttack = compound.getInt("NxtAt");
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putInt("Cooldown", attackCooldown);
		compound.putInt("Phase", getPhase());
		compound.putInt("NxtAt", nextAttack);
	}
	
	@Override
	protected SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return ModSounds.rosalyneHurt.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.rosalyneHurt.get();
	}

	@Override
	protected SoundEvent getMusic() {
		return ModSounds.musicMagnum.get();
	}
	
	@Override
	protected ResourceLocation getDefaultLootTable() {
		return MeetYourFight.rl("rosalyne");
	}
	
	private void rollNextAttack(int ignore) {
		if (ignore >= 0) {
			nextAttack = random.nextInt(2);
			if (nextAttack >= ignore) nextAttack++;
		}
		else nextAttack = random.nextInt(3);
	}
	
	private void cooldownNextAttack() {
		attackCooldown = 60 + random.nextInt(21);
		if (phase == PHASE_2) attackCooldown += 20;
		else if (phase == PHASE_3) attackCooldown -= 40;
	}
	
	private static class PhaseTransition extends StationaryAttack {
		private RosalyneEntity rosalyne;
		private int timer;
		//Putting that as a constant so that I write DURATION-20 instead of 60 so clearer
		private static final int DURATION = 80;

		public PhaseTransition(RosalyneEntity rosalyne) {
			super(rosalyne);
			this.rosalyne = rosalyne;
		}
		
		@Override
		public void start() {
			super.start();
			timer = 80;
		}

		@Override
		public void tick() {
			super.tick();
			timer--;
			//Start the custom animation a bit after stopping
			if (timer == DURATION-10) {
				switch (rosalyne.phase) {
					case BREAKING_OUT:
						//Actually we won't see this one but the transition to neutral is slow
						rosalyne.setAnimation(ANIM_BROKE_OUT);
						break;
					case SUMMONING:
						rosalyne.setAnimation(ANIM_SUMMONING);
						break;
					case MADDENING:
						rosalyne.setAnimation(ANIM_MADDENING);
						break;
				}
			}
			if (timer <= DURATION-30 && timer % 20 == 10) {
				switch (rosalyne.phase) {
					case BREAKING_OUT:
						rosalyne.playSound(SoundEvents.STONE_BREAK);
						break;
					case MADDENING:
						rosalyne.playSound(ModSounds.rosalyneCrack.get());
						break;
				}
			}
			if (timer <= 0) {
				switch (rosalyne.phase) {
					case BREAKING_OUT:
						rosalyne.setPhase(PHASE_1);
						rosalyne.level.explode(rosalyne, rosalyne.getX(), rosalyne.getY(), rosalyne.getZ(), 6, Explosion.BlockInteraction.NONE);
						break;
					case SUMMONING:
						rosalyne.setPhase(PHASE_2);
						rosalyne.createSpirits();
						break;
					case MADDENING:
						rosalyne.setPhase(PHASE_3);
						rosalyne.level.explode(rosalyne, rosalyne.getX(), rosalyne.getY(), rosalyne.getZ(), 4, Explosion.BlockInteraction.NONE);
						break;
				}
				rosalyne.setAnimation(ANIM_NEUTRAL);
				rosalyne.attackCooldown = 100;
			}
		}

		@Override
		public boolean canUse() {
			return rosalyne.phase == BREAKING_OUT || rosalyne.phase == SUMMONING || rosalyne.phase == MADDENING;
		}
		
	}
	
	private static class AdvanceAndSwingAttack extends Goal {
		//This is attack 0
		private RosalyneEntity rosalyne;
		private int timer, swingsLeft, attackPhase;
		private double holdx, holdy, holdz;
		//attackPhase 0 = approaching, 1 = preparing to swing, 2 = swinging
		
		public AdvanceAndSwingAttack(RosalyneEntity rosalyne) {
			this.rosalyne = rosalyne;
			setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}
		
		@Override
		public void start() {
			timer = 100;
			swingsLeft = 3 + rosalyne.random.nextInt(2);
			if (rosalyne.phase == PHASE_3) swingsLeft = 8 + rosalyne.random.nextInt(5);
			attackPhase = 0;
			LivingEntity target = rosalyne.getTarget();
			rosalyne.moveControl.setWantedPosition(target.getX(), target.getY(), target.getZ(), 4);
		}
		
		@Override
		public void tick() {
			timer--;
			LivingEntity target = rosalyne.getTarget();
			//Approaching target
			if (attackPhase == 0) {
				if (timer <= 0 || rosalyne.distanceToSqr(target) < 2) {
					holdx = rosalyne.getX();
					holdy = target.getY();
					holdz = rosalyne.getZ();
					rosalyne.moveControl.setWantedPosition(holdx, holdy, holdz, 4);
					attackPhase = 1;
					timer = 25;
					rosalyne.setAnimation(ANIM_ARM_OUT_DN);
				}
				else {
					rosalyne.moveControl.setWantedPosition(target.getX(), target.getY(), target.getZ(), 4);
				}
			}
			//Preparing to swing
			else if (attackPhase == 1) {
				rosalyne.moveControl.setWantedPosition(holdx, holdy, holdz, 4);
				if (timer <= 0) {
					attackPhase = 2;
					rosalyne.swing();
					swingsLeft--;
					timer = rosalyne.phase == PHASE_3 ? 12 : 20;
				}
			}
			//Swinging wildly while approaching
			else {
				rosalyne.moveControl.setWantedPosition(target.getX(), target.getY(), target.getZ(), 0.5);
				if (timer <= 0) {
					if (swingsLeft > 0) {
						rosalyne.swing();
						swingsLeft--;
						timer = rosalyne.phase == PHASE_3 ? 12 : 20;
					}
				}
			}
		}
		
		@Override
		public void stop() {
			rosalyne.cooldownNextAttack();
			rosalyne.setAnimation(ANIM_NEUTRAL);
			rosalyne.rollNextAttack(0);
		}

		@Override
		public boolean canUse() {
			return rosalyne.nextAttack == 0 && (rosalyne.phase == PHASE_1 || rosalyne.phase == PHASE_2 || rosalyne.phase == PHASE_3) && rosalyne.getTarget() != null && rosalyne.getTarget().isAlive() && rosalyne.attackCooldown <= 0;
		}
		
		@Override
		public boolean canContinueToUse() {
			return canUse() && (swingsLeft > 0 || timer > 0);
		}
		
	}
	
	private static class CircleAndDashAttack extends Goal {
		//This is attack 1
		private RosalyneEntity rosalyne;
		private int timer, swingsLeft, attackPhase;
		private double holdx, holdy, holdz;
		private Vec3 offset;
		//attackPhase 0 = approaching, 1 = circling, 2 = preparing to dash, 3 = dash, 4 = hold pose
		
		public CircleAndDashAttack(RosalyneEntity rosalyne) {
			this.rosalyne = rosalyne;
			setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}
		
		@Override
		public void start() {
			timer = 100;
			swingsLeft = 1 + rosalyne.random.nextInt(2);
			if (rosalyne.phase == PHASE_3) swingsLeft = 3 + rosalyne.random.nextInt(3);
			startCircling();
		}
		
		private void startCircling() {
			attackPhase = 0;
			LivingEntity target = rosalyne.getTarget();
			offset = new Vec3(rosalyne.getX() - target.getX(), 1, rosalyne.getZ() - target.getZ()).normalize().scale(4);
			rosalyne.moveControl.setWantedPosition(target.getX() + offset.x, target.getY() + offset.y, target.getZ() + offset.z, 4);
			rosalyne.setAnimation(ANIM_PREPARE_DASH);
		}
		
		@Override
		public void tick() {
			timer--;
			LivingEntity target = rosalyne.getTarget();
			//Approaching
			if (attackPhase == 0) {
				double tx = target.getX() + offset.x;
				double ty = target.getY() + offset.y;
				double tz = target.getZ() + offset.z;
				if (timer <= 0 || rosalyne.distanceToSqr(tx, ty, tz) < 1) {
					attackPhase = 1;
					//5° per tick, but also we need to not take too long or it's boring, so right now it's between 90° and 270°
					timer = 18 + rosalyne.random.nextInt(36);
				}
				rosalyne.moveControl.setWantedPosition(tx, ty, tz, 4);
			}
			//Circling
			else if (attackPhase == 1) {
				offset = offset.yRot(-5*Mth.DEG_TO_RAD);
				double tx = target.getX() + offset.x;
				double ty = target.getY() + offset.y;
				double tz = target.getZ() + offset.z;
				rosalyne.moveControl.setWantedPosition(tx, ty, tz, 4);
				if (timer <= 0) {
					attackPhase = 2;
					holdx = tx;
					holdy = ty;
					holdz = tz;
					timer = rosalyne.phase == PHASE_3 ? 15 : 20;
					rosalyne.setAnimation(ANIM_ARM_OUT_DN);
					rosalyne.playSound(SoundEvents.ARMOR_EQUIP_IRON, 1, 1);
				}
			}
			//Holding still before the swing
			else if (attackPhase == 2) {
				if (timer <= 0) {
					attackPhase = 3;
					timer = 20;
					//Overshoot the target by 4 blocks
					double tx = target.getX();
					double ty = target.getY();
					double tz = target.getZ();
					Vec3 tpos = new Vec3(tx - holdx, ty - holdy, tz - holdz).normalize();
					holdx = tx + 4*tpos.x;
					holdy = ty + 4*tpos.y;
					holdz = tz + 4*tpos.z;
				}
				rosalyne.moveControl.setWantedPosition(holdx, holdy, holdz, 4);
			}
			//Charging
			else if (attackPhase == 3) {
				if (timer <= 0 || rosalyne.distanceToSqr(target) < 2 || rosalyne.distanceToSqr(holdx, holdy, holdz) < 1) {
					//Got to the target, get ready to hold the pose
					rosalyne.swing();
					swingsLeft--;
					holdx = rosalyne.getX();
					holdy = target.getY();
					holdz = rosalyne.getZ();
					attackPhase = 4;
					timer = (swingsLeft > 0 && rosalyne.phase == PHASE_3) ? 10 : 20;
				}
				rosalyne.moveControl.setWantedPosition(holdx, holdy, holdz, 3);
			}
			//Holding still after the strike
			else if (attackPhase == 4) {
				if (timer <= 0 && swingsLeft > 0) startCircling();
				else rosalyne.moveControl.setWantedPosition(holdx, holdy, holdz, 4);
			}
		}
		
		@Override
		public void stop() {
			rosalyne.cooldownNextAttack();
			rosalyne.setAnimation(ANIM_NEUTRAL);
			rosalyne.rollNextAttack(1);
		}

		@Override
		public boolean canUse() {
			return rosalyne.nextAttack == 1 && (rosalyne.phase == PHASE_1 || rosalyne.phase == PHASE_2 || rosalyne.phase == PHASE_3) && rosalyne.getTarget() != null && rosalyne.getTarget().isAlive() && rosalyne.attackCooldown <= 0;
		}
		
		@Override
		public boolean canContinueToUse() {
			return canUse() && (swingsLeft > 0 || timer > 0);
		}
		
	}
	
	private static class VerticalCrashAttack extends Goal {
		//This is attack 2
		private RosalyneEntity rosalyne;
		private int timer, swingsLeft, attackPhase;
		private double holdx, holdy, holdz;
		private Vec3 offset;
		//attackPhase 0 = jumping, 1 = dash, 2 = hold pose
		
		public VerticalCrashAttack(RosalyneEntity rosalyne) {
			this.rosalyne = rosalyne;
			setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}
		
		@Override
		public void start() {
			swingsLeft = 1 + rosalyne.random.nextInt(2);
			if (rosalyne.phase == PHASE_3) swingsLeft = 3 + rosalyne.random.nextInt(3);
			startJump();
		}
		
		private void startJump() {
			timer = rosalyne.phase == PHASE_3 ? 25 : 40;
			attackPhase = 0;
			LivingEntity target = rosalyne.getTarget();
			offset = new Vec3(rosalyne.getX() - target.getX(), 0, rosalyne.getZ() - target.getZ()).normalize();
			holdx = target.getX() + offset.x*3;
			holdy = target.getY() + 4;
			holdz = target.getZ() + offset.z*3;
			rosalyne.moveControl.setWantedPosition(holdx, holdy, holdz, 1);
			rosalyne.setAnimation(ANIM_PREPARE_CRASH);
		}
		
		@Override
		public void tick() {
			timer--;
			LivingEntity target = rosalyne.getTarget();
			//Approaching
			if (attackPhase == 0) {
				//go up slightly as the pose is held for some movement
				holdy += 0.1;
				if (timer <= 0) {
					attackPhase = 1;
					timer = 20;
					//Overshoot the target slightly
					double tx = target.getX();
					double ty = target.getY();
					double tz = target.getZ();
					Vec3 tpos = new Vec3(tx - holdx, ty - holdy, tz - holdz).normalize();
					holdx = tx + 1*tpos.x;
					holdy = ty;
					holdz = tz + 1*tpos.z;
				}
				rosalyne.moveControl.setWantedPosition(holdx, holdy, holdz, 1);
			}
			//Charging
			else if (attackPhase == 1) {
				if (timer <= 0 || rosalyne.distanceToSqr(target) < 2 || rosalyne.distanceToSqr(holdx, holdy, holdz) < 1) {
					//Got to the target, get ready to hold the pose
					rosalyne.swing();
					swingsLeft--;
					holdx = rosalyne.getX();
					holdy = target.getY();
					holdz = rosalyne.getZ();
					attackPhase = 2;
					timer = (swingsLeft > 0 && rosalyne.phase == PHASE_3) ? 10 : 20;
				}
				rosalyne.moveControl.setWantedPosition(holdx, holdy, holdz, 3);
			}
			//Holding still after the strike
			else if (attackPhase == 2) {
				if (timer <= 0 && swingsLeft > 0) startJump();
				else rosalyne.moveControl.setWantedPosition(holdx, holdy, holdz, 4);
			}
		}
		
		@Override
		public void stop() {
			rosalyne.cooldownNextAttack();
			rosalyne.setAnimation(ANIM_NEUTRAL);
			rosalyne.rollNextAttack(2);
		}

		@Override
		public boolean canUse() {
			return rosalyne.nextAttack == 2 && (rosalyne.phase == PHASE_1 || rosalyne.phase == PHASE_2 || rosalyne.phase == PHASE_3) && rosalyne.getTarget() != null && rosalyne.getTarget().isAlive() && rosalyne.attackCooldown <= 0;
		}
		
		@Override
		public boolean canContinueToUse() {
			return canUse() && (swingsLeft > 0 || timer > 0);
		}
		
	}

}
