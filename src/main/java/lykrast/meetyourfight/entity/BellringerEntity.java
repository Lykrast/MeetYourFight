package lykrast.meetyourfight.entity;

import java.util.List;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.ai.MoveFrontOfTarget;
import lykrast.meetyourfight.entity.ai.VexMoveRandomGoal;
import lykrast.meetyourfight.entity.movement.VexMovementController;
import lykrast.meetyourfight.registry.MYFEntities;
import lykrast.meetyourfight.registry.MYFSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MoverType;
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
import net.minecraftforge.event.ForgeEventFactory;

public class BellringerEntity extends BossEntity {
	public int attackCooldown;
	private int rageAttacks = 0;
	
	public BellringerEntity(EntityType<? extends BellringerEntity> type, Level worldIn) {
		super(type, worldIn);
		moveControl = new VexMovementController(this);
		xpReward = 50;
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
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new RageAttack(this));
		goalSelector.addGoal(2, new BurstAttack(this));
		goalSelector.addGoal(7, new MoveFrontOfTarget(this, 1));
		goalSelector.addGoal(8, new VexMoveRandomGoal(this, 0.25));
		goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
		goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
		targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
		targetSelector.addGoal(2, new HurtByTargetGoal(this));
	}
	
	public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 200).add(Attributes.FOLLOW_RANGE, 64);
    }
	
	public static void spawn(Player player, Level world) {
		RandomSource rand = player.getRandom();
		BellringerEntity bellringer = MYFEntities.BELLRINGER.get().create(world);
		bellringer.moveTo(player.getX() + rand.nextInt(15) - 7, player.getY() + rand.nextInt(9) - 1, player.getZ() + rand.nextInt(15) - 7, rand.nextFloat() * 360 - 180, 0);
		bellringer.attackCooldown = 100;
		if (!player.getAbilities().instabuild) bellringer.setTarget(player);
		bellringer.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 2));

		ForgeEventFactory.onFinalizeSpawn(bellringer, (ServerLevel) world, world.getCurrentDifficultyAt(bellringer.blockPosition()), MobSpawnType.EVENT, null, null);
		world.addFreshEntity(bellringer);
		world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 2, 1);
	}
	
	@Override
	public void customServerAiStep() {
		if (attackCooldown > 0) attackCooldown--;
		super.customServerAiStep();
	}
	
	private void dingDong() {
		swing(InteractionHand.MAIN_HAND);
        playSound(SoundEvents.BELL_BLOCK, 2, 1);
	}
	
	private ProjectileLineEntity readyAttack() {
		ProjectileLineEntity ghost = new ProjectileLineEntity(level(), this);
		ghost.setOwner(this);
		ghost.setPos(getX() - 2 + random.nextDouble() * 4, getY() - 2 + random.nextDouble() * 4, getZ() - 2 + random.nextDouble() * 4);
		ghost.setVariant(ProjectileLineEntity.VAR_BELLRINGER);
		return ghost;
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("AttackCooldown")) attackCooldown = compound.getInt("AttackCooldown");
		rageAttacks = compound.getInt("Rage");

	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putInt("AttackCooldown", attackCooldown);
		compound.putInt("Rage", rageAttacks);
	}

	@Override
	public MobType getMobType() {
		return MobType.UNDEAD;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return MYFSounds.bellringerIdle.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return MYFSounds.bellringerHurt.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MYFSounds.bellringerDeath.get();
	}

	@Override
	protected SoundEvent getMusic() {
		return MYFSounds.musicBellringer.get();
	}
	
	@Override
	protected ResourceLocation getDefaultLootTable() {
		return MeetYourFight.rl("bellringer");
	}
	
	//The regular attacks
	private static class BurstAttack extends Goal {
		private BellringerEntity ringer;
		private LivingEntity target;
		private int attackRemaining, attackDelay, chosenAttack;

		public BurstAttack(BellringerEntity ringer) {
			this.ringer = ringer;
		}

		@Override
		public boolean canUse() {
			return ringer.attackCooldown <= 0 && ringer.getTarget() != null && ringer.getTarget().isAlive();
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void start() {
			ringer.attackCooldown = 2;
			attackDelay = 20;
			attackRemaining = 3 + ringer.rageAttacks;
			target = ringer.getTarget();
			chosenAttack = ringer.random.nextInt(2);
		}

		@Override
		public void tick() {
			ringer.attackCooldown = 2;
			attackDelay--;
			if (attackDelay <= 0) {
				attackDelay = 20;
				attackRemaining--;

				ringer.dingDong();
				
				performAttack();
				
				if (attackRemaining <= 0) stop();
			}
		}
		
		@SuppressWarnings("deprecation")
		private void performAttack() {
			//BlockPos rounds values so +0.5 for center of block
			BlockPos tgt = target.blockPosition();
			double tx = tgt.getX() + 0.5;
			double tz = tgt.getZ() + 0.5;
			double ty = tgt.getY() + 0.1;
			//Prevents lines being unjumpable if an attack is launched mid jump
			//uuh no clue what I'm supposed to replace the blocksMotion() with
			if (!target.onGround() && !target.isInWater() && !ringer.level().getBlockState(tgt.below()).blocksMotion()) ty -= 1;
			switch (chosenAttack) {
				default:
				case 0:
					//Lines at target
					BlockPos self = ringer.blockPosition();
					double sx = self.getX();
					double sz = self.getZ();
					Direction dir = Direction.getNearest(tx - sx, 0, tz - sz);
					double cx = dir.getStepX();
					double cz = dir.getStepZ();
					
					for (int i = -4; i <= 4; i++) {
						ProjectileLineEntity ghost = ringer.readyAttack();
						ghost.setUp(20, cx, 0, cz, tx - 7*cx + i*cz, ty, tz - 7*cz + i*cx);
						ringer.level().addFreshEntity(ghost);
					}
					break;
				case 1:
					//Attacks from above
					for (int x = -1; x <= 1; x++) {
						for (int z = -1; z <= 1; z++) {
							ProjectileLineEntity ghost = ringer.readyAttack();
							ghost.setUp(20, 0, -1, 0, tx + x, ty + 7, tz + z);
							ringer.level().addFreshEntity(ghost);
						}
					}
					break;
			}
		}

		@Override
		public void stop() {
			ringer.attackCooldown = 40 + ringer.random.nextInt(21);
		}

		@Override
		public boolean canContinueToUse() {
			return attackRemaining > 0 && target.isAlive();
		}
		
	}
	
	//Attacks that toggles rage mode
	private static class RageAttack extends Goal {
		private BellringerEntity ringer;
		private LivingEntity target;
		private int attackRemaining, attackDelay;
		private Direction dir;

		public RageAttack(BellringerEntity ringer) {
			this.ringer = ringer;
		}

		@Override
		public boolean canUse() {
			return ringer.attackCooldown <= 0 && ringer.rageAttacks == 0 && ringer.getHealth() <= ringer.getMaxHealth() / 2 && ringer.getTarget() != null && ringer.getTarget().isAlive();
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void start() {
			ringer.attackCooldown = 2;
			ringer.rageAttacks = 1;
			attackDelay = 30;
			attackRemaining = 16;
			target = ringer.getTarget();
			
			BlockPos self = ringer.blockPosition();
			double sx = self.getX();
			double sz = self.getZ();
			BlockPos tgt = target.blockPosition();
			double tx = tgt.getX();
			double tz = tgt.getZ();
			dir = Direction.getNearest(tx - sx, 0, tz - sz);
			
			//Slowness
			List<Entity> list = ringer.level().getEntities(ringer, ringer.getBoundingBox().inflate(16), e -> e instanceof LivingEntity && e.isAlive() && e.canChangeDimensions());
			list.add(target);
			for (Entity e : list) {
				//Duration should last through the whole attack
				((LivingEntity)e).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 1));
			}
			ringer.dingDong();
			ringer.playSound(SoundEvents.BELL_RESONATE, 2, 1);
		}

		@SuppressWarnings("deprecation")
		@Override
		public void tick() {
			ringer.attackCooldown = 2;
			attackDelay--;
			if (attackDelay <= 0) {
				attackDelay = 16;
				attackRemaining--;
				ringer.dingDong();
				
				//BlockPos rounds values so +0.5 for center of block
				BlockPos tgt = target.blockPosition();
				double tx = tgt.getX() + 0.5;
				double tz = tgt.getZ() + 0.5;
				double ty = tgt.getY() + 0.1;
				//Prevents lines being unjumpable if an attack is launched mid jump
				//uuh no clue what I'm supposed to replace the blocksMotion() with
				if (!target.onGround() && !target.isInWater() && !ringer.level().getBlockState(tgt.below()).blocksMotion()) ty -= 1;

				double cx = dir.getStepX();
				double cz = dir.getStepZ();
				
				int off = attackRemaining % 2 == 0 ? 1 : -1;
				for (int i = -5; i <= 5; i++) {
					ProjectileLineEntity ghost = ringer.readyAttack();
					ghost.setUp(15 + off*i, cx, 0, cz, tx - 7*cx + i*cz, ty, tz - 7*cz + i*cx);
					ringer.level().addFreshEntity(ghost);
				}
				
				if (attackRemaining <= 0) stop();
			}
		}

		@Override
		public void stop() {
			ringer.attackCooldown = 40 + ringer.random.nextInt(21);
		}

		@Override
		public boolean canContinueToUse() {
			return attackRemaining > 0 && target.isAlive();
		}
		
	}

}
