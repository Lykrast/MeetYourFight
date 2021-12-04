package lykrast.meetyourfight.entity;

import java.util.EnumSet;
import java.util.Random;

import javax.annotation.Nullable;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.ai.PhantomAttackPlayer;
import lykrast.meetyourfight.registry.ModEntities;
import lykrast.meetyourfight.registry.ModSounds;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class SwampjawEntity extends BossFlyingEntity {
	private int behavior;
	private static final int CIRCLE = 0, BOMB = 1, SWOOP = 2;
	//A lot of similarity with Phantoms
	private Vector3d orbitOffset = Vector3d.ZERO;
	private BlockPos orbitPosition = BlockPos.ZERO;
	
	//For rotating the tail
	public float tailYaw, tailPitch;

	public SwampjawEntity(EntityType<? extends SwampjawEntity> type, World worldIn) {
		super(type, worldIn);
		xpReward = 30;
		moveControl = new MoveHelperController(this);
		tailYaw = yRot;
		tailPitch = xRot;
	}

	public static AttributeModifierMap.MutableAttribute createAttributes() {
		return MobEntity.createMobAttributes().add(Attributes.MAX_HEALTH, 100).add(Attributes.ATTACK_DAMAGE, 7);
	}
	
	public static void spawn(PlayerEntity player, World world) {
		Random rand = player.getRandom();
		SwampjawEntity fish = ModEntities.SWAMPJAW.create(world);
		fish.moveTo(player.getX() + rand.nextInt(5) - 2, player.getY() + rand.nextInt(10) + 5, player.getZ() + rand.nextInt(5) - 2, rand.nextFloat() * 360 - 180, 0);
		//fish.attackCooldown = 100;
		if (!player.abilities.instabuild) fish.setTarget(player);
		fish.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, 100, 2));

		fish.finalizeSpawn((ServerWorld) world, world.getCurrentDifficultyAt(fish.blockPosition()), SpawnReason.EVENT, null, null);
		world.addFreshEntity(fish);
	}

	@Override
	public void tick() {
		noPhysics = true;
		super.tick();
		noPhysics = false;
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(1, new PickAttackGoal(this));
		goalSelector.addGoal(2, new SweepAttackGoal(this));
		goalSelector.addGoal(3, new BombMovementGoal(this));
		goalSelector.addGoal(4, new OrbitPointGoal(this));
		goalSelector.addGoal(8, new LookAtGoal(this, PlayerEntity.class, 16));
		targetSelector.addGoal(1, new PhantomAttackPlayer(this));
	}

	@Override
	public ILivingEntityData finalizeSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
		orbitPosition = this.blockPosition().above(5);
		return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
	}

	//Phantoms do that so uuuuuh... guess I'll do it too
	@Override
	public boolean canAttackType(EntityType<?> typeIn) {
		return true;
	}
	
	public float getTailYaw(float partialTick) {
		return MathHelper.approachDegrees(tailYaw, yRot, 6 * partialTick);
	}
	
	public float getTailPitch(float partialTick) {
		return MathHelper.approachDegrees(tailPitch, xRot, 2 * partialTick);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (level.isClientSide) {
			tailYaw = MathHelper.approachDegrees(tailYaw, yRot, 6);
			tailPitch = MathHelper.approachDegrees(tailPitch, xRot, 2);
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundNBT compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("AX")) orbitPosition = new BlockPos(compound.getInt("AX"), compound.getInt("AY"), compound.getInt("AZ"));
	}

	@Override
	public void addAdditionalSaveData(CompoundNBT compound) {
		super.addAdditionalSaveData(compound);
		compound.putInt("AX", orbitPosition.getX());
		compound.putInt("AY", orbitPosition.getY());
		compound.putInt("AZ", orbitPosition.getZ());
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.swampjawIdle;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return ModSounds.swampjawHurt;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.swampjawDeath;
	}

	@Override
	protected SoundEvent getMusic() {
		return ModSounds.musicMagnum;
	}

	@Override
	public CreatureAttribute getMobType() {
		return CreatureAttribute.UNDEAD;
	}
	
	@Override
	protected ResourceLocation getDefaultLootTable() {
		return MeetYourFight.rl("swampjaw");
	}
	
	//Same controller as Phantoms but ignores walls
	private static class MoveHelperController extends MovementController {
		private float speedFactor = 0.1F;
		private SwampjawEntity swampjaw;

		public MoveHelperController(SwampjawEntity entityIn) {
			super(entityIn);
			swampjaw = entityIn;
		}

		@Override
		public void tick() {
			float targetX = (float) (swampjaw.orbitOffset.x - swampjaw.getX());
			float targetY = (float) (swampjaw.orbitOffset.y - swampjaw.getY());
			float targetZ = (float) (swampjaw.orbitOffset.z - swampjaw.getZ());
			double horizontalDist = (double) MathHelper.sqrt(targetX * targetX + targetZ * targetZ);
			double verticalAdjust = 1.0D - (double) MathHelper.abs(targetY * 0.7F) / horizontalDist;
			targetX = (float) ((double) targetX * verticalAdjust);
			targetZ = (float) ((double) targetZ * verticalAdjust);
			horizontalDist = (double) MathHelper.sqrt(targetX * targetX + targetZ * targetZ);
			double totalDist = (double) MathHelper.sqrt(targetX * targetX + targetZ * targetZ + targetY * targetY);
			float prevYaw = swampjaw.yRot;
			float targetYaw = (float) MathHelper.atan2((double) targetZ, (double) targetX);
			float startYaw = MathHelper.wrapDegrees(swampjaw.yRot + 90.0F);
			targetYaw = MathHelper.wrapDegrees(targetYaw * (180F / (float) Math.PI));
			//Phantoms approach by 4�
			swampjaw.yRot = MathHelper.approachDegrees(startYaw, targetYaw, 10) - 90.0F;
			swampjaw.yBodyRot = swampjaw.yRot;
			if (MathHelper.degreesDifferenceAbs(prevYaw, swampjaw.yRot) < 3.0F) {
				float maxSpeed = swampjaw.behavior != CIRCLE ? 3F : 1.2F;
				float multiplier = speedFactor > maxSpeed ? 10 : maxSpeed / speedFactor;
				speedFactor = MathHelper.approach(speedFactor, maxSpeed, 0.005F * multiplier);
			}
			//else speedFactor = MathHelper.approach(this.speedFactor, 0.2F, 0.025F);
			else speedFactor = MathHelper.approach(speedFactor, swampjaw.behavior == BOMB ? 0.7F : 0.4F, 0.05F);

			float finalPitch = (float) (-(MathHelper.atan2(-targetY, horizontalDist) * (180F / (float) Math.PI)));
			swampjaw.xRot = finalPitch;
			float adjustedYaw = swampjaw.yRot + 90.0F;
			double finalX = (double) (speedFactor * MathHelper.cos(adjustedYaw * ((float) Math.PI / 180F))) * Math.abs((double) targetX / totalDist);
			double finalZ = (double) (speedFactor * MathHelper.sin(adjustedYaw * ((float) Math.PI / 180F))) * Math.abs((double) targetZ / totalDist);
			double finalY = (double) (speedFactor * MathHelper.sin(finalPitch * ((float) Math.PI / 180F))) * Math.abs((double) targetY / totalDist);
			Vector3d vector3d = swampjaw.getDeltaMovement();
			swampjaw.setDeltaMovement(vector3d.add((new Vector3d(finalX, finalY, finalZ)).subtract(vector3d).scale(0.2)));
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

			if (swampjaw.orbitOffset.y < swampjaw.getY() && !swampjaw.level.isEmptyBlock(swampjaw.blockPosition().below(1))) {
				height = Math.max(1, height);
				updateOffset();
			}

			if (swampjaw.orbitOffset.y > swampjaw.getY() && !swampjaw.level.isEmptyBlock(swampjaw.blockPosition().above(1))) {
				height = Math.min(-1, height);
				updateOffset();
			}
		}

		private void updateOffset() {
			if (BlockPos.ZERO.equals(swampjaw.orbitPosition)) swampjaw.orbitPosition = swampjaw.blockPosition();

			angle += direction * 20 * ((float) Math.PI / 180F);
			swampjaw.orbitOffset = Vector3d.atLowerCornerOf(swampjaw.orbitPosition).add(radius * MathHelper.cos(angle), -4.0F + height, radius * MathHelper.sin(this.angle));
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
		}

		private void updateOffset() {
			if (BlockPos.ZERO.equals(swampjaw.orbitPosition)) swampjaw.orbitPosition = swampjaw.blockPosition();
			LivingEntity target = swampjaw.getTarget();
			if (target != null) {
				double difX = target.getX() - swampjaw.orbitOffset.x;
				double difZ = target.getZ() - swampjaw.orbitOffset.z;
				Vector3d overshoot = new Vector3d(difX, 0, difZ).normalize();
				Vector3d vec = target.position();
				swampjaw.orbitOffset = new Vector3d(vec.x + overshoot.x * 7, swampjaw.orbitPosition.getY() - 4, vec.z + overshoot.z * 7);
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
			else if (!(livingentity instanceof PlayerEntity) || !((PlayerEntity) livingentity).isSpectator() && !((PlayerEntity) livingentity).isCreative()) return canUse();
			else return false;
		}

		@Override
		public void stop() {
			swampjaw.behavior = CIRCLE;
		}

		@Override
		public void tick() {
			LivingEntity livingentity = swampjaw.getTarget();
			swampjaw.orbitOffset = new Vector3d(livingentity.getX(), livingentity.getY(0.5D), livingentity.getZ());
			if (swampjaw.getBoundingBox().inflate(0.2).intersects(livingentity.getBoundingBox())) {
				swampjaw.doHurtTarget(livingentity);
				swampjaw.behavior = CIRCLE;
				//if (!swampjaw.isSilent()) swampjaw.world.playEvent(1039, swampjaw.getPosition(), 0);
			}
			else if (swampjaw.hurtTime > 0) swampjaw.behavior = CIRCLE;

		}
	}

	private static class PickAttackGoal extends Goal {
		private int tickDelay;
		private int bombLeft;
		private SwampjawEntity swampjaw;

		public PickAttackGoal(SwampjawEntity swampjaw) {
			this.swampjaw = swampjaw;
		}

		@Override
		public boolean canUse() {
			LivingEntity livingentity = swampjaw.getTarget();
			//Thanks debugger for letting me find a los check here
			return livingentity != null ? swampjaw.canAttack(swampjaw.getTarget(), PhantomAttackPlayer.DEFAULT_BUT_THROUGH_WALLS) : false;
		}

		@Override
		public void start() {
			tickDelay = 100;
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
			if (swampjaw.behavior == CIRCLE || swampjaw.behavior == BOMB) {
				--tickDelay;
				if (tickDelay <= 0) {
					//No bombs left, swoop in
					if (bombLeft <= 0) {
						bombLeft = 3;
						swampjaw.behavior = SWOOP;
						updateOrbit();
						tickDelay = (4 + swampjaw.random.nextInt(4)) * 20;
						swampjaw.playSound(ModSounds.swampjawCharge, 10.0F, 0.95F + swampjaw.random.nextFloat() * 0.1F);
					}
					//Switch to bomb mode
					else if (swampjaw.behavior == CIRCLE) {
						swampjaw.behavior = BOMB;
						tickDelay = 20;
					}
					//Bomb ready, wait for target near or for some extra time
					else if (tickDelay <= -120 || isTargetClose()) {
						bombLeft--;
						if (bombLeft <= 0) tickDelay = 30 + swampjaw.random.nextInt(30);
						else tickDelay = 20;
						updateOrbit();
						swampjaw.playSound(ModSounds.swampjawBomb, 10.0F, 0.95F + swampjaw.random.nextFloat() * 0.1F);
						SwampMineEntity tntentity = new SwampMineEntity(swampjaw.level, swampjaw.getX() + 0.5, swampjaw.getY(), swampjaw.getZ() + 0.5, swampjaw);
						//The ellpeck idea
						Vector3d motion = swampjaw.getDeltaMovement();
						tntentity.setDeltaMovement(tntentity.getDeltaMovement().add(motion.x * 0.5, 0, motion.z * 0.5));
						swampjaw.level.addFreshEntity(tntentity);
					}
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

}
