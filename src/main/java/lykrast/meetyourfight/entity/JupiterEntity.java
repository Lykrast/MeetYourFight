package lykrast.meetyourfight.entity;

import java.util.EnumSet;

import javax.annotation.Nullable;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.config.MYFConfigValues;
import lykrast.meetyourfight.entity.ai.MoveAroundTarget;
import lykrast.meetyourfight.entity.ai.VexMoveRandomGoal;
import lykrast.meetyourfight.entity.movement.VexMovementController;
import lykrast.meetyourfight.registry.MYFEntities;
import lykrast.meetyourfight.registry.MYFSounds;
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
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.ForgeEventFactory;

public class JupiterEntity extends BossEntity {
	public static final int HP = 120, DMG = 10;
	//I'll figure what to do for animations later
	private static final EntityDataAccessor<Byte> STATUS = SynchedEntityData.defineId(JupiterEntity.class, EntityDataSerializers.BYTE);
	//Animations
	public static final int ANIM_IDLE = 0, ANIM_CHARGE = 1, ANIM_AIM = 2, ANIM_THROW = 3;
	private int attackCooldown;
	public int clientAnim, prevAnim, animProg, animDur, projectileScale, prevProjectileScale;

	public JupiterEntity(EntityType<? extends JupiterEntity> type, Level worldIn) {
		super(type, worldIn);
		moveControl = new VexMovementController(this).slowdown(0.2);
		xpReward = 50;
		//Animations
		clientAnim = ANIM_IDLE;
		prevAnim = ANIM_IDLE;
		animProg = 1;
		animDur = 1;
		projectileScale = 0;
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new CircleAndBallAttack(this));
		goalSelector.addGoal(7, new MoveAroundTarget(this, 1));
		goalSelector.addGoal(8, new VexMoveRandomGoal(this, 0.25));
		goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
		goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
		targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
		targetSelector.addGoal(2, new HurtByTargetGoal(this));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, HP).add(Attributes.ARMOR, 15).add(Attributes.FOLLOW_RANGE, 64);
	}

	@Override
	public void move(MoverType typeIn, Vec3 pos) {
		super.move(typeIn, pos);
		checkInsideBlocks();
	}

	@SuppressWarnings("resource")
	@Override
	public void tick() {
		noPhysics = true;
		super.tick();
		noPhysics = false;
		setNoGravity(true);

		//Animations
		if (level().isClientSide) {
			int newanim = getAnimation();
			if (clientAnim != newanim) {
				prevAnim = clientAnim;
				clientAnim = newanim;
				animProg = 0;
				animDur = 10;
				if (clientAnim == ANIM_AIM) animDur = 7;
				else if (clientAnim == ANIM_CHARGE) animDur = 5;
				else if (clientAnim == ANIM_THROW) animDur = 3;

				if (clientAnim != ANIM_AIM) {
					prevProjectileScale = 0;
					projectileScale = 0;
				}
			}
			else if (animProg < animDur) animProg++;
			//projectile scale
			if (clientAnim == ANIM_CHARGE && animProg >= animDur) {
				prevProjectileScale = projectileScale;
				if (projectileScale < 5) projectileScale++;
			}
		}
	}

	public float getAnimProgress(float partial) {
		return Mth.clamp((animProg + partial) / animDur, 0, 1);
	}

	public float getProjectileScale(float partial) {
		if (projectileScale <= 0) return 0;
		else return Mth.clamp((prevProjectileScale + partial) / 5f, 0, 1);
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
		//model is 36px, eye at 32px
		return dimensions.height * 0.888F;
	}

	public static void spawn(Player player, Level world) {
		RandomSource rand = player.getRandom();
		JupiterEntity dame = MYFEntities.JUPITER.get().create(world);
		dame.moveTo(player.getX() + rand.nextInt(5) - 2, player.getY() + rand.nextInt(3) + 3, player.getZ() + rand.nextInt(5) - 2, rand.nextFloat() * 360 - 180, 0);
		dame.attackCooldown = 100;
		if (!player.getAbilities().instabuild) dame.setTarget(player);
		dame.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 2));

		ForgeEventFactory.onFinalizeSpawn(dame, (ServerLevel) world, world.getCurrentDifficultyAt(dame.blockPosition()), MobSpawnType.EVENT, null, null);
		world.addFreshEntity(dame);
	}

	//this one is fine to override
	@SuppressWarnings("deprecation")
	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
		//TODO get proper config
		getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("Config Health", MYFConfigValues.FORTUNA_HEALTH_MOD, AttributeModifier.Operation.ADDITION));
		setHealth(getMaxHealth());
		return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(STATUS, (byte) 0);
	}

	public int getAnimation() {
		return (entityData.get(STATUS));
	}

	public void setAnimation(int animation) {
		entityData.set(STATUS, (byte) animation);
	}

	@Override
	public void customServerAiStep() {
		if (attackCooldown > 0) attackCooldown--;
		super.customServerAiStep();
	}

	private void cooldownNextAttack() {
		attackCooldown = 50 + random.nextInt(21);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("AttackCooldown")) attackCooldown = compound.getInt("AttackCooldown");
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putInt("AttackCooldown", attackCooldown);
	}

	//TODO sounds

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.BLAZE_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.IRON_GOLEM_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.IRON_GOLEM_DEATH;
	}

	@Override
	protected SoundEvent getMusic() {
		return MYFSounds.musicSwampjaw.get();
	}

	@Override
	protected ResourceLocation getDefaultLootTable() {
		return MeetYourFight.rl("entities/jupiter");
	}

	//Copied from Evoker
	private void spawnFang(double posX, double posZ, double minY, double maxY, float rotationRad, int delay) {
		//TODO remake with lightning strikes
		BlockPos blockpos = BlockPos.containing(posX, maxY, posZ);
		double d0 = 0;

		do {
			BlockPos blockpos1 = blockpos.below();
			BlockState blockstate = level().getBlockState(blockpos1);
			if (blockstate.isFaceSturdy(level(), blockpos1, Direction.UP)) {
				if (!level().isEmptyBlock(blockpos)) {
					BlockState blockstate1 = level().getBlockState(blockpos);
					VoxelShape voxelshape = blockstate1.getCollisionShape(level(), blockpos);
					if (!voxelshape.isEmpty()) d0 = voxelshape.max(Direction.Axis.Y);
				}

				break;
			}

			blockpos = blockpos.below();
		} while (blockpos.getY() >= Mth.floor(minY) - 1);

		level().addFreshEntity(new EvokerFangs(level(), posX, blockpos.getY() + d0, posZ, rotationRad, delay, this));

	}

	private static class CircleAndBallAttack extends Goal {
		//Based on Rosalyne's CircleAndDashAttack
		private JupiterEntity jupiter;
		private int timer, swingsLeft, attackPhase;
		private boolean direction;
		private double holdx, holdy, holdz;
		private Vec3 offset;
		//attackPhase 0 = approaching, 1 = charge animation, 2 = circling, 3 = preparing to ball, 4 = hold pose

		public CircleAndBallAttack(JupiterEntity jupiter) {
			this.jupiter = jupiter;
			setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void start() {
			timer = 100;
			swingsLeft = 2;
			startCircling();
		}

		private void startCircling() {
			attackPhase = 0;
			LivingEntity target = jupiter.getTarget();
			offset = new Vec3(jupiter.getX() - target.getX(), 1, jupiter.getZ() - target.getZ()).normalize().scale(4);
			jupiter.moveControl.setWantedPosition(target.getX() + offset.x, target.getY() + offset.y, target.getZ() + offset.z, 4);
			//jupiter.setAnimation(ANIM_PREPARE_DASH);
		}

		@Override
		public void tick() {
			timer--;
			LivingEntity target = jupiter.getTarget();
			//didn't think this case could happen but apparently it can? (and it'd crashes)
			//in that case the attack should be interrupted in a few ticks anyway
			if (target == null) return;
			if (attackPhase < 4) jupiter.getLookControl().setLookAt(target.getX(), target.getEyeY(), target.getZ());
			//Approaching/Charge animation
			if (attackPhase == 0 || attackPhase == 1) {
				double tx = target.getX() + offset.x;
				double ty = target.getY() + offset.y;
				double tz = target.getZ() + offset.z;
				if (timer <= 0 || (attackPhase == 0 && jupiter.distanceToSqr(tx, ty, tz) < 2)) {
					if (attackPhase == 0) {
						//finished approaching, charge animation
						attackPhase = 1;
						timer = 10;
						jupiter.setAnimation(ANIM_CHARGE);
					}
					else {
						//finished charging, start circling
						attackPhase = 2;
						//5° per tick, but also we need to not take too long or it's boring, so right now it's between 90° and 270°
						timer = 18 + jupiter.random.nextInt(36);
						direction = jupiter.random.nextBoolean();
					}
				}
				jupiter.moveControl.setWantedPosition(tx, ty, tz, 4);
			}
			//Circling
			else if (attackPhase == 2) {
				offset = offset.yRot((direction ? 5 : -5) * Mth.DEG_TO_RAD);
				double tx = target.getX() + offset.x;
				double ty = target.getY() + offset.y;
				double tz = target.getZ() + offset.z;
				jupiter.moveControl.setWantedPosition(tx, ty, tz, 4);
				if (timer <= 0) {
					attackPhase = 3;
					holdx = tx;
					holdy = ty;
					holdz = tz;
					timer = 20;
					jupiter.setAnimation(ANIM_AIM);
				}
			}
			//Holding still before the attack
			else if (attackPhase == 3) {
				if (timer <= 0) {
					attackPhase = 4;
					timer = 10;
					swingsLeft--;
					//ball!
					jupiter.setAnimation(ANIM_THROW);
					double tx = target.getX();
					double ty = target.getY();
					double tz = target.getZ();
					sendLineAt(tx, ty, tz);
				}
				jupiter.moveControl.setWantedPosition(holdx, holdy, holdz, 4);
			}
			//Holding still after the ball
			else if (attackPhase == 4) {
				if (timer <= 0 && swingsLeft > 0) {
					startCircling();
					//jupiter.setAnimation(ANIM_IDLE);
				}
				else jupiter.moveControl.setWantedPosition(holdx, holdy, holdz, 4);
			}
		}

		private void sendLineAt(double tx, double ty, double tz) {
			double minY = Math.min(ty, jupiter.getY());
			double maxY = Math.max(ty, jupiter.getY()) + 1.0D;
			float angle = (float) Mth.atan2(tz - jupiter.getZ(), tx - jupiter.getX());
			for (int i = 0; i < 16; ++i) {
				double dist = 1.25 * (i + 1);
				jupiter.spawnFang(jupiter.getX() + Mth.cos(angle) * dist, jupiter.getZ() + Mth.sin(angle) * dist, minY, maxY, angle, i);
			}
		}

		@Override
		public void stop() {
			jupiter.cooldownNextAttack();
			jupiter.setAnimation(ANIM_IDLE);
			//jupiter.rollNextAttack(1);
		}

		@Override
		public boolean canUse() {
			return jupiter.getTarget() != null && jupiter.getTarget().isAlive() && jupiter.attackCooldown <= 0;
		}

		@Override
		public boolean canContinueToUse() {
			return canUse() && (swingsLeft > 0 || timer > 0);
		}

	}
}
