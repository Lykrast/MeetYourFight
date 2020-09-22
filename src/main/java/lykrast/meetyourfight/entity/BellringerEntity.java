package lykrast.meetyourfight.entity;

import java.util.EnumSet;

import javax.annotation.Nullable;

import lykrast.meetyourfight.entity.ai.VexMoveRandomGoal;
import lykrast.meetyourfight.entity.movement.VexMovementController;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
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
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

public class BellringerEntity extends MonsterEntity {
	private int attackCooldown;
	
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
		goalSelector.addGoal(2, new BurstAttack(this));
		goalSelector.addGoal(7, new MoveFrontOfTarget(this));
		goalSelector.addGoal(8, new VexMoveRandomGoal(this));
		goalSelector.addGoal(9, new LookAtGoal(this, PlayerEntity.class, 3.0F, 1.0F));
		goalSelector.addGoal(10, new LookAtGoal(this, MobEntity.class, 8.0F));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
		targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
	}
	
	public static AttributeModifierMap.MutableAttribute getAttributes() {
        return MobEntity.func_233666_p_().createMutableAttribute(Attributes.MAX_HEALTH, 20);
    }
	
	@Override
	public void livingTick() {
		if (attackCooldown > 0) attackCooldown--;
		super.livingTick();
	}
	
	private void dingDong() {
		swingArm(Hand.MAIN_HAND);
        playSound(SoundEvents.BLOCK_BELL_USE, 2, 1);
	}
	

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		if (compound.contains("AttackCooldown")) {
			attackCooldown = compound.getInt("AttackCooldown");
		}

	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.putInt("AttackCooldown", attackCooldown);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_VEX_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_VEX_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.ENTITY_VEX_HURT;
	}

	@Override
	public float getBrightness() {
		return 1.0F;
	}

	@Override
	@Nullable
	public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
		this.setEquipmentBasedOnDifficulty(difficultyIn);
		return super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
	}

	@Override
	protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
		this.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.BELL));
		this.setDropChance(EquipmentSlotType.MAINHAND, 0.0F);
	}
	
	private static class BurstAttack extends Goal {
		private BellringerEntity ringer;
		private LivingEntity target;
		private int attackRemaining, attackDelay;

		public BurstAttack(BellringerEntity ringer) {
			this.ringer = ringer;
		}

		@Override
		public boolean shouldExecute() {
			return ringer.attackCooldown <= 0 && ringer.getAttackTarget() != null && ringer.getAttackTarget().isAlive();
		}

		@Override
		public void startExecuting() {
			attackDelay = 20;
			attackRemaining = 3;
			target = ringer.getAttackTarget();
		}

		@Override
		public void tick() {
			attackDelay--;
			if (attackDelay <= 0) {
				attackDelay = 20;
				attackRemaining--;

				ringer.dingDong();
				
				BlockPos self = ringer.getPosition();
				double sx = self.getX();
				double sy = self.getY();
				double sz = self.getZ();
				BlockPos tgt = target.getPosition();
				double tx = tgt.getX();
				double ty = tgt.getY() + 0.1;
				double tz = tgt.getZ();
				
				Direction dir = Direction.getFacingFromVector(tx - sx, 0, tz - sz);
//				Direction dir = target.getAdjustedHorizontalFacing().getOpposite();
//				int change = ringer.rand.nextInt(3);
//				if (change == 1) dir = dir.rotateY();
//				else if (change == 2) dir = dir.rotateYCCW();
				double cx = dir.getXOffset();
				double cz = dir.getZOffset();
				
				for (int i = -4; i <= 4; i++) {
					GhostLineEntity ghost = new GhostLineEntity(ringer.world, ringer, 0, 0, 0);
					ghost.setShooter(ringer);
					ghost.setPosition(sx - 2 + ringer.rand.nextDouble() * 4, sy - 2 + ringer.rand.nextDouble() * 4, sz - 2 + ringer.rand.nextDouble() * 4);
					ghost.setUp(20, cx, 0, cz, tx - 7*cx + i*cz, ty, tz - 7*cz + i*cx);
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
