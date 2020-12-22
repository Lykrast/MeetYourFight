package lykrast.meetyourfight.entity;

import java.util.EnumSet;
import java.util.Random;

import javax.annotation.Nullable;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.ai.PhantomAttackPlayer;
import lykrast.meetyourfight.registry.ModEntities;
import lykrast.meetyourfight.registry.ModSounds;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntityPredicate;
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
import net.minecraft.util.SoundEvents;
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
		experienceValue = 30;
		moveController = new MoveHelperController(this);
		tailYaw = rotationYaw;
		tailPitch = rotationPitch;
	}

	public static AttributeModifierMap.MutableAttribute getAttributes() {
		return MobEntity.func_233666_p_().createMutableAttribute(Attributes.MAX_HEALTH, 100).createMutableAttribute(Attributes.ATTACK_DAMAGE, 7);
	}
	
	public static void spawn(PlayerEntity player, World world) {
		Random rand = player.getRNG();
		SwampjawEntity fish = ModEntities.SWAMPJAW.create(world);
		fish.setLocationAndAngles(player.getPosX() + rand.nextInt(5) - 2, player.getPosY() + rand.nextInt(10) + 5, player.getPosZ() + rand.nextInt(5) - 2, rand.nextFloat() * 360 - 180, 0);
		//fish.attackCooldown = 100;
		if (!player.abilities.isCreativeMode) fish.setAttackTarget(player);
		fish.addPotionEffect(new EffectInstance(Effects.RESISTANCE, 100, 2));

		fish.onInitialSpawn((ServerWorld) world, world.getDifficultyForLocation(fish.getPosition()), SpawnReason.EVENT, null, null);
		world.addEntity(fish);
	}

	@Override
	public void tick() {
		noClip = true;
		super.tick();
		noClip = false;
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
	public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
		orbitPosition = this.getPosition().up(5);
		return super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
	}
	
	public float getTailYaw(float partialTick) {
		return MathHelper.approachDegrees(tailYaw, rotationYaw, 6 * partialTick);
	}
	
	public float getTailPitch(float partialTick) {
		return MathHelper.approachDegrees(tailPitch, rotationPitch, 2 * partialTick);
	}

	@Override
	public void livingTick() {
		super.livingTick();
		if (world.isRemote) {
			tailYaw = MathHelper.approachDegrees(tailYaw, rotationYaw, 6);
			tailPitch = MathHelper.approachDegrees(tailPitch, rotationPitch, 2);
		}
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		if (compound.contains("AX")) orbitPosition = new BlockPos(compound.getInt("AX"), compound.getInt("AY"), compound.getInt("AZ"));
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.putInt("AX", orbitPosition.getX());
		compound.putInt("AY", orbitPosition.getY());
		compound.putInt("AZ", orbitPosition.getZ());
	}

	//TODO change sounds
	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_PHANTOM_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.ENTITY_PHANTOM_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_PHANTOM_DEATH;
	}

	@Override
	protected SoundEvent getMusic() {
		return ModSounds.musicSwampjaw;
	}

	@Override
	public CreatureAttribute getCreatureAttribute() {
		return CreatureAttribute.UNDEAD;
	}
	
	@Override
	protected ResourceLocation getLootTable() {
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
			float targetX = (float) (swampjaw.orbitOffset.x - swampjaw.getPosX());
			float targetY = (float) (swampjaw.orbitOffset.y - swampjaw.getPosY());
			float targetZ = (float) (swampjaw.orbitOffset.z - swampjaw.getPosZ());
			double horizontalDist = (double) MathHelper.sqrt(targetX * targetX + targetZ * targetZ);
			double d1 = 1.0D - (double) MathHelper.abs(targetY * 0.7F) / horizontalDist;
			targetX = (float) ((double) targetX * d1);
			targetZ = (float) ((double) targetZ * d1);
			horizontalDist = (double) MathHelper.sqrt(targetX * targetX + targetZ * targetZ);
			double d2 = (double) MathHelper.sqrt(targetX * targetX + targetZ * targetZ + targetY * targetY);
			float prevYaw = swampjaw.rotationYaw;
			float targetYaw = (float) MathHelper.atan2((double) targetZ, (double) targetX);
			float startYaw = MathHelper.wrapDegrees(swampjaw.rotationYaw + 90.0F);
			targetYaw = MathHelper.wrapDegrees(targetYaw * (180F / (float) Math.PI));
			//Phantoms approach by 4°
			swampjaw.rotationYaw = MathHelper.approachDegrees(startYaw, targetYaw, 10) - 90.0F;
			swampjaw.renderYawOffset = swampjaw.rotationYaw;
			if (MathHelper.degreesDifferenceAbs(prevYaw, swampjaw.rotationYaw) < 3.0F) {
				float maxSpeed = swampjaw.behavior == BOMB ? 3F : 1.2F;
				float multiplier = speedFactor > maxSpeed ? 10 : maxSpeed / speedFactor;
				speedFactor = MathHelper.approach(speedFactor, maxSpeed, 0.005F * multiplier);
			}
			//else speedFactor = MathHelper.approach(this.speedFactor, 0.2F, 0.025F);
			else speedFactor = MathHelper.approach(this.speedFactor, 0.3F, 0.05F);

			float finalPitch = (float) (-(MathHelper.atan2(-targetY, horizontalDist) * (180F / (float) Math.PI)));
			swampjaw.rotationPitch = finalPitch;
			float f8 = swampjaw.rotationYaw + 90.0F;
			double finalX = (double) (speedFactor * MathHelper.cos(f8 * ((float) Math.PI / 180F))) * Math.abs((double) targetX / d2);
			double finalZ = (double) (speedFactor * MathHelper.sin(f8 * ((float) Math.PI / 180F))) * Math.abs((double) targetZ / d2);
			double finalY = (double) (speedFactor * MathHelper.sin(finalPitch * ((float) Math.PI / 180F))) * Math.abs((double) targetY / d2);
			Vector3d vector3d = swampjaw.getMotion();
			swampjaw.setMotion(vector3d.add((new Vector3d(finalX, finalY, finalZ)).subtract(vector3d).scale(0.2)));
		}
	}

	private static abstract class BaseMoveGoal extends Goal {
		protected SwampjawEntity swampjaw;
		
		public BaseMoveGoal(SwampjawEntity swampjaw) {
			this.swampjaw = swampjaw;
			setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		protected boolean isCloseToOffset() {
			return swampjaw.orbitOffset.squareDistanceTo(swampjaw.getPosX(), swampjaw.getPosY(), swampjaw.getPosZ()) < 4.0D;
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
		public boolean shouldExecute() {
			return swampjaw.getAttackTarget() == null || swampjaw.behavior == CIRCLE;
		}

		@Override
		public void startExecuting() {
			radius = 6 + swampjaw.rand.nextFloat() * 6;
			height = -4.0F + swampjaw.rand.nextFloat() * 6.0F;
			direction = swampjaw.rand.nextBoolean() ? 1.0F : -1.0F;
			updateOffset();
		}
		
		@Override
		public void tick() {
			if (swampjaw.rand.nextInt(350) == 0) {
				height = -4.0F + swampjaw.rand.nextFloat() * 6.0F;
			}

			if (swampjaw.rand.nextInt(250) == 0) {
				--radius;
				if (radius < 6) {
					radius = 12;
					direction = -direction;
				}
			}

			if (swampjaw.rand.nextInt(450) == 0) {
				angle = swampjaw.rand.nextFloat() * 2.0F * (float) Math.PI;
				updateOffset();
			}

			if (isCloseToOffset()) {
				updateOffset();
			}

			if (swampjaw.orbitOffset.y < swampjaw.getPosY() && !swampjaw.world.isAirBlock(swampjaw.getPosition().down(1))) {
				height = Math.max(1, height);
				updateOffset();
			}

			if (swampjaw.orbitOffset.y > swampjaw.getPosY() && !swampjaw.world.isAirBlock(swampjaw.getPosition().up(1))) {
				height = Math.min(-1, height);
				updateOffset();
			}
		}

		private void updateOffset() {
			if (BlockPos.ZERO.equals(swampjaw.orbitPosition)) swampjaw.orbitPosition = swampjaw.getPosition();

			angle += direction * 20 * ((float) Math.PI / 180F);
			swampjaw.orbitOffset = Vector3d.copy(swampjaw.orbitPosition).add(radius * MathHelper.cos(angle), -4.0F + height, radius * MathHelper.sin(this.angle));
		}
	}
	
	private static class BombMovementGoal extends BaseMoveGoal {
		public BombMovementGoal(SwampjawEntity swampjaw) {
			super(swampjaw);
		}
		
		@Override
		public boolean shouldExecute() {
			return swampjaw.getAttackTarget() != null && swampjaw.behavior == BOMB;
		}

		@Override
		public void startExecuting() {
			updateOffset();
		}
		
		@Override
		public void tick() {
			if (isCloseToOffset()) updateOffset();
		}

		private void updateOffset() {
			if (BlockPos.ZERO.equals(swampjaw.orbitPosition)) swampjaw.orbitPosition = swampjaw.getPosition();
			LivingEntity target = swampjaw.getAttackTarget();
			if (target != null) {
				double difX = target.getPosX() - swampjaw.orbitOffset.x;
				double difZ = target.getPosZ() - swampjaw.orbitOffset.z;
				Vector3d overshoot = new Vector3d(difX, 0, difZ).normalize();
				swampjaw.orbitOffset = Vector3d.copy(swampjaw.orbitPosition).add(difX + overshoot.x * 2, -4, difZ + overshoot.z * 2);
			}
		}
	}

	private static class SweepAttackGoal extends BaseMoveGoal {
		public SweepAttackGoal(SwampjawEntity swampjaw) {
			super(swampjaw);
		}

		@Override
		public boolean shouldExecute() {
			return swampjaw.getAttackTarget() != null && swampjaw.behavior == SWOOP;
		}

		@Override
		public boolean shouldContinueExecuting() {
			LivingEntity livingentity = swampjaw.getAttackTarget();
			if (livingentity == null) return false;
			else if (!livingentity.isAlive()) return false;
			else if (!(livingentity instanceof PlayerEntity) || !((PlayerEntity) livingentity).isSpectator() && !((PlayerEntity) livingentity).isCreative()) return shouldExecute();
			else return false;
		}

		@Override
		public void resetTask() {
			swampjaw.behavior = CIRCLE;
		}

		@Override
		public void tick() {
			LivingEntity livingentity = swampjaw.getAttackTarget();
			swampjaw.orbitOffset = new Vector3d(livingentity.getPosX(), livingentity.getPosYHeight(0.5D), livingentity.getPosZ());
			if (swampjaw.getBoundingBox().grow(0.2).intersects(livingentity.getBoundingBox())) {
				swampjaw.attackEntityAsMob(livingentity);
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
		public boolean shouldExecute() {
			LivingEntity livingentity = swampjaw.getAttackTarget();
			return livingentity != null ? swampjaw.canAttack(swampjaw.getAttackTarget(), EntityPredicate.DEFAULT) : false;
		}

		@Override
		public void startExecuting() {
			tickDelay = 100;
			bombLeft = 3;
			swampjaw.behavior = CIRCLE;
			updateOrbit();
		}

		@Override
		public void resetTask() {
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
						tickDelay = (4 + swampjaw.rand.nextInt(4)) * 20;
						swampjaw.playSound(SoundEvents.ENTITY_PHANTOM_SWOOP, 10.0F, 0.95F + swampjaw.rand.nextFloat() * 0.1F);
					}
					//Switch to bomb mode
					else if (swampjaw.behavior == CIRCLE) {
						swampjaw.behavior = BOMB;
						tickDelay = 20;
					}
					//Bomb ready, wait for target near or for some extra time
					else if (tickDelay <= -40 || isTargetClose()) {
						bombLeft--;
						if (bombLeft <= 0) tickDelay = 30 + swampjaw.rand.nextInt(30);
						else tickDelay = 20;
						updateOrbit();
						swampjaw.playSound(SoundEvents.ENTITY_TNT_PRIMED, 10.0F, 0.95F + swampjaw.rand.nextFloat() * 0.1F);
						SwampMineEntity tntentity = new SwampMineEntity(swampjaw.world, swampjaw.getPosX() + 0.5, swampjaw.getPosY(), swampjaw.getPosZ() + 0.5, swampjaw);
						swampjaw.world.addEntity(tntentity);
					}
				}
			}

		}
		
		private boolean isTargetClose() {
			LivingEntity target = swampjaw.getAttackTarget();
			if (target == null) return false;
			double dx = target.getPosX() - (swampjaw.getPosX() + swampjaw.getMotion().x);
			double dz = target.getPosZ() - (swampjaw.getPosZ() + swampjaw.getMotion().z);
			return (dx * dx + dz * dz) < 9;
		}

		private void updateOrbit() {
			swampjaw.orbitPosition = swampjaw.getAttackTarget().getPosition().up(14 + swampjaw.rand.nextInt(6));
		}
	}

}
