package lykrast.meetyourfight.entity;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.ai.VexMoveRandomGoal;
import lykrast.meetyourfight.entity.movement.VexMovementController;
import lykrast.meetyourfight.registry.ModEntities;
import lykrast.meetyourfight.registry.ModSounds;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class BellringerEntity extends BossEntity {
	public int attackCooldown;
	private int rageAttacks = 0;
	
	public BellringerEntity(EntityType<? extends BellringerEntity> type, World worldIn) {
		super(type, worldIn);
		moveController = new VexMovementController(this);
		experienceValue = 50;
	}

	@Override
	public void move(MoverType typeIn, Vector3d pos) {
		super.move(typeIn, pos);
		doBlockCollisions();
	}

	@Override
	public void tick() {
		noClip = true;
		super.tick();
		noClip = false;
		setNoGravity(true);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		goalSelector.addGoal(0, new SwimGoal(this));
		goalSelector.addGoal(1, new RageAttack(this));
		goalSelector.addGoal(2, new BurstAttack(this));
		goalSelector.addGoal(7, new MoveFrontOfTarget(this));
		goalSelector.addGoal(8, new VexMoveRandomGoal(this));
		goalSelector.addGoal(9, new LookAtGoal(this, PlayerEntity.class, 3.0F, 1.0F));
		goalSelector.addGoal(10, new LookAtGoal(this, MobEntity.class, 8.0F));
		targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, false));
		targetSelector.addGoal(2, new HurtByTargetGoal(this));
	}
	
	public static AttributeModifierMap.MutableAttribute getAttributes() {
        return MobEntity.func_233666_p_().createMutableAttribute(Attributes.MAX_HEALTH, 200).createMutableAttribute(Attributes.FOLLOW_RANGE, 64);
    }
	
	public static void spawn(PlayerEntity player, World world) {
		Random rand = player.getRNG();
		BellringerEntity bellringer = ModEntities.BELLRINGER.create(world);
		bellringer.setLocationAndAngles(player.getPosX() + rand.nextInt(15) - 7, player.getPosY() + rand.nextInt(9) - 1, player.getPosZ() + rand.nextInt(15) - 7, rand.nextFloat() * 360 - 180, 0);
		bellringer.attackCooldown = 100;
		if (!player.abilities.isCreativeMode) bellringer.setAttackTarget(player);
		bellringer.addPotionEffect(new EffectInstance(Effects.RESISTANCE, 100, 2));

		bellringer.onInitialSpawn((ServerWorld) world, world.getDifficultyForLocation(bellringer.getPosition()), SpawnReason.EVENT, null, null);
		world.addEntity(bellringer);
		world.playSound(null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.BLOCK_BELL_USE, SoundCategory.PLAYERS, 2, 1);
	}
	
	@Override
	public void updateAITasks() {
		if (attackCooldown > 0) attackCooldown--;
		super.updateAITasks();
	}
	
	private void dingDong() {
		swingArm(Hand.MAIN_HAND);
        playSound(SoundEvents.BLOCK_BELL_USE, 2, 1);
	}
	
	private ProjectileLineEntity readyAttack() {
		ProjectileLineEntity ghost = new ProjectileLineEntity(world, this, 0, 0, 0);
		ghost.setShooter(this);
		ghost.setPosition(getPosX() - 2 + rand.nextDouble() * 4, getPosY() - 2 + rand.nextDouble() * 4, getPosZ() - 2 + rand.nextDouble() * 4);
		ghost.setVariant(ProjectileLineEntity.VAR_BELLRINGER);
		return ghost;
	}
	
	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		if (compound.contains("AttackCooldown")) attackCooldown = compound.getInt("AttackCooldown");
		rageAttacks = compound.getInt("Rage");

	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.putInt("AttackCooldown", attackCooldown);
		compound.putInt("Rage", rageAttacks);
	}

	@Override
	public CreatureAttribute getCreatureAttribute() {
		return CreatureAttribute.UNDEAD;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.bellringerIdle;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return ModSounds.bellringerHurt;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.bellringerDeath;
	}

	@Override
	protected SoundEvent getMusic() {
		return ModSounds.musicMagnum;
	}
	
	@Override
	protected ResourceLocation getLootTable() {
		return MeetYourFight.rl("bellringer");
	}

	@Override
	public float getBrightness() {
		return 1.0F;
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
		public boolean shouldExecute() {
			return ringer.attackCooldown <= 0 && ringer.getAttackTarget() != null && ringer.getAttackTarget().isAlive();
		}

		@Override
		public void startExecuting() {
			ringer.attackCooldown = 2;
			attackDelay = 20;
			attackRemaining = 3 + ringer.rageAttacks;
			target = ringer.getAttackTarget();
			chosenAttack = ringer.rand.nextInt(2);
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
				
				if (attackRemaining <= 0) resetTask();
			}
		}
		
		private void performAttack() {
			//BlockPos rounds values so +0.5 for center of block
			BlockPos tgt = target.getPosition();
			double tx = tgt.getX() + 0.5;
			double tz = tgt.getZ() + 0.5;
			double ty = tgt.getY() + 0.1;
			//Prevents lines being unjumpable if an attack is launched mid jump
			if (!target.isOnGround() && !target.isInWater() && !ringer.world.getBlockState(tgt.down()).getMaterial().blocksMovement()) ty -= 1;
			switch (chosenAttack) {
				default:
				case 0:
					//Lines at target
					BlockPos self = ringer.getPosition();
					double sx = self.getX();
					double sz = self.getZ();
					Direction dir = Direction.getFacingFromVector(tx - sx, 0, tz - sz);
					double cx = dir.getXOffset();
					double cz = dir.getZOffset();
					
					for (int i = -4; i <= 4; i++) {
						ProjectileLineEntity ghost = ringer.readyAttack();
						ghost.setUp(20, cx, 0, cz, tx - 7*cx + i*cz, ty, tz - 7*cz + i*cx);
						ringer.world.addEntity(ghost);
					}
					break;
				case 1:
					//Attacks from above
					for (int x = -1; x <= 1; x++) {
						for (int z = -1; z <= 1; z++) {
							ProjectileLineEntity ghost = ringer.readyAttack();
							ghost.setUp(20, 0, -1, 0, tx + x, ty + 7, tz + z);
							ringer.world.addEntity(ghost);
						}
					}
					break;
			}
		}

		@Override
		public void resetTask() {
			ringer.attackCooldown = 40 + ringer.rand.nextInt(21);
		}

		@Override
		public boolean shouldContinueExecuting() {
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
		public boolean shouldExecute() {
			return ringer.attackCooldown <= 0 && ringer.rageAttacks == 0 && ringer.getHealth() <= ringer.getMaxHealth() / 2 && ringer.getAttackTarget() != null && ringer.getAttackTarget().isAlive();
		}

		@Override
		public void startExecuting() {
			ringer.attackCooldown = 2;
			ringer.rageAttacks = 1;
			attackDelay = 30;
			attackRemaining = 20;
			target = ringer.getAttackTarget();
			
			BlockPos self = ringer.getPosition();
			double sx = self.getX();
			double sz = self.getZ();
			BlockPos tgt = target.getPosition();
			double tx = tgt.getX();
			double tz = tgt.getZ();
			dir = Direction.getFacingFromVector(tx - sx, 0, tz - sz);
			
			//Slowness
			List<Entity> list = ringer.world.getEntitiesInAABBexcluding(ringer, ringer.getBoundingBox().grow(16), e -> e instanceof LivingEntity && e.isAlive() && e.isNonBoss());
			list.add(target);
			for (Entity e : list) {
				//Duration should last through the whole attack
				((LivingEntity)e).addPotionEffect(new EffectInstance(Effects.SLOWNESS, 300, 1));
			}
			ringer.dingDong();
			ringer.playSound(SoundEvents.BLOCK_BELL_RESONATE, 2, 1);
		}

		@Override
		public void tick() {
			ringer.attackCooldown = 2;
			attackDelay--;
			if (attackDelay <= 0) {
				attackDelay = 12;
				attackRemaining--;
				ringer.dingDong();
				
				//BlockPos rounds values so +0.5 for center of block
				BlockPos tgt = target.getPosition();
				double tx = tgt.getX() + 0.5;
				double tz = tgt.getZ() + 0.5;
				double ty = tgt.getY() + 0.1;
				//Prevents lines being unjumpable if an attack is launched mid jump
				if (!target.isOnGround() && !target.isInWater() && !ringer.world.getBlockState(tgt.down()).getMaterial().blocksMovement()) ty -= 1;

				double cx = dir.getXOffset();
				double cz = dir.getZOffset();
				
				int off = attackRemaining % 2 == 0 ? 1 : -1;
				for (int i = -5; i <= 5; i++) {
					ProjectileLineEntity ghost = ringer.readyAttack();
					ghost.setUp(15 + off*i, cx, 0, cz, tx - 7*cx + i*cz, ty, tz - 7*cz + i*cx);
					ringer.world.addEntity(ghost);
				}
				
				if (attackRemaining <= 0) resetTask();
			}
		}

		@Override
		public void resetTask() {
			ringer.attackCooldown = 40 + ringer.rand.nextInt(21);
		}

		@Override
		public boolean shouldContinueExecuting() {
			return attackRemaining > 0 && target.isAlive();
		}
		
	}
	
	//Stay in front of attack target
	private static class MoveFrontOfTarget extends Goal {
		private MobEntity mob;
		private int moveCooldown;

		public MoveFrontOfTarget(MobEntity mob) {
			setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
			this.mob = mob;
		}

		@Override
		public boolean shouldExecute() {
			return mob.getAttackTarget() != null && !mob.getMoveHelper().isUpdating();
		}

		@Override
		public void startExecuting() {
			moveCooldown = 20;
			
			LivingEntity target = mob.getAttackTarget();
			BlockPos targetP = target.getPosition();
			Vector3d look = Vector3d.fromPitchYaw(0, target.rotationYaw);

			mob.getMoveHelper().setMoveTo(
					targetP.getX() + look.x * 4 - 0.5 + mob.getRNG().nextDouble() * 2, 
					targetP.getY() + 2 + mob.getRNG().nextDouble() * 2, 
					targetP.getZ() + look.z * 4 - 0.5 + mob.getRNG().nextDouble() * 2,
					1);
		}

		@Override
		public boolean shouldContinueExecuting() {
			return moveCooldown > 0;
		}

		@Override
		public void tick() {
			moveCooldown--;
		}

	}

}
