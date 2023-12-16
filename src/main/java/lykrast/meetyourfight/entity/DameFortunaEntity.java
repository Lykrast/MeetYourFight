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
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DameFortunaEntity extends BossEntity {
	/**
	 * Look at me I can embed attacks AND rage in a single byte! 0x0000RRAA with R for rage (0-2) and A for attack (0-2)
	 */
	private static final EntityDataAccessor<Byte> STATUS = SynchedEntityData.defineId(DameFortunaEntity.class, EntityDataSerializers.BYTE);
	public static final int PHASE_1 = 0, SHUFFLE_1 = 1, PHASE_2 = 2, SHUFFLE_2 = 3, PHASE_3 = 4, SHUFFLE_3 = 5, DEATH = 6;
	private static final float TRESHOLD_1 = 2f/3, TRESHOLD_2 = 1f/3, TRESHOLD_3 = 1f/10;
	private static final float RESET_1 = (1 + TRESHOLD_1)/2, RESET_2 = (TRESHOLD_1 + TRESHOLD_2)/2, RESET_3 = (TRESHOLD_2 + TRESHOLD_3)/2;
	public static final int NO_ATTACK = 0, PROJ_ATTACK = 1, CLAW_ATTACK = 2;
	private static final int PHASE_MASK = 0b111, ANIMATION_MASK = ~PHASE_MASK;
	public int attackCooldown;
	private int phase;
	private boolean hasSpawnedShuffle = false;
	/**
	 * Since the animation only rotates in 90ï¿½, it's given in 90ï¿½ by 0 1 2 or 3
	 */
	public int headTargetPitch, headTargetYaw, headTargetRoll;
	public int headRotationTimer;
	public float headRotationProgress, headRotationProgressLast;
	
	public DameFortunaEntity(EntityType<? extends DameFortunaEntity> type, Level worldIn) {
		super(type, worldIn);
		moveControl = new VexMovementController(this);
		xpReward = 100;
		phase = 0;
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
		goalSelector.addGoal(1, new WaitShuffle(this));
		goalSelector.addGoal(2, new DoTheShuffle(this));
		goalSelector.addGoal(3, new RegularAttack(this));
		goalSelector.addGoal(7, new MoveAroundTarget(this, 1));
		goalSelector.addGoal(8, new VexMoveRandomGoal(this, 0.25));
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
				switch (getPhase()) {
					default:
					case PHASE_1:
					case SHUFFLE_1:
						headRotationTimer = 20 + random.nextInt(21);
						break;
					case PHASE_2:
					case SHUFFLE_2:
						headRotationTimer = 15 + random.nextInt(11);
						break;
					case PHASE_3:
					case SHUFFLE_3:
					case DEATH:
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
	
	public int getAnimation() {
		return (entityData.get(STATUS) & ANIMATION_MASK) >> 3;
	}
	
	public void setAnimation(int animation) {
		int phase = entityData.get(STATUS) & PHASE_MASK;
		entityData.set(STATUS, (byte)((animation << 3) | phase));
	}
	
	public int getPhase() {
		return entityData.get(STATUS) & PHASE_MASK;
	}
	
	public void setPhase(int phase) {
		int animation = entityData.get(STATUS) & ANIMATION_MASK;
		entityData.set(STATUS, (byte)(phase | animation));
	}
	
	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (!source.isBypassInvul() && (getPhase() == SHUFFLE_1 || getPhase() == SHUFFLE_2 || getPhase() == SHUFFLE_3)) {
			if (amount > 1) playSound(ModSounds.aceOfIronProc.get(), 1, 1);
			return false;
		}
		return super.hurt(source, amount);
	}
	
	@Override
	public void customServerAiStep() {
		if (attackCooldown > 0) attackCooldown--;
		if (phase != getPhase()) phase = getPhase();
		//Start phase transitions
		if ((phase == SHUFFLE_1 || phase == SHUFFLE_2 || phase == SHUFFLE_3) && tickCount % 10 == 0) {
			if (hasSpawnedShuffle && level.getEntitiesOfClass(FortunaCardEntity.class, getBoundingBox().inflate(32)).isEmpty()) {
				//If we hit a correct card we get booted out of the shuffle phase, so here if it's failed
				if (phase == SHUFFLE_1) {
					setHealth(getMaxHealth()*RESET_1);
					setPhase(PHASE_1);
					phase = PHASE_1;
				}
				else if (phase == SHUFFLE_2) {
					setHealth(getMaxHealth()*RESET_2);
					setPhase(PHASE_2);
					phase = PHASE_2;
				}
				else if (phase == SHUFFLE_3) {
					setHealth(getMaxHealth()*RESET_3);
					setPhase(PHASE_3);
					phase = PHASE_3;
				}
			}
		}
		else if (phase == PHASE_1 && getHealth() < getMaxHealth()*TRESHOLD_1) {
			setPhase(SHUFFLE_1);
			phase = SHUFFLE_1;
			hasSpawnedShuffle = false;
		}
		else if (phase == PHASE_2 && getHealth() < getMaxHealth()*TRESHOLD_2) {
			setPhase(SHUFFLE_2);
			phase = SHUFFLE_2;
			hasSpawnedShuffle = false;
		}
		else if (phase == PHASE_3 && getHealth() < getMaxHealth()*TRESHOLD_3) {
			setPhase(SHUFFLE_3);
			phase = SHUFFLE_3;
			hasSpawnedShuffle = false;
		}
		super.customServerAiStep();
	}
	
	public void progressShuffle() {
		if (phase == SHUFFLE_1) {
			setPhase(PHASE_2);
			phase = PHASE_2;
		}
		else if (phase == SHUFFLE_2) {
			setPhase(PHASE_3);
			phase = PHASE_3;
		}
		else if (phase == SHUFFLE_3) {
			setPhase(DEATH);
			phase = DEATH;
		}
		attackCooldown = 20;
	}
	
	private ProjectileLineEntity readyLine() {
		ProjectileLineEntity proj = new ProjectileLineEntity(level, this);
		proj.setOwner(this);
		proj.setPos(getX(), getEyeY() + 1, getZ());
		proj.setVariant(ProjectileLineEntity.VAR_DAME_FORTUNA);
		return proj;
	}
	
	private ProjectileTargetedEntity readyTargeted() {
		ProjectileTargetedEntity proj = new ProjectileTargetedEntity(level, this);
		proj.setOwner(this);
		proj.setPos(getX(), getEyeY() + 1, getZ());
		return proj;
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("Phase")) setPhase(compound.getByte("Phase"));
		if (compound.contains("AttackCooldown")) attackCooldown = compound.getInt("AttackCooldown");
		if (compound.contains("HasShuffled")) hasSpawnedShuffle = compound.getBoolean("HasShuffled");
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putByte("Phase", (byte)getPhase());
		compound.putInt("AttackCooldown", attackCooldown);
		compound.putBoolean("HasShuffled", hasSpawnedShuffle);
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
	
	private static class WaitShuffle extends StationaryAttack {
		//Wait around while shuffle is ongoing
		private DameFortunaEntity dame;
		
		public WaitShuffle(DameFortunaEntity dame) {
			super(dame, 4);
			this.dame = dame;
		}

		@Override
		public boolean canContinueToUse() {
			return dame.phase == SHUFFLE_1 || dame.phase == SHUFFLE_2 || dame.phase == SHUFFLE_3;
		}

		@Override
		public boolean canUse() {
			return (dame.phase == SHUFFLE_1 || dame.phase == SHUFFLE_2 || dame.phase == SHUFFLE_3) && dame.hasSpawnedShuffle;
		}
		
	}
	
	private static class DoTheShuffle extends StationaryAttack {
		//Do the shuffle attack
		private DameFortunaEntity dame;
		private LivingEntity target;
		private int timer;

		public DoTheShuffle(DameFortunaEntity dame) {
			super(dame, 4);
			this.dame = dame;
		}

		@Override
		public void start() {
			super.start();
			dame.attackCooldown = 2;
			target = dame.getTarget();
			dame.setAnimation(PROJ_ATTACK);
			dame.playSound(ModSounds.dameFortunaAttack.get(), dame.getSoundVolume(), dame.getVoicePitch());
			timer = 30;
		}

		@Override
		public void tick() {
			super.tick();
			dame.attackCooldown = 2;
			timer--;
			if (timer <= 0) {
				dame.hasSpawnedShuffle = true;
				Direction dir = Direction.getNearest(target.getX() - dame.getX(), 0, target.getZ() - dame.getZ());
				Direction side = dir.getClockWise();
				int cards = 2;
				if (dame.phase == SHUFFLE_2) cards = 3;
				else if (dame.phase == SHUFFLE_3) cards = 4;
				//Determine in what order will the cards actually be shuffled
				int[] shuffled = new int[cards];
				for (int i = 0; i < cards; i++) shuffled[i] = i;
				shuffle(shuffled);
				//Which card will be the correct one
				int correct = dame.random.nextInt(cards);
				//Evenly space out the cards on the line
				BlockPos center = new BlockPos(dame.getX(), target.getY() + 1, dame.getZ());
				Vec3 start = new Vec3(center.getX() - side.getStepX() * 1.5 * (cards - 1), center.getY(), center.getZ() - side.getStepZ() * 1.5 * (cards - 1));
				for (int i = 0; i < cards; i++) {
					FortunaCardEntity card = new FortunaCardEntity(dame.level, start.x + 3 * i * side.getStepX(), start.y, start.z + 3 * i * side.getStepZ());
					card.setYRot(dir.toYRot());
					card.setup(i, i == correct, i * 10 + 5, center.getX(), center.getY(), center.getZ(), i * (360 / cards), start.x + 3 * shuffled[i] * side.getStepX(), start.y,
							start.z + 3 * shuffled[i] * side.getStepZ());
					dame.level.addFreshEntity(card);
				}
			}
		}
		
		//To use the boss's random gotta remade a fisher yates shuffle
		private void shuffle(int[] arr) {
			for (int i = 0; i < arr.length-1; i++) {
				int j = dame.random.nextInt(i, arr.length);
				int swap = arr[i];
				arr[i] = arr[j];
				arr[j] = swap;
			}
		}

		@Override
		public boolean canUse() {
			return (dame.phase == SHUFFLE_1 || dame.phase == SHUFFLE_2 || dame.phase == SHUFFLE_3) && !dame.hasSpawnedShuffle && dame.getTarget() != null && dame.getTarget().isAlive();
		}

		@Override
		public boolean canContinueToUse() {
			return !dame.hasSpawnedShuffle;
		}
	}
	
	//The regular attacks
	private static class RegularAttack extends StationaryAttack {
		private DameFortunaEntity dame;
		private LivingEntity target;
		private int attackRemaining, attackDelay, chosenAttack;

		public RegularAttack(DameFortunaEntity dame) {
			super(dame);
			this.dame = dame;
		}

		@Override
		public boolean canUse() {
			return (dame.phase == PHASE_1 || dame.phase == PHASE_2 || dame.phase == PHASE_3) && dame.attackCooldown <= 0 && dame.getTarget() != null && dame.getTarget().isAlive();
		}

		@Override
		public void start() {
			super.start();
			dame.attackCooldown = 2;
			target = dame.getTarget();
			chosenAttack = dame.random.nextInt(3);
			//Choose animation depending on the attack
			//Horrible ad hoc nï¿½1
			dame.setAnimation(chosenAttack == 1 ? CLAW_ATTACK : PROJ_ATTACK);
			attackDelay = 30;
			attackRemaining = getAttackCount();
			dame.playSound(ModSounds.dameFortunaAttack.get(), dame.getSoundVolume(), dame.getVoicePitch());
		}

		private int getAttackCount() {
			switch (chosenAttack) {
				case 1:
					return 8 + dame.phase * 2;
				default:
				case 0:
					return 2;
				case 2:
					return 4 + dame.phase;
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
		
		private void performAttack() {
			double tx = target.getX();
			double ty = target.getY();
			double tz = target.getZ();
			switch (chosenAttack) {
				default:
				case 0:
					//Homing chips
					attackDelay = 5;
					//should be pointing to like left/right (doesn't matter) of her
					Vec3 perp = dame.getLookAngle().cross(new Vec3(0,1,0)).normalize();
					if (attackRemaining % 2 == 0) perp = perp.scale(-1);
					double sx = dame.getX() + perp.x;
					double sy = dame.getY() + 1;
					double sz = dame.getZ() + perp.y;
					
					for (int i = 0; i < 8; i++) {
						ProjectileTargetedEntity proj = dame.readyTargeted();
						proj.setPos(sx, sy + i*0.125, sz);
						proj.setUp(85 - 10*i, 10, target, 1, sx, sy + i*0.5 + 0.25, sz);
						dame.level.addFreshEntity(proj);
					}
					
					dame.playSound(ModSounds.dameFortunaChipsStart.get(), 2.0F, (dame.random.nextFloat() - dame.random.nextFloat()) * 0.2F + 1.0F);
					break;
				case 1:
					//Attack that spawn cardinal around player
					//Like the old harvester claw attack
					attackDelay = 11;
					if (target.isOnGround()) ty += 1.25;
					projAroundTarget(tx, ty, tz, 1, 0);
					projAroundTarget(tx, ty, tz, -1, 0);
					projAroundTarget(tx, ty, tz, 0, 1);
					projAroundTarget(tx, ty, tz, 0, -1);
					dame.playSound(ModSounds.dameFortunaShoot.get(), 2.0F, (dame.random.nextFloat() - dame.random.nextFloat()) * 0.2F + 1.0F);
					break;
				case 2:
					//Dice bombs
					attackDelay = 20;
					//don't want the bombs to be thrown behind the target, so we aim a 60° cone
					//don't think the yrot can get negative values so wrap it is
					Vec3 offset = new Vec3(dame.getX() - tx,0,dame.getZ() - tz).normalize().yRot(Mth.wrapDegrees(dame.random.nextFloat()*60 - 30)*Mth.DEG_TO_RAD);
					double bombX = tx + offset.x*3;
					double bombY = ty;
					double bombZ = tz + offset.z*3;
					if (target.isOnGround()) bombY += 1.25;
					FortunaBombEntity bomb = new FortunaBombEntity(dame.level, dame.getX(), dame.getY() + 2, dame.getZ(), dame);
					bomb.setup(25, 15, bombX, bombY, bombZ);
					dame.level.addFreshEntity(bomb);
					dame.playSound(ModSounds.dameFortunaShoot.get(), 2.0F, (dame.random.nextFloat() - dame.random.nextFloat()) * 0.2F + 1.0F);
					break;
			}
		}
		
		private void projAroundTarget(double tx, double ty, double tz, double dx, double dz) {
			ProjectileLineEntity proj = dame.readyLine();
			proj.setPos(tx - dx * 6, ty - 2, tz - dz * 6);
			proj.setUp(10, dx, 0, dz, tx - dx * 6, ty, tz - dz * 6);
			dame.level.addFreshEntity(proj);
		}

		@Override
		public void stop() {
			dame.attackCooldown = 50 + dame.random.nextInt(21);
			dame.setAnimation(NO_ATTACK);
		}

		@Override
		public boolean canContinueToUse() {
			return attackRemaining > 0 && target.isAlive();
		}

	}

}
