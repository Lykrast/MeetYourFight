package lykrast.meetyourfight.entity;

import java.util.Random;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.ai.MoveAroundTarget;
import lykrast.meetyourfight.entity.ai.VexMoveRandomGoal;
import lykrast.meetyourfight.entity.movement.VexMovementController;
import lykrast.meetyourfight.registry.ModEntities;
import lykrast.meetyourfight.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class VelaEntity extends BossEntity {
	public int waterCooldown, airCooldown;
	
	public VelaEntity(EntityType<? extends VelaEntity> type, Level worldIn) {
		super(type, worldIn);
		moveControl = new VexMovementController(this);
		xpReward = 150;
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(2, new WaterAttack(this));
		goalSelector.addGoal(7, new MoveAroundTarget(this));
		goalSelector.addGoal(8, new VexMoveRandomGoal(this));
		goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
		goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
		targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
		targetSelector.addGoal(2, new HurtByTargetGoal(this));
	}
	
	public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 500).add(Attributes.ARMOR, 5).add(Attributes.FOLLOW_RANGE, 64);
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
	
	public static void spawn(Player player, Level world) {
		Random rand = player.getRandom();
		VelaEntity dame = ModEntities.VELA.create(world);
		dame.moveTo(player.getX() + rand.nextInt(5) - 2, player.getY() + rand.nextInt(3) + 3, player.getZ() + rand.nextInt(5) - 2, rand.nextFloat() * 360 - 180, 0);
		dame.waterCooldown = 100;
		dame.airCooldown = 150;
		if (!player.getAbilities().instabuild) dame.setTarget(player);
		dame.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 2));

		dame.finalizeSpawn((ServerLevel) world, world.getCurrentDifficultyAt(dame.blockPosition()), MobSpawnType.EVENT, null, null);
		world.addFreshEntity(dame);
	}
	
	@Override
	public void customServerAiStep() {
		if (waterCooldown > 0) waterCooldown--;
		if (airCooldown > 0) airCooldown--;
		super.customServerAiStep();
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("WCooldown")) waterCooldown = compound.getInt("WCooldown");
		if (compound.contains("ACooldown")) airCooldown = compound.getInt("ACooldown");
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putInt("WCooldown", waterCooldown);
		compound.putInt("ACooldown", airCooldown);
	}
	
	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.WITHER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.WITHER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.WITHER_DEATH;
	}

	@Override
	protected SoundEvent getMusic() {
		return ModSounds.musicMagnum;
	}
	
	@Override
	protected ResourceLocation getDefaultLootTable() {
		return MeetYourFight.rl("vela");
	}
	
	private static class WaterAttack extends Goal {
		private VelaEntity vela;
		private LivingEntity target;
		private int attackDelay, chosenAttack;

		public WaterAttack(VelaEntity vela) {
			this.vela = vela;
		}

		@Override
		public boolean canUse() {
			return vela.waterCooldown <= 0 && vela.getTarget() != null && vela.getTarget().isAlive();
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void start() {
			vela.waterCooldown = 2;
			attackDelay = 20;
			target = vela.getTarget();
			chosenAttack = vela.random.nextInt(2);
		}

		@Override
		public void tick() {
			vela.waterCooldown = 2;
			attackDelay--;
			if (attackDelay <= 0) {
				performAttack();
				stop();
			}
		}
		
		private void performAttack() {
			WaterBoulderEntity boulder = new WaterBoulderEntity(vela.level, vela, target);
			boulder.setOwner(vela);
			boulder.setPos(vela.getX(), vela.getY() + 1, vela.getZ());
			boulder.setUp(60, 0, -0.5, 0, 0, 5, 0);
			vela.level.addFreshEntity(boulder);
		}

		@Override
		public void stop() {
			vela.waterCooldown = 80 + vela.random.nextInt(41);
		}

		@Override
		public boolean canContinueToUse() {
			return attackDelay > 0 && target.isAlive();
		}
		
	}

}
