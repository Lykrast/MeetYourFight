package lykrast.meetyourfight.entity;

import java.util.EnumSet;

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
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DameFortunaEntity extends BossEntity implements PowerableMob {
	/**
	 * Look at me I can embed attacks AND rage in a single byte! 0x0000RRAA with R for rage (0-2) and A for attack (0-2)
	 */
	private static final EntityDataAccessor<Byte> STATUS = SynchedEntityData.defineId(DameFortunaEntity.class, EntityDataSerializers.BYTE);
	//Phases
	public static final int PHASE_1 = 0, SHUFFLE_1 = 1, PHASE_2 = 2, SHUFFLE_2 = 3, PHASE_3 = 4, SHUFFLE_3 = 5, DEATH = 6;
	//Health tresholds for phases
	private static final float TRESHOLD_1 = 2f/3, TRESHOLD_2 = 1f/3, TRESHOLD_3 = 1f/10;
	//Heal values for failed shuffles
	private static final float RESET_1 = (1 + TRESHOLD_1)/2, RESET_2 = (TRESHOLD_1 + TRESHOLD_2)/2, RESET_3 = (TRESHOLD_2 + TRESHOLD_3)/2;
	//Attacks
	private static final int ATK_CHIPS = 0, ATK_DICE = 1, ATK_SPIN = 2;
	//Animations
	public static final int ANIM_IDLE = 0, ANIM_ATTACK_1 = 1, ANIM_ATTACK_2 = 2, ANIM_SPIN = 3;
	private static final int PHASE_MASK = 0b111, ANIMATION_MASK = ~PHASE_MASK;
	private int attackCooldown, nextAttack, chipsCooldown;
	private int phase;
	private boolean hasSpawnedShuffle = false;
	/**
	 * Since the animation only rotates in 90°, it's given in 90° by 0 1 2 or 3
	 */
	public int headTargetPitch, headTargetYaw, headTargetRoll;
	public int headRotationTimer;
	public float headRotationProgress, headRotationProgressLast;
	public int clientAnim, prevAnim, animProg, animDur, spinTime;
	public float spinAngle, spinPrev;
	
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
		clientAnim = ANIM_IDLE;
		prevAnim = ANIM_IDLE;
		animProg = 1;
		animDur = 1;
		spinAngle = 0;
		spinPrev = 0;
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new WaitShuffle(this));
		goalSelector.addGoal(2, new DoTheShuffle(this));
		goalSelector.addGoal(3, new SpinAttack(this));
		goalSelector.addGoal(4, new DiceAttack(this));
		goalSelector.addGoal(5, new ChipsAttack(this));
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
			int newanim = getAnimation();
			if (clientAnim != newanim) {
				prevAnim = clientAnim;
				clientAnim = newanim;
				animProg = 0;
				animDur = 10;
			}
			else if (animProg < animDur) animProg++;
			
			//spin
			spinPrev = spinAngle;
			if (clientAnim == ANIM_SPIN) {
				//spinning up
				if (spinTime < 20) {
					spinTime++;
					//keep spin angle at 0 to know our offset when we spin down
				}
				//stable spinning
				else {
					spinAngle += 36;
					if (spinAngle >= 360) spinAngle = 0;
				}
			}
			else {
				if (spinAngle > 0) {
					//finish our full turn 
					spinAngle += 36;
					if (spinAngle >= 360) spinAngle = 0;
				}
				else if (spinTime > 0) {
					spinTime--;
				}
			}
			
			//head
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
	
	public float getAnimProgress(float partial) {
		return Mth.clamp((animProg + partial) / animDur, 0, 1);
	}
	
	public float getHeadRotationProgress(float partial) {
	      return Mth.lerp(partial, headRotationProgressLast, headRotationProgress);
	}
	
	private float getEasedSpin(float progress) {
		//0.9x^2 means we spin a full 360° after 20 ticks
		//and derivative at x = 20 is 36, matching the constant speed rate when stable
		return 0.9f*progress*progress;
	}
	
	public float getSpinAngle(float partial) {
		if (clientAnim == ANIM_SPIN) {
			//spinning up
			if (spinTime < 20) return getEasedSpin(spinTime + partial);
			//stable spin
			else return Mth.rotLerp(partial, spinPrev, spinAngle);
		}
		//spinning down
		else {
			//finishing the turn
			if (spinAngle > 0) return Mth.rotLerp(partial, spinPrev, spinAngle);
			//actual spin down
			else return 360-getEasedSpin(spinTime - partial);
		}
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
		dame.nextAttack = ATK_CHIPS;
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
		if (!source.isBypassInvul() && isPowered()) {
			if (amount > 1) playSound(ModSounds.aceOfIronProc.get(), 1, 1);
			return false;
		}
		return super.hurt(source, amount);
	}

	@Override
	public boolean isPowered() {
		//For armor layer
		return getPhase() == SHUFFLE_1 || getPhase() == SHUFFLE_2 || getPhase() == SHUFFLE_3;
	}
	
	@Override
	public void customServerAiStep() {
		if (attackCooldown > 0) attackCooldown--;
		if (chipsCooldown > 0) chipsCooldown--;
		if (phase != getPhase()) phase = getPhase();
		//Start phase transitions
		if (isShuffling() && tickCount % 10 == 0) {
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
	
	public boolean isAttackPhase() {
		return phase == PHASE_1 || phase == PHASE_2 || phase == PHASE_3;
	}
	
	public boolean isShuffling() {
		return phase == SHUFFLE_1 || phase == SHUFFLE_2 || phase == SHUFFLE_3;
	}
	
	private void rollNextAttack(int ignore) {
		if (ignore >= 0) {
			nextAttack = random.nextInt(2);
			if (nextAttack >= ignore) nextAttack++;
		}
		else nextAttack = random.nextInt(3);
	}
	
	private void cooldownNextAttack() {
		attackCooldown = 50 + random.nextInt(21);
	}
	
//	private ProjectileLineEntity readyLine() {
//		ProjectileLineEntity proj = new ProjectileLineEntity(level, this);
//		proj.setOwner(this);
//		proj.setPos(getX(), getEyeY() + 1, getZ());
//		proj.setVariant(ProjectileLineEntity.VAR_DAME_FORTUNA);
//		return proj;
//	}
	
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
		if (compound.contains("NxtAt")) nextAttack = compound.getInt("NxtAt");
		if (compound.contains("ChipsCooldown")) chipsCooldown = compound.getInt("ChipsCooldown");
		if (compound.contains("HasShuffled")) hasSpawnedShuffle = compound.getBoolean("HasShuffled");
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putByte("Phase", (byte)getPhase());
		compound.putInt("AttackCooldown", attackCooldown);
		compound.putInt("NxtAt", nextAttack);
		compound.putInt("ChipsCooldown", chipsCooldown);
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
		public void start() {
			super.start();
			dame.setAnimation(ANIM_IDLE);
		}

		@Override
		public void stop() {
			dame.setAnimation(ANIM_IDLE);
		}

		@Override
		public boolean canContinueToUse() {
			return dame.isShuffling();
		}

		@Override
		public boolean canUse() {
			return dame.isShuffling() && dame.hasSpawnedShuffle;
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
			dame.setAnimation(ANIM_ATTACK_1);
			dame.playSound(ModSounds.dameFortunaAttack.get(), dame.getSoundVolume(), dame.getVoicePitch());
			//Wait for chips to clean up if possible
			timer = Math.max(30, dame.chipsCooldown + 20);
		}

		@Override
		public void tick() {
			super.tick();
			dame.attackCooldown = 2;
			timer--;
			if (timer <= 0 && !dame.hasSpawnedShuffle) {
				dame.setAnimation(ANIM_IDLE);
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
					card.setup(i, correct, i == correct, i * 10 + 5, center.getX(), center.getY(), center.getZ(), i * (360 / cards), start.x + 3 * shuffled[i] * side.getStepX(), start.y,
							start.z + 3 * shuffled[i] * side.getStepZ());
					dame.level.addFreshEntity(card);
				}
				//Hang around a bit before going in the wait animation
				timer = FortunaCardEntity.START_TIME + FortunaCardEntity.GOTODEST_TIME + FortunaCardEntity.SPIN_TIME;
			}
		}

		@Override
		public void stop() {
			dame.setAnimation(ANIM_IDLE);
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
			return dame.isShuffling() && !dame.hasSpawnedShuffle && dame.getTarget() != null && dame.getTarget().isAlive();
		}

		@Override
		public boolean canContinueToUse() {
			return !dame.hasSpawnedShuffle || timer > 0;
		}
	}
	
	//Regular attacks below
	private static class DiceAttack extends StationaryAttack {
		private DameFortunaEntity dame;
		private LivingEntity target;
		private int attackRemaining, attackDelay;

		public DiceAttack(DameFortunaEntity dame) {
			super(dame);
			this.dame = dame;
		}

		@Override
		public boolean canUse() {
			return dame.nextAttack == ATK_DICE && dame.isAttackPhase() && dame.attackCooldown <= 0 && dame.getTarget() != null && dame.getTarget().isAlive();
		}

		@Override
		public boolean canContinueToUse() {
			return dame.nextAttack == ATK_DICE && attackRemaining > 0 && target.isAlive() && dame.isAttackPhase();
		}

		@Override
		public void start() {
			super.start();
			target = dame.getTarget();
			dame.setAnimation(ANIM_ATTACK_1);
			attackDelay = 30;
			attackRemaining = getAttackCount();
			dame.playSound(ModSounds.dameFortunaAttack.get(), dame.getSoundVolume(), dame.getVoicePitch());
		}

		private int getAttackCount() {
			if (dame.phase == PHASE_2) return 4 + dame.random.nextInt(3);
			else if (dame.phase == PHASE_3) return 8 + dame.random.nextInt(4);
			//Phase 1
			return 2 + dame.random.nextInt(2);
		}

		@Override
		public void tick() {
			super.tick();
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
			//Dice bombs
			attackDelay = dame.phase == PHASE_3 ? 12 : 20;
			//don't want the bombs to be thrown behind the target, so we aim a 60� cone
			//don't think the yrot can get negative values so wrap it is
			Vec3 offset = new Vec3(dame.getX() - tx,0,dame.getZ() - tz).normalize().yRot(Mth.wrapDegrees(dame.random.nextFloat()*60 - 30)*Mth.DEG_TO_RAD);
			double bombX = tx + offset.x*3;
			double bombY = ty + 0.5;
			double bombZ = tz + offset.z*3;
			if (target.isOnGround()) bombY += 0.75;
			FortunaBombEntity bomb = new FortunaBombEntity(dame.level, dame.getX(), dame.getY() + 2, dame.getZ(), dame);
			int dettime = dame.phase == PHASE_1 ? 0 : dame.random.nextInt(11);
			bomb.setup(25 + dettime, 15 + dettime, bombX, bombY, bombZ);
			dame.level.addFreshEntity(bomb);
			dame.playSound(ModSounds.dameFortunaShoot.get(), 2.0F, (dame.random.nextFloat() - dame.random.nextFloat()) * 0.2F + 1.0F);
		}

		@Override
		public void stop() {
			dame.cooldownNextAttack();
			dame.rollNextAttack(ATK_DICE);
			//if we going to shuffle phase, let that handle the next animation
			if (dame.isAttackPhase()) dame.setAnimation(ANIM_IDLE);
		}

	}
	
	private static class ChipsAttack extends StationaryAttack {
		private DameFortunaEntity dame;
		private LivingEntity target;
		private int attackRemaining, attackDelay;

		public ChipsAttack(DameFortunaEntity dame) {
			super(dame);
			this.dame = dame;
		}

		@Override
		public boolean canUse() {
			return dame.nextAttack == ATK_CHIPS && dame.isAttackPhase() && dame.attackCooldown <= 0 && dame.getTarget() != null && dame.getTarget().isAlive();
		}

		@Override
		public boolean canContinueToUse() {
			return dame.nextAttack == ATK_CHIPS && attackRemaining > 0 && target.isAlive() && dame.isAttackPhase();
		}

		@Override
		public void start() {
			super.start();
			target = dame.getTarget();
			dame.setAnimation(ANIM_ATTACK_1);
			attackDelay = 30;
			attackRemaining = getAttackCount();
			dame.playSound(ModSounds.dameFortunaAttack.get(), dame.getSoundVolume(), dame.getVoicePitch());
		}

		private int getAttackCount() {
			if (dame.phase == PHASE_3) return 2 + dame.random.nextInt(3);
			else if (dame.phase == PHASE_2) return 2 + dame.random.nextInt(2);
			else return 1 + dame.random.nextInt(2);
		}

		@Override
		public void tick() {
			super.tick();
			attackDelay--;
			if (attackDelay <= 0) {
				attackRemaining--;
				performAttack();
				if (attackRemaining <= 0) stop();
			}
		}
		
		private void performAttack() {
			//Homing chips
			//Only phase 3 will have multiple attacks here
			attackDelay = 45;
			
			int chips = 6;
			if (dame.phase == PHASE_2) chips = 8;
			else if (dame.phase == PHASE_3) chips = 12;
			
			int delay = 2;
			//Get a random shape between vertical stack, side to side stack, and circle around fortuna
			switch (dame.random.nextInt(3)) {
				default:
				case 0:
					fireChipsStack(chips, delay, false);
					break;
				case 1:
					fireChipsStack(chips, delay, true);
					break;
				case 2:
					fireChipsCircle(chips, delay);
					break;
			}
		}
		
		private void fireChipsStack(int number, int delay, boolean horizontal) {
			//should be pointing to like left/right (doesn't matter) of her
			Vec3 perp = dame.getLookAngle().cross(new Vec3(0,1,0)).normalize();
			double sy = dame.getY() + 1;
			
			//Don't want 2 sets of chips at the same time
			dame.chipsCooldown = Math.max(dame.chipsCooldown, 20 + number*delay);
			
			for (int dir = -1; dir <= 1; dir += 2) {
				double sx = dame.getX() + perp.x*dir;
				double sz = dame.getZ() + perp.z*dir;
				int intialdelay = dir == -1 ? 30 : 35;
				//-1 and 1 to have both sides
				for (int i = 0; i < number; i++) {
					ProjectileTargetedEntity proj = dame.readyTargeted();
					proj.setPos(sx, sy + i*0.125, sz);
					if (horizontal) proj.setUp(intialdelay + (number-i-1)*delay, 15, target, 1, sx + i*perp.x*dir, sy + 1, sz + i*perp.z*dir);
					else proj.setUp(intialdelay + (number-i-1)*delay, 15, target, 1, sx, sy + i*0.5 + 0.25, sz);
					dame.level.addFreshEntity(proj);
				}
			}
			
			dame.playSound(ModSounds.dameFortunaChipsStart.get(), 2.0F, (dame.random.nextFloat() - dame.random.nextFloat()) * 0.2F + 1.0F);
		}
		
		//TODO Copy pasted fireChipsStack as a base, but like now lot of the logic is duplicated oh no
		private void fireChipsCircle(int number, int delay) {
			//should be pointing to like left/right (doesn't matter) of her
			Vec3 perp = dame.getLookAngle().cross(new Vec3(0,1,0)).normalize();
			double sy = dame.getY() + 1;
			float angle = Mth.PI / number;
			
			//Don't want 2 sets of chips at the same time
			dame.chipsCooldown = Math.max(dame.chipsCooldown, 20 + number*delay);
			
			for (int dir = -1; dir <= 1; dir += 2) {
				double damex = dame.getX();
				double damez = dame.getZ();
				double sx = damex + perp.x*dir;
				double sz = damez + perp.z*dir;
				int intialdelay = dir == -1 ? 25 : 30;
				//-1 and 1 to have both sides
				Vec3 offset = perp.scale(dir);
				for (int i = 0; i < number; i++) {
					ProjectileTargetedEntity proj = dame.readyTargeted();
					proj.setPos(sx, sy + i*0.125, sz);
					proj.setUp(intialdelay + (number-i-1)*delay, 15, target, 1, damex + 3*offset.x, sy+1, damez + 3*offset.z);
					dame.level.addFreshEntity(proj);
					offset = offset.yRot(angle);
				}
			}
			
			dame.playSound(ModSounds.dameFortunaChipsStart.get(), 2.0F, (dame.random.nextFloat() - dame.random.nextFloat()) * 0.2F + 1.0F);
		}

		@Override
		public void stop() {
			dame.cooldownNextAttack();
			dame.rollNextAttack(ATK_CHIPS);
			//if we going to shuffle phase, let that handle the next animation
			if (dame.isAttackPhase()) dame.setAnimation(ANIM_IDLE);
		}

	}
	
	private static class SpinAttack extends Goal {
		private DameFortunaEntity dame;
		private int timer, chipsLeft, attackPhase;
		private double holdx, holdy, holdz;
		//attackPhase 0 = start animation, 1 = chase and throw chips, 2 = final animation
		
		public SpinAttack(DameFortunaEntity dame) {
			this.dame = dame;
			setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public boolean canUse() {
			return dame.nextAttack == ATK_SPIN && dame.isAttackPhase() && dame.attackCooldown <= 0 && dame.getTarget() != null && dame.getTarget().isAlive();
		}

		@Override
		public boolean canContinueToUse() {
			return canUse() && (chipsLeft > 0 || timer > 0);
		}
		
		@Override
		public void start() {
			timer = 20;
			chipsLeft = 6 + dame.random.nextInt(5);
			if (dame.phase == PHASE_2) chipsLeft = 12 + dame.random.nextInt(11);
			else if (dame.phase == PHASE_3) chipsLeft = 24 + dame.random.nextInt(21);
			attackPhase = 0;
			holdx = dame.getX();
			holdy = dame.getTarget().getY() + 1;
			holdz = dame.getZ();
			dame.moveControl.setWantedPosition(holdx, holdy, holdz, 1);
			dame.setAnimation(ANIM_SPIN);
		}
		
		@Override
		public void tick() {
			timer--;
			LivingEntity target = dame.getTarget();
			//Start animation
			if (attackPhase == 0) {
				dame.moveControl.setWantedPosition(holdx, holdy, holdz, 1);
				if (timer <= 0) {
					attackPhase = 1;
					timer = 10;
				}
			}
			//Chasing
			else if (attackPhase == 1) {
				dame.moveControl.setWantedPosition(target.getX(), target.getY() + 1, target.getZ(), 0.4);
				if (timer <= 0) {
					fireChips(target);
					chipsLeft--;
					if (chipsLeft > 0) {
						timer = dame.phase == PHASE_3 ? 6 : dame.phase == PHASE_2 ? 12 : 20;
					}
					else {
						attackPhase = 2;
						timer = 20;
						holdx = dame.getX();
						holdy = dame.getTarget().getY() + 1;
						holdz = dame.getZ();
						dame.moveControl.setWantedPosition(holdx, holdy, holdz, 1);
						dame.setAnimation(ANIM_IDLE);
					}
				}
			}
			//Finish animation
			else {
				dame.moveControl.setWantedPosition(holdx, holdy, holdz, 1);
			}
		}
		private void fireChips(LivingEntity target) {
			//should be pointing to like left/right (doesn't matter) of her
			Vec3 perp = dame.getLookAngle().cross(new Vec3(0,1,0)).normalize();
			perp = perp.yRot(dame.random.nextFloat() * Mth.TWO_PI);
			double sy = dame.getY() + 1;
			
			double damex = dame.getX();
			double damez = dame.getZ();
			ProjectileTargetedEntity proj = dame.readyTargeted();
			proj.setPos(damex, sy, damez);
			proj.setUp(10, 15, target, 1, damex + 1*perp.x, sy+1, damez + 1*perp.z);
			dame.level.addFreshEntity(proj);
		}

		@Override
		public void stop() {
			dame.cooldownNextAttack();
			dame.rollNextAttack(ATK_SPIN);
			if (dame.isAttackPhase() && attackPhase < 2) dame.setAnimation(ANIM_IDLE);
		}
		
	}

}
