package lykrast.meetyourfight.entity;

import java.util.EnumSet;

import javax.annotation.Nullable;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.config.MYFConfigValues;
import lykrast.meetyourfight.entity.ai.PhantomAttackPlayer;
import lykrast.meetyourfight.registry.MYFEntities;
import lykrast.meetyourfight.registry.MYFSounds;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

public class SwampjawEntity extends BossFlyingEntity {
	private static final EntityDataAccessor<Byte> ANIMATION = SynchedEntityData.defineId(SwampjawEntity.class, EntityDataSerializers.BYTE);
	public static final int ANIM_NEUTRAL = 0, ANIM_SWOOP = 1, ANIM_STUN = 2, ANIM_SWIPE = 3;
	public static final int HP = 100, DMG_CHARGE = 12;
	private int behavior;
	private int attackDelay;
	private static final int CIRCLE = 0, BOMB = 1, SWOOP = 2, STUNNED = 3, SWIPING = 4;
	//A lot of similarity with Phantoms
	private Vec3 orbitOffset = Vec3.ZERO;
	private BlockPos orbitPosition = BlockPos.ZERO;
	
	//For rotating the tail
	public float tailYaw, tailPitch;
	public int clientAnim, prevAnim, animProg, animDur;

	public SwampjawEntity(EntityType<? extends SwampjawEntity> type, Level worldIn) {
		super(type, worldIn);
		xpReward = 30;
		moveControl = new MoveHelperController(this);
		tailYaw = getYRot();
		tailPitch = getXRot();
		clientAnim = ANIM_NEUTRAL;
		prevAnim = ANIM_NEUTRAL;
		animProg = 1;
		animDur = 1;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, HP).add(Attributes.ATTACK_DAMAGE, DMG_CHARGE).add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
	}
	
	public static void spawn(Player player, Level world) {
		RandomSource rand = player.getRandom();
		SwampjawEntity fish = MYFEntities.SWAMPJAW.get().create(world);
		fish.moveTo(player.getX() + rand.nextInt(5) - 2, player.getY() + rand.nextInt(10) + 5, player.getZ() + rand.nextInt(5) - 2, rand.nextFloat() * 360 - 180, 0);
		//fish.attackCooldown = 100;
		if (!player.getAbilities().instabuild) fish.setTarget(player);
		fish.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 2));

		fish.finalizeSpawn((ServerLevel) world, world.getCurrentDifficultyAt(fish.blockPosition()), MobSpawnType.EVENT, null, null);
		world.addFreshEntity(fish);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(1, new PickAttackGoal(this));
		goalSelector.addGoal(2, new SweepAttackGoal(this));
		goalSelector.addGoal(3, new BombMovementGoal(this));
		goalSelector.addGoal(4, new OrbitPointGoal(this));
		goalSelector.addGoal(8, new LookWhenNotStunned(this, Player.class, 16));
		targetSelector.addGoal(1, new PhantomAttackPlayer(this));
	}

	@Override
	public void tick() {
		noPhysics = true;
		super.tick();
		noPhysics = false;
		
		if (level().isClientSide()) {
			int newanim = getAnimation();
			if (clientAnim != newanim) {
				prevAnim = clientAnim;
				clientAnim = newanim;
				animProg = 0;
				animDur = 10;
				if (clientAnim == ANIM_NEUTRAL && prevAnim == ANIM_SWOOP) animDur = 5;
			}
			else if (animProg < animDur) animProg++;
		}
	}

	//this one is fine to override
	@SuppressWarnings("deprecation")
	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
		orbitPosition = this.blockPosition().above(5);
		getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("Config Health", MYFConfigValues.SWAMPJAW_HEALTH_MOD, AttributeModifier.Operation.ADDITION));
		setHealth(getMaxHealth());
		getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier("Config Damage", MYFConfigValues.SWAMPJAW_DMG_MOD, AttributeModifier.Operation.ADDITION));
		return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
	}

	//Phantoms do that so uuuuuh... guess I'll do it too
	@Override
	public boolean canAttackType(EntityType<?> typeIn) {
		return true;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(ANIMATION, (byte)0);
	}
	
	public int getAnimation() {
		return entityData.get(ANIMATION);
	}
	
	public void setAnimation(int anim) {
		entityData.set(ANIMATION, (byte)anim);
	}
	
	public float getAnimProgress(float partial) {
		return Mth.clamp((animProg + partial) / animDur, 0, 1);
	}
	
	public float getTailYaw(float partialTick) {
		return Mth.approachDegrees(tailYaw, getYRot(), 6 * partialTick);
	}
	
	public float getTailPitch(float partialTick) {
		return Mth.approachDegrees(tailPitch, getXRot(), 2 * partialTick);
	}

	@SuppressWarnings("resource")
	@Override
	public void aiStep() {
		super.aiStep();
		if (level().isClientSide) {
			tailYaw = Mth.approachDegrees(tailYaw, getYRot(), 6);
			tailPitch = Mth.approachDegrees(tailPitch, getXRot(), 2);
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("AX")) orbitPosition = new BlockPos(compound.getInt("AX"), compound.getInt("AY"), compound.getInt("AZ"));
		if (compound.contains("Delay")) attackDelay = compound.getInt("Delay");
		if (compound.contains("Behavior")) behavior = compound.getInt("Behavior");
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putInt("AX", orbitPosition.getX());
		compound.putInt("AY", orbitPosition.getY());
		compound.putInt("AZ", orbitPosition.getZ());
		compound.putInt("Delay", attackDelay);
		compound.putInt("Behavior", behavior);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return MYFSounds.swampjawIdle.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return MYFSounds.swampjawHurt.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MYFSounds.swampjawDeath.get();
	}

	@Override
	protected SoundEvent getMusic() {
		return MYFSounds.musicSwampjaw.get();
	}

	@Override
	public MobType getMobType() {
		return MobType.UNDEAD;
	}
	
	@Override
	protected ResourceLocation getDefaultLootTable() {
		return MeetYourFight.rl("entities/swampjaw");
	}
	
	private void swipeAttack() {
		playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 10.0F, 0.95F + random.nextFloat() * 0.1F);
        for(LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(1.75, 1, 1.75))) {
        	if (target.isAlive() && !target.isInvulnerable() && target != this) {
        		if (doHurtTarget(target)) {
					double mult = Math.max(0, 1 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
					Vec3 knockback = new Vec3(target.getX() - getX(), 0, target.getZ() - getZ()).normalize().add(0, 0.2, 0).scale(2*mult);
					target.setDeltaMovement(target.getDeltaMovement().add(knockback));
        		}
        	}
        }
	}
	
	//Same controller as Phantoms but ignores walls
	private static class MoveHelperController extends MoveControl {
		private float speedFactor = 0.1F;
		private SwampjawEntity swampjaw;

		public MoveHelperController(SwampjawEntity entityIn) {
			super(entityIn);
			swampjaw = entityIn;
		}

		@Override
		public void tick() {
			if (swampjaw.behavior == STUNNED || swampjaw.behavior == SWIPING) {
				swampjaw.setDeltaMovement(swampjaw.getDeltaMovement().scale(0.9));
				return;
			}
			
			float targetX = (float) (swampjaw.orbitOffset.x - swampjaw.getX());
			float targetY = (float) (swampjaw.orbitOffset.y - swampjaw.getY());
			float targetZ = (float) (swampjaw.orbitOffset.z - swampjaw.getZ());
			double horizontalDist = (double) Mth.sqrt(targetX * targetX + targetZ * targetZ);
			double verticalAdjust = 1.0D - (double) Mth.abs(targetY * 0.7F) / horizontalDist;
			targetX = (float) ((double) targetX * verticalAdjust);
			targetZ = (float) ((double) targetZ * verticalAdjust);
			horizontalDist = (double) Mth.sqrt(targetX * targetX + targetZ * targetZ);
			double totalDist = (double) Mth.sqrt(targetX * targetX + targetZ * targetZ + targetY * targetY);
			float prevYaw = swampjaw.getYRot();
			float targetYaw = (float) Mth.atan2((double) targetZ, (double) targetX);
			float startYaw = Mth.wrapDegrees(swampjaw.getYRot() + 90.0F);
			targetYaw = Mth.wrapDegrees(targetYaw * (180F / (float) Math.PI));
			boolean isFastBomb = swampjaw.behavior == BOMB && swampjaw.attackDelay <= 10;
			//Phantoms approach by 4�
			swampjaw.setYRot(Mth.approachDegrees(startYaw, targetYaw, isFastBomb ? 20 : 10) - 90.0F);
			swampjaw.yBodyRot = swampjaw.getYRot();
			if (isFastBomb || Mth.degreesDifferenceAbs(prevYaw, swampjaw.getYRot()) < 3.0F) {
				float maxSpeed = swampjaw.behavior != CIRCLE ? 3F : 1.2F;
				float multiplier = speedFactor > maxSpeed ? 10 : maxSpeed / speedFactor;
				speedFactor = Mth.approach(speedFactor, maxSpeed, 0.005F * multiplier);
			}
			//else speedFactor = MathHelper.approach(this.speedFactor, 0.2F, 0.025F);
			else speedFactor = Mth.approach(speedFactor, swampjaw.behavior == BOMB ? 0.7F : 0.4F, 0.05F);

			float finalPitch = (float) (-(Mth.atan2(-targetY, horizontalDist) * (180F / (float) Math.PI)));
			swampjaw.setXRot(finalPitch);
			float adjustedYaw = swampjaw.getYRot() + 90.0F;
			double finalX = (double) (speedFactor * Mth.cos(adjustedYaw * ((float) Math.PI / 180F))) * Math.abs((double) targetX / totalDist);
			double finalZ = (double) (speedFactor * Mth.sin(adjustedYaw * ((float) Math.PI / 180F))) * Math.abs((double) targetZ / totalDist);
			double finalY = (double) (speedFactor * Mth.sin(finalPitch * ((float) Math.PI / 180F))) * Math.abs((double) targetY / totalDist);
			Vec3 vector3d = swampjaw.getDeltaMovement();
			swampjaw.setDeltaMovement(vector3d.add((new Vec3(finalX, finalY, finalZ)).subtract(vector3d).scale(0.2)));
		}
	}

	private static abstract class BaseMoveGoal extends Goal {
		protected SwampjawEntity swampjaw;
		
		public BaseMoveGoal(SwampjawEntity swampjaw) {
			this.swampjaw = swampjaw;
			setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		protected boolean isCloseToOffset() {
			return swampjaw.orbitOffset.distanceToSqr(swampjaw.getX(), swampjaw.getY(), swampjaw.getZ()) < 4;
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}
	}

	private static class OrbitPointGoal extends BaseMoveGoal {
		private float angle;
		private float radius;
		private float height;
		private float direction;

		public OrbitPointGoal(SwampjawEntity swampjaw) {
			super(swampjaw);
		}
		
		@Override
		public boolean canUse() {
			return swampjaw.getTarget() == null || swampjaw.behavior == CIRCLE;
		}

		@Override
		public void start() {
			radius = 6 + swampjaw.random.nextFloat() * 6;
			height = -4.0F + swampjaw.random.nextFloat() * 6.0F;
			direction = swampjaw.random.nextBoolean() ? 1.0F : -1.0F;
			updateOffset();
		}
		
		@Override
		public void tick() {
			if (swampjaw.random.nextInt(350) == 0) {
				height = -4.0F + swampjaw.random.nextFloat() * 6.0F;
			}

			if (swampjaw.random.nextInt(250) == 0) {
				--radius;
				if (radius < 6) {
					radius = 12;
					direction = -direction;
				}
			}

			if (swampjaw.random.nextInt(450) == 0) {
				angle = swampjaw.random.nextFloat() * 2.0F * (float) Math.PI;
				updateOffset();
			}

			if (isCloseToOffset()) {
				updateOffset();
			}

			if (swampjaw.orbitOffset.y < swampjaw.getY() && !swampjaw.level().isEmptyBlock(swampjaw.blockPosition().below(1))) {
				height = Math.max(1, height);
				updateOffset();
			}

			if (swampjaw.orbitOffset.y > swampjaw.getY() && !swampjaw.level().isEmptyBlock(swampjaw.blockPosition().above(1))) {
				height = Math.min(-1, height);
				updateOffset();
			}
		}

		private void updateOffset() {
			if (BlockPos.ZERO.equals(swampjaw.orbitPosition)) swampjaw.orbitPosition = swampjaw.blockPosition();

			angle += direction * 20 * ((float) Math.PI / 180F);
			swampjaw.orbitOffset = Vec3.atLowerCornerOf(swampjaw.orbitPosition).add(radius * Mth.cos(angle), -4.0F + height, radius * Mth.sin(this.angle));
		}
	}
	
	private static class BombMovementGoal extends BaseMoveGoal {
		public BombMovementGoal(SwampjawEntity swampjaw) {
			super(swampjaw);
		}
		
		@Override
		public boolean canUse() {
			return swampjaw.getTarget() != null && swampjaw.behavior == BOMB;
		}

		@Override
		public void start() {
			updateOffset();
		}
		
		@Override
		public void tick() {
			if (isCloseToOffset()) updateOffset();
			else if (swampjaw.attackDelay <= 10) {
				//distance² between the travel line (swampjaw to orbit offset) and target
				//to catch a player sidestepping
				//bomb drop checks for a squared distance of 12
				Vec3 swamp = swampjaw.position();
				Vec3 destination = swampjaw.orbitOffset;
				Vec3 target = swampjaw.getTarget().position();
				//got formula from wikipedia https://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line
				double distance = (destination.z - swamp.z)*target.x - (destination.x - swamp.x)*target.z + destination.x*swamp.z - destination.z*swamp.x;
				distance = (distance*distance) / ((destination.z-swamp.z)*(destination.z-swamp.z) + (destination.x-swamp.x)*(destination.x-swamp.x));
				if (distance > 10) updateOffset();
			}
		}

		private void updateOffset() {
			if (BlockPos.ZERO.equals(swampjaw.orbitPosition)) swampjaw.orbitPosition = swampjaw.blockPosition();
			LivingEntity target = swampjaw.getTarget();
			if (target != null) {
				double difX = target.getX() - swampjaw.orbitOffset.x;
				double difZ = target.getZ() - swampjaw.orbitOffset.z;
				Vec3 overshoot = new Vec3(difX, 0, difZ).normalize();
				Vec3 vec = target.position();
				swampjaw.orbitOffset = new Vec3(vec.x + overshoot.x * 10, swampjaw.orbitPosition.getY() - 4, vec.z + overshoot.z * 10);
			}
		}
	}

	private static class SweepAttackGoal extends BaseMoveGoal {
		public SweepAttackGoal(SwampjawEntity swampjaw) {
			super(swampjaw);
		}

		@Override
		public boolean canUse() {
			return swampjaw.getTarget() != null && swampjaw.behavior == SWOOP;
		}

		@Override
		public boolean canContinueToUse() {
			LivingEntity livingentity = swampjaw.getTarget();
			if (livingentity == null) return false;
			else if (!livingentity.isAlive()) return false;
			else if (!(livingentity instanceof Player) || !((Player) livingentity).isSpectator() && !((Player) livingentity).isCreative()) return canUse();
			else return false;
		}

		@Override
		public void stop() {
			if (swampjaw.behavior == SWOOP) {
				swampjaw.behavior = CIRCLE;
				swampjaw.setAnimation(ANIM_NEUTRAL);
			}
		}

		@Override
		public void tick() {
			LivingEntity livingentity = swampjaw.getTarget();
			swampjaw.orbitOffset = new Vec3(livingentity.getX(), livingentity.getY(0.5D), livingentity.getZ());
			if (swampjaw.getBoundingBox().intersects(livingentity.getBoundingBox())) {
				swampjaw.doHurtTarget(livingentity);
				swampjaw.behavior = CIRCLE;
				swampjaw.setAnimation(ANIM_NEUTRAL);
				//if (!swampjaw.isSilent()) swampjaw.world.playEvent(1039, swampjaw.getPosition(), 0);
			}
			else if (swampjaw.hurtTime > 0) {
				swampjaw.playSound(MYFSounds.swampjawStun.get(), 10.0F, 0.95F + swampjaw.random.nextFloat() * 0.1F);
				swampjaw.attackDelay = 50;
				swampjaw.behavior = STUNNED;
				swampjaw.setAnimation(ANIM_STUN);
			}

		}
	}

	private static class PickAttackGoal extends Goal {
		private int bombLeft;
		private SwampjawEntity swampjaw;

		public PickAttackGoal(SwampjawEntity swampjaw) {
			this.swampjaw = swampjaw;
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public boolean canUse() {
			LivingEntity livingentity = swampjaw.getTarget();
			//Thanks debugger for letting me find a los check here
			return swampjaw.behavior == STUNNED || swampjaw.behavior == SWIPING || (livingentity != null ? swampjaw.canAttack(swampjaw.getTarget(), PhantomAttackPlayer.DEFAULT_BUT_THROUGH_WALLS) : false);
		}

		@Override
		public void start() {
			swampjaw.attackDelay = 100;
			bombLeft = 3;
			swampjaw.behavior = CIRCLE;
			updateOrbit();
		}

		@Override
		public void stop() {
			//swampjaw.orbitPosition = swampjaw.world.getHeight(Heightmap.Type.MOTION_BLOCKING, swampjaw.orbitPosition).up(10 + swampjaw.rand.nextInt(20));
		}

		@Override
		public void tick() {
			if (swampjaw.behavior == CIRCLE || swampjaw.behavior == BOMB || swampjaw.behavior == STUNNED || swampjaw.behavior == SWIPING) {
				--swampjaw.attackDelay;
				if (swampjaw.attackDelay <= 0) {
					//Recover from stun
					if (swampjaw.behavior == STUNNED) {
						swampjaw.behavior = SWIPING;
						swampjaw.setAnimation(ANIM_SWIPE);
						swampjaw.attackDelay = 10;
					}
					//Recover from stun
					else if (swampjaw.behavior == SWIPING) {
						swampjaw.behavior = CIRCLE;
						swampjaw.setAnimation(ANIM_NEUTRAL);
						swampjaw.attackDelay = (2 + swampjaw.random.nextInt(3)) * 20;
					}
					//No bombs left, swoop in
					else if (bombLeft <= 0) {
						bombLeft = 3;
						swampjaw.behavior = SWOOP;
						swampjaw.setAnimation(ANIM_SWOOP);
						updateOrbit();
						swampjaw.attackDelay = (4 + swampjaw.random.nextInt(4)) * 20;
						swampjaw.playSound(MYFSounds.swampjawCharge.get(), 10.0F, 0.95F + swampjaw.random.nextFloat() * 0.1F);
					}
					//Switch to bomb mode
					else if (swampjaw.behavior == CIRCLE) {
						swampjaw.behavior = BOMB;
						swampjaw.attackDelay = 20;
					}
					//Bomb ready, wait for target near or for some extra time
					else if (swampjaw.attackDelay <= -120 || isTargetClose()) {
						bombLeft--;
						if (bombLeft <= 0) swampjaw.attackDelay = 30 + swampjaw.random.nextInt(30);
						else swampjaw.attackDelay = 30;
						updateOrbit();
						swampjaw.playSound(MYFSounds.swampjawBomb.get(), 10.0F, 0.95F + swampjaw.random.nextFloat() * 0.1F);
						SwampMineEntity tntentity = new SwampMineEntity(swampjaw.level(), swampjaw.getX() + 0.5, swampjaw.getY(), swampjaw.getZ() + 0.5, swampjaw);
						//The ellpeck idea
						Vec3 motion = swampjaw.getDeltaMovement();
						tntentity.setDeltaMovement(tntentity.getDeltaMovement().add(motion.x * 0.5, 0, motion.z * 0.5));
						swampjaw.level().addFreshEntity(tntentity);
					}
				}
				//animation change
				else if (swampjaw.behavior == STUNNED && swampjaw.attackDelay == 10) {
					swampjaw.setAnimation(ANIM_NEUTRAL);
				}
				//perform the swipe
				else if (swampjaw.behavior == SWIPING && swampjaw.attackDelay == 5) {
					swampjaw.swipeAttack();
				}
			}
		}
		
		private boolean isTargetClose() {
			LivingEntity target = swampjaw.getTarget();
			if (target == null) return false;
			double dx = target.getX() - (swampjaw.getX() + swampjaw.getDeltaMovement().x);
			double dz = target.getZ() - (swampjaw.getZ() + swampjaw.getDeltaMovement().z);
			return (dx * dx + dz * dz) < 12;
		}

		private void updateOrbit() {
			swampjaw.orbitPosition = swampjaw.getTarget().blockPosition().above(14 + swampjaw.random.nextInt(6));
		}
	}
	
	private static class LookWhenNotStunned extends LookAtPlayerGoal {
		private SwampjawEntity swampjaw;

		public LookWhenNotStunned(SwampjawEntity swampjaw, Class<? extends LivingEntity> target, float range) {
			super(swampjaw, target, range);
			this.swampjaw = swampjaw;
		}
		
		@Override
		public boolean canUse() {
			return swampjaw.behavior != STUNNED && swampjaw.behavior != SWIPING && super.canUse();
		}
		
		@Override
		public boolean canContinueToUse() {
			return swampjaw.behavior != STUNNED && swampjaw.behavior != SWIPING && super.canContinueToUse();
		}
		
	}

}
