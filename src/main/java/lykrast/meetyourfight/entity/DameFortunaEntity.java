package lykrast.meetyourfight.entity;

import java.util.EnumSet;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.ai.VexMoveRandomGoal;
import lykrast.meetyourfight.entity.movement.VexMovementController;
import lykrast.meetyourfight.registry.ModSounds;
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
import net.minecraft.entity.monster.VexEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class DameFortunaEntity extends BossEntity {
	private static final DataParameter<Byte> ATTACK = EntityDataManager.createKey(DameFortunaEntity.class, DataSerializers.BYTE);
	public static final int NO_ATTACK = 0, SMALL_ATTACK = 1, BIG_ATTACK = 2;
	public int attackCooldown;
	
	public DameFortunaEntity(EntityType<? extends DameFortunaEntity> type, World worldIn) {
		super(type, worldIn);
		moveController = new VexMovementController(this);
		experienceValue = 100;
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
		goalSelector.addGoal(2, new RegularAttack(this));
		goalSelector.addGoal(8, new VexMoveRandomGoal(this));
		goalSelector.addGoal(9, new LookAtGoal(this, PlayerEntity.class, 3.0F, 1.0F));
		goalSelector.addGoal(10, new LookAtGoal(this, MobEntity.class, 8.0F));
		targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
		targetSelector.addGoal(2, new HurtByTargetGoal(this));
	}
	
	public static AttributeModifierMap.MutableAttribute getAttributes() {
        return MobEntity.func_233666_p_().createMutableAttribute(Attributes.MAX_HEALTH, 300).createMutableAttribute(Attributes.ARMOR, 5).createMutableAttribute(Attributes.FOLLOW_RANGE, 64);
    }

	@Override
	protected void registerData() {
		super.registerData();
		dataManager.register(ATTACK, (byte)0);
	}
	
	public int getAttack() {
		return dataManager.get(ATTACK);
	}
	
	public void setAttack(int attack) {
		dataManager.set(ATTACK, (byte)attack);
	}
	
	@Override
	public void updateAITasks() {
		if (attackCooldown > 0) attackCooldown--;
		super.updateAITasks();
	}
	
	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		if (compound.contains("AttackCooldown")) attackCooldown = compound.getInt("AttackCooldown");
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.putInt("AttackCooldown", attackCooldown);
	}
	
	//TODO change sounds
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
	protected SoundEvent getMusic() {
		return ModSounds.musicDameFortuna;
	}
	
	@Override
	protected ResourceLocation getLootTable() {
		return MeetYourFight.rl("dame_fortuna");
	}
	
	//The regular attacks
	private static class RegularAttack extends Goal {
		private DameFortunaEntity dame;
		private LivingEntity target;
		private int attackRemaining, attackDelay, chosenAttack;

		public RegularAttack(DameFortunaEntity dame) {
			setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
			this.dame = dame;
		}

		@Override
		public boolean shouldExecute() {
			return dame.attackCooldown <= 0 && dame.getAttackTarget() != null && dame.getAttackTarget().isAlive();
		}

		@Override
		public void startExecuting() {
			dame.attackCooldown = 2;
			attackDelay = 20;
			attackRemaining = 2;
			target = dame.getAttackTarget();
			chosenAttack = 0;
			dame.setAttack(SMALL_ATTACK);
		}

		@Override
		public void tick() {
			dame.attackCooldown = 2;
			attackDelay--;
			if (attackDelay <= 0) {
				attackDelay = 20;
				attackRemaining--;

				performAttack();

				if (attackRemaining <= 0) resetTask();
			}
		}

		private void performAttack() {
			switch (chosenAttack) {
				default:
				case 0:
					ServerWorld serverworld = (ServerWorld) dame.world;
					for (int i = 0; i < 3; ++i) {
						BlockPos blockpos = dame.getPosition().add(-2 + dame.rand.nextInt(5), 1, -2 + dame.rand.nextInt(5));
						VexEntity vexentity = EntityType.VEX.create(dame.world);
						vexentity.moveToBlockPosAndAngles(blockpos, 0.0F, 0.0F);
						vexentity.onInitialSpawn(serverworld, dame.world.getDifficultyForLocation(blockpos), SpawnReason.MOB_SUMMONED, null, null);
						vexentity.setOwner(dame);
						vexentity.setBoundOrigin(blockpos);
						vexentity.setLimitedLife(20 * (10 + dame.rand.nextInt(21)));
						vexentity.setAttackTarget(target);
						serverworld.func_242417_l(vexentity);
					}
					break;
			}
		}

		@Override
		public void resetTask() {
			dame.attackCooldown = 40 + dame.rand.nextInt(21);
			dame.setAttack(NO_ATTACK);
		}

		@Override
		public boolean shouldContinueExecuting() {
			return attackRemaining > 0 && target.isAlive();
		}

	}

}
