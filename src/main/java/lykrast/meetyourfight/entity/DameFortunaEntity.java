package lykrast.meetyourfight.entity;

import java.util.EnumSet;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.ai.MoveAroundTarget;
import lykrast.meetyourfight.entity.ai.StationaryAttack;
import lykrast.meetyourfight.entity.ai.VexMoveRandomGoal;
import lykrast.meetyourfight.entity.movement.VexMovementController;
import lykrast.meetyourfight.misc.FortunaSpinSound;
import lykrast.meetyourfight.registry.MYFEntities;
import lykrast.meetyourfight.registry.MYFSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;

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
	private static final int ATK_DICE = 0, ATK_SPIN = 1, ATK_CHIPS_CIRCLE = 2, ATK_CHIPS_STRAFE = 3;
	//Animations
	public static final int ANIM_IDLE = 0, ANIM_CHIPS_WINDUP = 1, ANIM_CHIPS_LAUNCH = 2, ANIM_DICE_WINDUP = 3, ANIM_DICE_LAUNCH = 4, ANIM_SPIN = 5, ANIM_SPIN_POSE = 6,
			ANIM_SNAP_PRE = 7, ANIM_SNAP_POST = 8, ANIM_CARD_WAIT = 9, ANIM_FINALE = 10, ANIM_CLAP = 11;
	private static final int PHASE_MASK = 0b111, ANIMATION_MASK = ~PHASE_MASK;
	private int attackCooldown, nextAttack, shuffleAttackWait;
	private int phase;
	private boolean hasSpawnedShuffle = false;
	/**
	 * Since the animation only rotates in 90°, it's given in 90° by 0 1 2 or 3
	 */
	public int headTargetPitch, headTargetYaw, headTargetRoll;
	public int headRotationTimer;
	public float headRotationProgress, headRotationProgressLast;
	public int clientAnim, prevAnim, animProg, animDur, spinTime, headRegrowTime;
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
		goalSelector.addGoal(1, new DoTheShuffle(this));
		goalSelector.addGoal(2, new WaitShuffle(this));
		goalSelector.addGoal(3, new EndPose(this));
		goalSelector.addGoal(4, new SpinAttack(this));
		goalSelector.addGoal(5, new DiceAttack(this));
		goalSelector.addGoal(6, new ChipsAttack(this));
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
				if (clientAnim == ANIM_DICE_LAUNCH) {
					 animDur = 4;
					 headRegrowTime = 11; //it'll get decreased to 10 right after
					 headRotationTimer = 0;
				}
				else if (clientAnim == ANIM_CHIPS_LAUNCH) animDur = 4;
				else if (clientAnim == ANIM_DICE_WINDUP) animDur = 8;
				else if (clientAnim == ANIM_SNAP_POST || clientAnim == ANIM_CLAP) animDur = 2;
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
			if (headRegrowTime > 0) headRegrowTime--;
			if (clientAnim != ANIM_DICE_WINDUP) headRotationTimer--;
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
			else if (spinTime > 0) return 360-getEasedSpin(spinTime - partial);
			else return 0;
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
		DameFortunaEntity dame = MYFEntities.DAME_FORTUNA.get().create(world);
		dame.moveTo(player.getX() + rand.nextInt(5) - 2, player.getY() + rand.nextInt(3) + 3, player.getZ() + rand.nextInt(5) - 2, rand.nextFloat() * 360 - 180, 0);
		dame.attackCooldown = 100;
		dame.nextAttack = ATK_CHIPS_CIRCLE;
		if (!player.getAbilities().instabuild) dame.setTarget(player);
		dame.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 2));

		ForgeEventFactory.onFinalizeSpawn(dame, (ServerLevel) world, world.getCurrentDifficultyAt(dame.blockPosition()), MobSpawnType.EVENT, null, null);
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
		if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && isPowered()) {
			if (amount > 1) playSound(MYFSounds.aceOfIronProc.get(), 1, 1);
			return false;
		}
		else if (amount > 1 && getPhase() == DEATH) return super.hurt(source, Math.max(getHealth()*2, amount));
		else return super.hurt(source, amount);
	}

	@Override
	public boolean isPowered() {
		//For armor layer
		return getPhase() == SHUFFLE_1 || getPhase() == SHUFFLE_2 || getPhase() == SHUFFLE_3;
	}
	
	@Override
	public void customServerAiStep() {
		if (attackCooldown > 0) attackCooldown--;
		if (shuffleAttackWait > 0) shuffleAttackWait--;
		if (phase != getPhase()) phase = getPhase();
		//Start phase transitions
		if (isShuffling() && tickCount % 10 == 0) {
			if (hasSpawnedShuffle && level().getEntitiesOfClass(FortunaCardEntity.class, getBoundingBox().inflate(32)).isEmpty()) {
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
		int max = ATK_CHIPS_CIRCLE;
		if (phase == PHASE_2 || phase == PHASE_3) max = ATK_CHIPS_STRAFE;
		if (ignore >= 0) {
			nextAttack = random.nextInt(max);
			if (nextAttack >= ignore) nextAttack++;
		}
		else nextAttack = random.nextInt(max+1);
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
		ProjectileTargetedEntity proj = new ProjectileTargetedEntity(level(), this);
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
		if (compound.contains("ChipsCooldown")) shuffleAttackWait = compound.getInt("ChipsCooldown");
		if (compound.contains("HasShuffled")) hasSpawnedShuffle = compound.getBoolean("HasShuffled");
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putByte("Phase", (byte)getPhase());
		compound.putInt("AttackCooldown", attackCooldown);
		compound.putInt("NxtAt", nextAttack);
		compound.putInt("ChipsCooldown", shuffleAttackWait);
		compound.putBoolean("HasShuffled", hasSpawnedShuffle);
	}
	
	@Override
	protected SoundEvent getAmbientSound() {
		return MYFSounds.dameFortunaIdle.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return MYFSounds.dameFortunaHurt.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MYFSounds.dameFortunaDeath.get();
	}

	@Override
	protected SoundEvent getMusic() {
		return MYFSounds.musicMagnum.get();
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void readSpawnData(FriendlyByteBuf additionalData) {
		super.readSpawnData(additionalData);
		Minecraft.getInstance().getSoundManager().play(new FortunaSpinSound(this));
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
			dame.setAnimation(ANIM_CARD_WAIT);
		}

		@Override
		public void stop() {
			dame.setAnimation(ANIM_IDLE);
			dame.attackCooldown = 20;
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
			dame.setAnimation(ANIM_SNAP_PRE);
			dame.playSound(MYFSounds.dameFortunaAttack.get(), dame.getSoundVolume(), dame.getVoicePitch());
			//Wait for attacks to be done if possible
			timer = Math.max(40, dame.shuffleAttackWait + 30);
		}

		@Override
		public void tick() {
			super.tick();
			dame.attackCooldown = 2;
			timer--;
			if (timer <= 0 && !dame.hasSpawnedShuffle) {
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
				BlockPos center = BlockPos.containing(dame.getX(), target.getY() + 1, dame.getZ());
				Vec3 start = new Vec3(center.getX() - side.getStepX() * 1.5 * (cards - 1), center.getY(), center.getZ() - side.getStepZ() * 1.5 * (cards - 1));
				for (int i = 0; i < cards; i++) {
					FortunaCardEntity card = new FortunaCardEntity(dame.level(), start.x + 3 * i * side.getStepX(), start.y, start.z + 3 * i * side.getStepZ());
					card.setYRot(dir.toYRot());
					//aaaaaaa this was hell to figure out to make it consistent across all orientations
					//the minus sign on the yrot was hard part (and 360 is cause negative mod in java gives negative)
					int angleOffset = (i * (360 / cards) + 360 - (int)dir.toYRot()) % 360;
					card.setup(i, correct, i == correct, i * 10 + 5, center.getX(), center.getY() + 3, center.getZ(), angleOffset, start.x + 3 * shuffled[i] * side.getStepX(), start.y,
							start.z + 3 * shuffled[i] * side.getStepZ());
					dame.level().addFreshEntity(card);
				}
				//Hang around a bit before going in the wait animation
				timer = FortunaCardEntity.START_TIME;
			}
			else if (timer == 10 && !dame.hasSpawnedShuffle) {
				//Snap!
				dame.playSound(MYFSounds.dameFortunaSnap.get(), 2.0F, (dame.random.nextFloat() - dame.random.nextFloat()) * 0.1F + 1.0F);
				dame.setAnimation(ANIM_SNAP_POST);
			}
			else if (timer == (FortunaCardEntity.START_TIME - 20) && dame.hasSpawnedShuffle) {
				dame.setAnimation(ANIM_IDLE);
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
			return dame.nextAttack == ATK_DICE && (attackDelay > 0 || attackRemaining > 0) && target.isAlive() && dame.isAttackPhase();
		}

		@Override
		public void start() {
			super.start();
			target = dame.getTarget();
			dame.setAnimation(ANIM_DICE_WINDUP);
			attackDelay = 30;
			attackRemaining = getAttackCount();
			dame.playSound(MYFSounds.dameFortunaAttack.get(), dame.getSoundVolume(), dame.getVoicePitch());
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
			if (attackDelay <= 0 && attackRemaining > 0) {
				attackRemaining--;
				dame.setAnimation(ANIM_DICE_LAUNCH);
				performAttack();
			}
			if (attackDelay == (dame.phase == PHASE_3 ? 8 : 10) && attackRemaining > 0) dame.setAnimation(ANIM_DICE_WINDUP);
		}
		
		private void performAttack() {
			double tx = target.getX();
			double ty = target.getY();
			double tz = target.getZ();
			//Dice bombs
			attackDelay = attackRemaining == 0 ? 40 : dame.phase == PHASE_3 ? 12 : 20;
			
			dame.shuffleAttackWait = Math.max(dame.shuffleAttackWait, 30);
			
			//don't want the bombs to be thrown behind the target, so we aim a 60° cone
			//don't think the yrot can get negative values so wrap it is
			Vec3 offset = new Vec3(dame.getX() - tx,0,dame.getZ() - tz).normalize().yRot(Mth.wrapDegrees(dame.random.nextFloat()*60 - 30)*Mth.DEG_TO_RAD);
			double bombX = tx + offset.x*3;
			double bombY = ty + 0.5;
			double bombZ = tz + offset.z*3;
			if (target.onGround()) bombY += 0.75;
			FortunaBombEntity bomb = new FortunaBombEntity(dame.level(), dame.getX(), dame.getY() + 2, dame.getZ(), dame);
			int dettime = dame.phase == PHASE_1 ? 0 : dame.random.nextInt(11);
			bomb.setup(25 + dettime, 15 + dettime, bombX, bombY, bombZ);
			dame.level().addFreshEntity(bomb);
			dame.playSound(MYFSounds.dameFortunaShoot.get(), 2.0F, (dame.random.nextFloat() - dame.random.nextFloat()) * 0.2F + 1.0F);
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
		private int attackRemaining, attackDelay, chosenPattern, circleDelay, circleDirection;
		private int clapTime, midStrafe;

		public ChipsAttack(DameFortunaEntity dame) {
			super(dame);
			this.dame = dame;
		}

		@Override
		public boolean canUse() {
			return dame.nextAttack >= ATK_CHIPS_CIRCLE && dame.nextAttack <= ATK_CHIPS_STRAFE && dame.isAttackPhase() && dame.attackCooldown <= 0 && dame.getTarget() != null && dame.getTarget().isAlive();
		}

		@Override
		public boolean canContinueToUse() {
			return dame.nextAttack >= ATK_CHIPS_CIRCLE && dame.nextAttack <= ATK_CHIPS_STRAFE && (attackDelay > 0 || attackRemaining > 0) && target.isAlive() && dame.isAttackPhase();
		}

		@Override
		public void start() {
			super.start();
			target = dame.getTarget();
			dame.setAnimation(ANIM_CHIPS_WINDUP);
			attackDelay = 20;
			dame.playSound(MYFSounds.dameFortunaAttack.get(), dame.getSoundVolume(), dame.getVoicePitch());
			chosenPattern = dame.nextAttack;
			switch (chosenPattern) {
				default:
				case ATK_CHIPS_CIRCLE:
					//midStrafe is the second clap
					midStrafe = 0;
					if (dame.phase == PHASE_3) {
						attackRemaining = 3 + dame.random.nextInt(3);
						//midStrafe is 1 + attacks on second strafe
						midStrafe = 4 + dame.random.nextInt(3);
						attackRemaining += midStrafe;
					}
					else if (dame.phase == PHASE_2) attackRemaining = 3 + dame.random.nextInt(2);
					else attackRemaining = 3;
					//first fire all the chips, then they get fired at 15 ticks interval
					//first one is fire 20 (was 15, not updating formula here) ticks after last one, so (20*(totalAttacks-1) + 15) after being placed
					//the next one fires 15 ticks later but is placed 20 later, so -5 to that delay
					//so each one should have a delay of 15*totalAttacks + 5*attackRemaining
					circleDelay = 15*(midStrafe > 0 ? attackRemaining-midStrafe : attackRemaining) + 5;
					circleDirection = dame.random.nextBoolean() ? 1 : -1;
					//clap 7 ticks before the circles are launched
					//so that we sound 5 ticks before they're launch (when animation is finished)
					//delay is at 45 the tick we fire the last circle
					//for the midstrafe the delay will be at 20
					clapTime = midStrafe > 0 ? 7 : 32;
					break;
				case ATK_CHIPS_STRAFE:
					attackRemaining = 2;
					clapTime = 17;
					break;
				case 0:
					//TODO find a phase 3 attack
					attackRemaining = 2;
					break;
			}
		}

		@Override
		public void tick() {
			super.tick();
			attackDelay--;
			if (attackRemaining > 0 && attackRemaining != midStrafe) {
				if (attackDelay <= 0) {
					attackRemaining--;
					dame.setAnimation(ANIM_CHIPS_LAUNCH);
					performAttack();
				}
				else if (attackDelay == 15) dame.setAnimation(ANIM_CHIPS_WINDUP);
			}
			else {
				//clap
				if (attackDelay == clapTime) dame.setAnimation(ANIM_CLAP);
				else if (attackDelay == clapTime - 2) dame.playSound(MYFSounds.dameFortunaClap.get(), 2.0F, (dame.random.nextFloat() - dame.random.nextFloat()) * 0.1F + 1.0F);
				//second strafe
				else if (attackDelay <= 0 && attackRemaining > 0 && attackRemaining == midStrafe) {
					attackRemaining--;
					midStrafe = 0;
					circleDelay = 15*attackRemaining + 5;
					clapTime = 32;
					attackDelay = 20;
					rotateAroundTarget();
				}
			}
		}
		
		private void performAttack() {
			attackDelay = 45;
			if (chosenPattern == ATK_CHIPS_CIRCLE && attackRemaining > 0) attackDelay = 20;
			
			switch (chosenPattern) {
				default:
				case ATK_CHIPS_CIRCLE:
					fireChipsCircle(8, 1, circleDelay + 5*(midStrafe > 0 ? attackRemaining-midStrafe-1 : attackRemaining));
					if (attackRemaining > 0 && attackRemaining != midStrafe) rotateAroundTarget();
					break;
				case ATK_CHIPS_STRAFE:
					if (attackRemaining == 1) fireChipsStack(dame.phase == PHASE_3 ? 20 : 16);
					else fireChipsCircle(8, (dame.phase == PHASE_3 ? 5 : 3) + dame.random.nextInt(2), 35);
					break;
				case 0:
					//TODO find a phase 3 attack
					attackRemaining = 2;
					break;
			}
		}
		
		private void rotateAroundTarget() {
			//like the MoveAroundTarget goal but with more speed
			float angle = (dame.random.nextInt(4) + 4) * 10f * Mth.DEG_TO_RAD * circleDirection;
			Vec3 offset = new Vec3(dame.getX() - target.getX(), 0, dame.getZ() - target.getZ()).normalize().yRot(angle);
			double distance = dame.random.nextDouble() * 2 + 4;

			dame.getMoveControl().setWantedPosition(
					target.getX() + offset.x * distance, 
					target.getY() + 1 + dame.random.nextDouble() * 2, 
					target.getZ() + offset.z * distance,
					3);
		}
		
		private void fireChipsStack(int number) {
			//should be pointing to like left/right (doesn't matter) of her
			Vec3 perp = dame.getLookAngle().cross(new Vec3(0,1,0)).normalize();
			double sy = dame.getY() + 1;
			
			dame.shuffleAttackWait = Math.max(dame.shuffleAttackWait, 38 + number*6);
			
			for (int dir = -1; dir <= 1; dir += 2) {
				double sx = dame.getX() + perp.x*dir;
				double sz = dame.getZ() + perp.z*dir;
				int intialdelay = dir == -1 ? 35 : 38;
				//-1 and 1 to have both sides
				for (int i = 0; i < number; i++) {
					ProjectileTargetedEntity proj = dame.readyTargeted();
					proj.setPos(sx, sy + i*0.125, sz);
					proj.setUp(intialdelay + (number-i-1)*6, 15, target, 0.75, sx, sy + i*0.25 + 0.25, sz, dir*-20);
					dame.level().addFreshEntity(proj);
				}
			}
			
			dame.playSound(MYFSounds.dameFortunaChipsStart.get(), 2.0F, (dame.random.nextFloat() - dame.random.nextFloat()) * 0.2F + 1.0F);
		}
		
		private void fireChipsCircle(int chips, int circles, int delay) {
			//should be pointing to like left/right (doesn't matter) of her
			Vec3 perp = dame.getLookAngle().cross(new Vec3(0,1,0)).normalize();
			double sy = dame.getY() + 1;
			float angle = Mth.TWO_PI / chips;
			
			dame.shuffleAttackWait = Math.max(dame.shuffleAttackWait, 35 + 15*(circles-1));

			double damex = dame.getX();
			double damez = dame.getZ();
			double sx = damex;
			double sz = damez;
			
			for (int c = 0; c < circles; c++) {
				Vec3 offset = perp;
				for (int i = 0; i < chips; i++) {
					ProjectileTargetedEntity proj = dame.readyTargeted();
					proj.setPos(sx, sy + (c*chips+i)*0.125, sz);
					proj.setUp(delay + 15*c, 15, target, 1, damex + 2*offset.x, sy+1+c, damez + 2*offset.z);
					dame.level().addFreshEntity(proj);
					offset = offset.yRot(angle);
				}
			}
			
			dame.playSound(MYFSounds.dameFortunaChipsStart.get(), 2.0F, (dame.random.nextFloat() - dame.random.nextFloat()) * 0.2F + 1.0F);
		}

		@Override
		public void stop() {
			dame.cooldownNextAttack();
			dame.rollNextAttack(chosenPattern);
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
			chipsLeft = 5 + dame.random.nextInt(3);
			if (dame.phase == PHASE_2) chipsLeft = 10 + dame.random.nextInt(5);
			else if (dame.phase == PHASE_3) chipsLeft = 20 + dame.random.nextInt(9);
			attackPhase = 0;
			holdx = dame.getX();
			holdy = dame.getTarget().getY() + 1;
			holdz = dame.getZ();
			dame.moveControl.setWantedPosition(holdx, holdy, holdz, 1);
			dame.setAnimation(ANIM_SPIN);
			dame.playSound(MYFSounds.dameFortunaSpinStart.get(), 2, 1);
			dame.playSound(MYFSounds.dameFortunaAttack.get(), dame.getSoundVolume(), dame.getVoicePitch());
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
						timer = 30;
						holdx = dame.getX();
						holdy = dame.getTarget().getY() + 1;
						holdz = dame.getZ();
						dame.moveControl.setWantedPosition(holdx, holdy, holdz, 1);
						dame.setAnimation(ANIM_SPIN_POSE);
						dame.playSound(MYFSounds.dameFortunaSpinStop.get(), 2, 1);
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
			
			dame.shuffleAttackWait = Math.max(dame.shuffleAttackWait, 10);
			
			double damex = dame.getX();
			double damez = dame.getZ();
			ProjectileTargetedEntity proj = dame.readyTargeted();
			proj.setPos(damex, sy, damez);
			proj.setUp(10, 15, target, 1, damex + 1*perp.x, sy+1, damez + 1*perp.z);
			dame.level().addFreshEntity(proj);
		}

		@Override
		public void stop() {
			dame.cooldownNextAttack();
			dame.rollNextAttack(ATK_SPIN);
			if (dame.isAttackPhase()) dame.setAnimation(ANIM_IDLE);
		}
		
	}
	
	//Hold the pose when defeated
	private static class EndPose extends StationaryAttack {
		private DameFortunaEntity dame;
		private int patience;

		public EndPose(DameFortunaEntity dame) {
			super(dame);
			this.dame = dame;
		}
		
		@Override
		public void start() {
			super.start();
			dame.setAnimation(ANIM_FINALE);
			//if not killed within 30 seconds (should be plenty generous), go back to the fight
			patience = 30*20;
			if (dame.getTarget() != null) stationaryY = dame.getTarget().getY() + 1;
		}
		
		@Override
		public void tick() {
			super.tick();
			patience--;
			if (patience <= 0) {
				dame.setHealth(dame.getMaxHealth()*RESET_3);
				dame.setPhase(PHASE_3);
				dame.phase = PHASE_3;
				dame.attackCooldown = 20;
				dame.setAnimation(ANIM_IDLE);
			}
		}

		@Override
		public boolean canUse() {
			return dame.phase == DEATH;
		}
	}

}
