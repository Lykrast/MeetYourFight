package lykrast.meetyourfight.entity;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.ai.MoveAroundTarget;
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
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DameFortunaEntity extends BossEntity {
	/**
	 * Look at me I can embed attacks AND rage in a single byte! 0x0000RRAA with R for rage (0-2) and A for attack (0-2)
	 */
	private static final EntityDataAccessor<Byte> STATUS = SynchedEntityData.defineId(DameFortunaEntity.class, EntityDataSerializers.BYTE);
	public static final int NO_ATTACK = 0, PROJ_ATTACK = 1, CLAW_ATTACK = 2;
	private static final int ATTACK_MASK = 0b11, RAGE_MASK = ~ATTACK_MASK;
	public int attackCooldown;
	/**
	 * Since the animation only rotates in 90�, it's given in 90� by 0 1 2 or 3
	 */
	public int headTargetPitch, headTargetYaw, headTargetRoll;
	public int headRotationTimer;
	public float headRotationProgress, headRotationProgressLast;
	/**
	 * Server side cached rage
	 */
	private int rage;
	
	public DameFortunaEntity(EntityType<? extends DameFortunaEntity> type, Level worldIn) {
		super(type, worldIn);
		moveControl = new VexMovementController(this);
		xpReward = 100;
		rage = 0;
		//Animations
		headRotationTimer = 30;
		headTargetPitch = 0;
		headTargetYaw = 0;
		headTargetRoll = 0;
		headRotationProgress = 1;
		headRotationProgressLast = 1;
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(2, new RegularAttack(this));
		goalSelector.addGoal(3, new RageEvokerLines(this));
		goalSelector.addGoal(7, new MoveAroundTarget(this, 1));
		goalSelector.addGoal(8, new VexMoveRandomGoal(this));
		goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
		goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
		targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
		targetSelector.addGoal(2, new HurtByTargetGoal(this));
	}
	
	public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 300).add(Attributes.ARMOR, 5).add(Attributes.FOLLOW_RANGE, 64);
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
		
		//Animations
		if (level.isClientSide) {
			headRotationTimer--;
			if (headRotationTimer <= 0) {
				//So uh I do not intend to have rage beyond 3 levels, so the ad hoc will do
				switch (getRage()) {
					case 0:
						headRotationTimer = 20 + random.nextInt(21);
						break;
					case 1:
						headRotationTimer = 15 + random.nextInt(11);
						break;
					default:
						headRotationTimer = 5 + random.nextInt(11);
				}
				rotateHead();
				headRotationProgress = 0;
				headRotationProgressLast = 0;
			}
			else {
				headRotationProgressLast = headRotationProgress;
				headRotationProgress = Math.min(1, headRotationProgress + 0.07f);
			}
		}
	}
	
	public float getHeadRotationProgress(float partialTicks) {
	      return Mth.lerp(partialTicks, headRotationProgressLast, headRotationProgress);
	}
	
	private void rotateHead() {
		boolean reverse = random.nextBoolean();
		int axis = random.nextInt(3);
		switch (axis) {
			case 0:
				if (reverse) {
					if (headTargetPitch <= 0) headTargetPitch = 3;
					else headTargetPitch--;
				}
				else headTargetPitch = (headTargetPitch + 1) % 4;
				break;
			case 1:
				if (reverse) {
					if (headTargetYaw <= 0) headTargetYaw = 3;
					else headTargetYaw--;
				}
				else headTargetYaw = (headTargetYaw + 1) % 4;
				break;
			case 2:
				if (reverse) {
					if (headTargetRoll <= 0) headTargetRoll = 3;
					else headTargetRoll--;
				}
				else headTargetRoll = (headTargetRoll + 1) % 4;
				break;
		}
	}
	
	public static void spawn(Player player, Level world) {
		RandomSource rand = player.getRandom();
		DameFortunaEntity dame = ModEntities.DAME_FORTUNA.get().create(world);
		dame.moveTo(player.getX() + rand.nextInt(5) - 2, player.getY() + rand.nextInt(3) + 3, player.getZ() + rand.nextInt(5) - 2, rand.nextFloat() * 360 - 180, 0);
		dame.attackCooldown = 100;
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
		if (attackCooldown > 0) attackCooldown--;
		int newrage = getRageTarget();
		if (newrage > rage) {
			rage = newrage;
			setRage(rage);
		}
		super.customServerAiStep();
	}
	
	private ProjectileLineEntity readyLine() {
		ProjectileLineEntity proj = new ProjectileLineEntity(level, this, 0, 0, 0);
		proj.setOwner(this);
		proj.setPos(getX(), getEyeY() + 1, getZ());
		proj.setVariant(ProjectileLineEntity.VAR_DAME_FORTUNA);
		return proj;
	}
	
	//Copied from Evoker
	private void spawnFangs(double posX, double posZ, double minY, double minZ, float rotationRad, int delay) {
		BlockPos blockpos = new BlockPos(posX, minZ, posZ);
		boolean success = false;
		double d0 = 0;

		do {
			BlockPos blockpos1 = blockpos.below();
			BlockState blockstate = level.getBlockState(blockpos1);
			if (blockstate.isFaceSturdy(level, blockpos1, Direction.UP)) {
				if (!level.isEmptyBlock(blockpos)) {
					BlockState blockstate1 = level.getBlockState(blockpos);
					VoxelShape voxelshape = blockstate1.getCollisionShape(level, blockpos);
					if (!voxelshape.isEmpty()) d0 = voxelshape.max(Direction.Axis.Y);
				}

				success = true;
				break;
			}

			blockpos = blockpos.below();
		}
		while (blockpos.getY() >= Mth.floor(minY) - 1);

		if (success) level.addFreshEntity(new EvokerFangs(level, posX, blockpos.getY() + d0, posZ, rotationRad, delay, this));

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
	
	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.dameFortunaIdle.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return ModSounds.dameFortunaHurt.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.dameFortunaDeath.get();
	}

	@Override
	protected SoundEvent getMusic() {
		return ModSounds.musicMagnum.get();
	}
	
	@Override
	protected ResourceLocation getDefaultLootTable() {
		return MeetYourFight.rl("dame_fortuna");
	}
	
	//The regular attacks
	//It's horribly ad hoc but it'll do "for now"
	private static class RegularAttack extends StationaryAttack {
		private DameFortunaEntity dame;
		private LivingEntity target;
		private int attackRemaining, attackDelay, chosenAttack;

		public RegularAttack(DameFortunaEntity dame) {
			super(dame);
			this.dame = dame;
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public boolean canUse() {
			return dame.attackCooldown <= 0 && dame.getTarget() != null && dame.getTarget().isAlive();
		}

		@Override
		public void start() {
			super.start();
			dame.attackCooldown = 2;
			target = dame.getTarget();
			chosenAttack = dame.random.nextInt(3);
			//Choose animation depending on the attack
			//Horrible ad hoc n�1
			dame.setAttack(chosenAttack == 1 ? CLAW_ATTACK : PROJ_ATTACK);
			attackDelay = 30;
			attackRemaining = getAttackCount();
			dame.playSound(ModSounds.dameFortunaAttack.get(), dame.getSoundVolume(), dame.getVoicePitch());
		}

		//Horrible horrible ad hoc n�2
		private int getAttackCount() {
			switch (chosenAttack) {
				case 1:
					return 8 + dame.rage * 2;
				default:
				case 0:
				case 2:
					return 4 + dame.rage;
			}
		}

		@Override
		public void tick() {
			super.tick();
			dame.attackCooldown = 2;
			attackDelay--;
			if (attackDelay <= 0) {
				attackRemaining--;
				performAttack();
				if (attackRemaining <= 0) stop();
			}
		}

		//Horrible horrible ad hoc n�3
		private void performAttack() {
			double tx = target.getX();
			double ty = target.getY();
			double tz = target.getZ();
			switch (chosenAttack) {
				default:
				case 0:
					//Vertical lines
					//Copied a good deal from Bellringer
					attackDelay = 20;
					BlockPos self = dame.blockPosition();
					double sx = self.getX();
					double sz = self.getZ();
					Direction dir = Direction.getNearest(tx - sx, 0, tz - sz);
					double cx = dir.getStepX();
					double cz = dir.getStepZ();
					
					for (int i = -4; i <= 4; i++) {
						if ((i + 4) % 2 == attackRemaining % 2) {
							for (int y = 0; y < 3; y++) {
								ProjectileLineEntity proj = dame.readyLine();
								proj.setUp(10, cx, 0, cz, tx - 7*cx + 1.5*i*cz, ty + 1.5*y, tz - 7*cz + 1.5*i*cx);
								dame.level.addFreshEntity(proj);
							}
						}
					}
					
					dame.playSound(ModSounds.dameFortunaShoot.get(), 2.0F, (dame.random.nextFloat() - dame.random.nextFloat()) * 0.2F + 1.0F);
					break;
				case 1:
					//Harvester style Evoker jaws
					attackDelay = 10;
					double minY = Math.min(ty, dame.getY());
					double maxY = Math.max(ty, dame.getY()) + 1;
					float angle = (float) Mth.atan2(tz - dame.getZ(), tx - dame.getX());
					dame.spawnFangs(tx, tz, minY, maxY, angle, 0);
					break;
				case 2:
					//Grid above
					attackDelay = 20;
					for (int x = -3; x <= 3; x++) {
						for (int z = -3; z <= 3; z++) {
							if ((x + z + 6) % 2 != attackRemaining % 2) continue;
							ProjectileLineEntity proj = dame.readyLine();
							proj.setUp(15, 0, -1, 0, tx + x * 1.6, ty + 7, tz + z * 1.6);
							dame.level.addFreshEntity(proj);
						}
					}
					dame.playSound(ModSounds.dameFortunaShoot.get(), 2.0F, (dame.random.nextFloat() - dame.random.nextFloat()) * 0.2F + 1.0F);
					break;
			}
		}

		@Override
		public void stop() {
			dame.attackCooldown = 40 + dame.random.nextInt(21);
			dame.setAttack(NO_ATTACK);
		}

		@Override
		public boolean canContinueToUse() {
			return attackRemaining > 0 && target.isAlive();
		}

	}
	
	//Firing evoker lines when enraged
	private static class RageEvokerLines extends Goal {
		private DameFortunaEntity dame;
		private LivingEntity target;
		private int delay;

		public RageEvokerLines(DameFortunaEntity dame) {
			this.dame = dame;
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public boolean canUse() {
			return dame.rage >= 2 && dame.getTarget() != null && dame.getTarget().isAlive();
		}

		@Override
		public void start() {
			target = dame.getTarget();
			delay = 80;
		}

		@Override
		public void tick() {
			delay--;
			if (delay <= 0) {
				double tx = target.getX();
				double ty = target.getY();
				double tz = target.getZ();
				//Evoker lines
				double minY = Math.min(ty, dame.getY());
				double maxY = Math.max(ty, dame.getY()) + 1;
				float angle = (float) Mth.atan2(tz - dame.getZ(), tx - dame.getX());
				for (int i = 0; i < 16; ++i) {
					double dist = 1.25 * (i + 1);
					dame.spawnFangs(dame.getX() + Mth.cos(angle) * dist, dame.getZ() + Mth.sin(angle) * dist, minY, maxY, angle, i);
				}
			}
		}

		@Override
		public boolean canContinueToUse() {
			return delay > 0 && target.isAlive();
		}

	}

}
