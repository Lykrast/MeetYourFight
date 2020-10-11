package lykrast.meetyourfight.entity;

import java.util.EnumSet;
import java.util.Random;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.ai.VexMoveRandomGoal;
import lykrast.meetyourfight.entity.movement.VexMovementController;
import lykrast.meetyourfight.registry.ModEntities;
import lykrast.meetyourfight.registry.ModSounds;
import net.minecraft.block.BlockState;
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
import net.minecraft.entity.projectile.EvokerFangsEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
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
		goalSelector.addGoal(7, new MoveAroundTarget(this));
		goalSelector.addGoal(8, new VexMoveRandomGoal(this));
		goalSelector.addGoal(9, new LookAtGoal(this, PlayerEntity.class, 3.0F, 1.0F));
		goalSelector.addGoal(10, new LookAtGoal(this, MobEntity.class, 8.0F));
		targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
		targetSelector.addGoal(2, new HurtByTargetGoal(this));
	}
	
	public static AttributeModifierMap.MutableAttribute getAttributes() {
        return MobEntity.func_233666_p_().createMutableAttribute(Attributes.MAX_HEALTH, 300).createMutableAttribute(Attributes.ARMOR, 5).createMutableAttribute(Attributes.FOLLOW_RANGE, 64);
    }
	
	public static void spawn(PlayerEntity player, World world) {
		Random rand = player.getRNG();
		DameFortunaEntity dame = ModEntities.DAME_FORTUNA.create(world);
		dame.setLocationAndAngles(player.getPosX() + rand.nextInt(5) - 2, player.getPosY() + rand.nextInt(3) + 3, player.getPosZ() + rand.nextInt(5) - 2, rand.nextFloat() * 360 - 180, 0);
		dame.attackCooldown = 100;
		if (!player.abilities.isCreativeMode) dame.setAttackTarget(player);
		dame.addPotionEffect(new EffectInstance(Effects.RESISTANCE, 100, 2));

		dame.onInitialSpawn((ServerWorld) world, world.getDifficultyForLocation(dame.getPosition()), SpawnReason.EVENT, null, null);
		world.addEntity(dame);
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
	
	private ProjectileLineEntity readyLine() {
		ProjectileLineEntity proj = new ProjectileLineEntity(world, this, 0, 0, 0);
		proj.setShooter(this);
		proj.setPosition(getPosX(), getPosYEye() + 1, getPosZ());
		return proj;
	}
	

	//Copied from Evoker
	private void spawnFangs(double posX, double posZ, double minY, double minZ, float rotationRad, int delay) {
		BlockPos blockpos = new BlockPos(posX, minZ, posZ);
		boolean success = false;
		double d0 = 0;

		do {
			BlockPos blockpos1 = blockpos.down();
			BlockState blockstate = world.getBlockState(blockpos1);
			if (blockstate.isSolidSide(world, blockpos1, Direction.UP)) {
				if (!world.isAirBlock(blockpos)) {
					BlockState blockstate1 = world.getBlockState(blockpos);
					VoxelShape voxelshape = blockstate1.getCollisionShape(world, blockpos);
					if (!voxelshape.isEmpty()) d0 = voxelshape.getEnd(Direction.Axis.Y);
				}

				success = true;
				break;
			}

			blockpos = blockpos.down();
		}
		while (blockpos.getY() >= MathHelper.floor(minY) - 1);

		if (success) world.addEntity(new EvokerFangsEntity(world, posX, blockpos.getY() + d0, posZ, rotationRad, delay, this));

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
	//It's horribly ad hoc but it'll do "for now"
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
			target = dame.getAttackTarget();
			dame.setAttack(SMALL_ATTACK);
			chosenAttack = dame.rand.nextInt(4);
			attackDelay = 20;
			attackRemaining = getAttackCount();
		}

		//Horrible horrible ad hoc n°1
		private int getAttackCount() {
			switch (chosenAttack) {
				case 0:
					return 16;
				case 2:
					return 8;
				case 3:
					return 4;
				default:
					return 3;
			}
		}

		@Override
		public void tick() {
			dame.attackCooldown = 2;
			attackDelay--;
			if (attackDelay <= 0) {
				attackRemaining--;
				performAttack();
				if (attackRemaining <= 0) resetTask();
			}
		}

		//Horrible horrible ad hoc n°2
		private void performAttack() {
			switch (chosenAttack) {
				default:
				case 0:
					//Circular lines
					attackDelay = 3;
					float angle = MathHelper.wrapDegrees(attackRemaining * 45F) * ((float)Math.PI / 180F);
					ProjectileLineEntity proj = dame.readyLine();
					proj.setUpTowards(9, dame.getPosX() + MathHelper.sin(angle) * 4, dame.getPosY() + 6, dame.getPosZ() + MathHelper.cos(angle) * 4, target.getPosX(), target.getPosY(), target.getPosZ());
					dame.world.addEntity(proj);
					//TODO proper sound event
					dame.playSound(SoundEvents.ENTITY_SHULKER_SHOOT, 2.0F, (dame.rand.nextFloat() - dame.rand.nextFloat()) * 0.2F + 1.0F);
					break;
				case 1:
					//Evoker lines
					attackDelay = 20;
					double minY = Math.min(target.getPosY(), dame.getPosY());
					double maxY = Math.max(target.getPosY(), dame.getPosY()) + 1;
					angle = (float) MathHelper.atan2(target.getPosZ() - dame.getPosZ(), target.getPosX() - dame.getPosX());
					for (int i = 0; i < 16; ++i) {
						double dist = 1.25 * (i + 1);
						dame.spawnFangs(dame.getPosX() + MathHelper.cos(angle) * dist, dame.getPosZ() + MathHelper.sin(angle) * dist, minY, maxY, angle, i);
					}
					break;
				case 2:
					//Harvester style Evoker jaws
					attackDelay = 10;
					minY = Math.min(target.getPosY(), dame.getPosY());
					maxY = Math.max(target.getPosY(), dame.getPosY()) + 1;
					angle = (float) MathHelper.atan2(target.getPosZ() - dame.getPosZ(), target.getPosX() - dame.getPosX());
					dame.spawnFangs(target.getPosX(), target.getPosZ(), minY, maxY, angle, 0);
					break;
				case 3:
					//Grid above
					attackDelay = 20;
					for (int x = -3; x <= 3; x++) {
						for (int z = -3; z <= 3; z++) {
							if ((x + z + 6) % 2 != attackRemaining % 2) continue;
							proj = dame.readyLine();
							proj.setUp(15, 0, -1, 0, target.getPosX() + x * 1.5, target.getPosY() + 7, target.getPosZ() + z * 1.5);
							dame.world.addEntity(proj);
						}
					}
					//TODO proper sound event
					dame.playSound(SoundEvents.ENTITY_SHULKER_SHOOT, 2.0F, (dame.rand.nextFloat() - dame.rand.nextFloat()) * 0.2F + 1.0F);
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
	
	//Rotate around attack target
	private static class MoveAroundTarget extends Goal {
		private MobEntity mob;

		public MoveAroundTarget(MobEntity mob) {
			setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
			this.mob = mob;
		}

		@Override
		public boolean shouldExecute() {
			return mob.getAttackTarget() != null && !mob.getMoveHelper().isUpdating();
		}

		@Override
		public void startExecuting() {			
			LivingEntity target = mob.getAttackTarget();
			Random rand = mob.getRNG();
			float angle = (rand.nextInt(6) + 1) * 10f;
			if (rand.nextBoolean()) angle *= -1;
			Vector3d offset = new Vector3d(mob.getPosX() - target.getPosX(), 0, mob.getPosZ() - target.getPosZ()).normalize().rotateYaw(angle);
			double distance = rand.nextDouble() * 2 + 4;

			mob.getMoveHelper().setMoveTo(
					target.getPosX() + offset.x * distance, 
					target.getPosY() + 2 + rand.nextDouble() * 2, 
					target.getPosZ() + offset.z * distance,
					1);
		}

		@Override
		public boolean shouldContinueExecuting() {
			return false;
		}

	}

}
