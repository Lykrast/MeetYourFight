package lykrast.meetyourfight.entity;

import lykrast.meetyourfight.registry.MYFEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ProjectileLineEntity extends ProjectileBossAbstract {
	//Projectile goes to a point over a set duration, then activates and accelerates in a given straight line
	private static final EntityDataAccessor<Integer> PROJECTILE_VARIANT = SynchedEntityData.defineId(ProjectileLineEntity.class, EntityDataSerializers.INT);
	public static final int VAR_BELLRINGER = 0, VAR_DAME_FORTUNA = 1, VAR_ROSALYNE = 2;
	
	private double dirX, dirY, dirZ;
	private double startX, startY, startZ;
	
	public ProjectileLineEntity(EntityType<? extends ProjectileLineEntity> type, Level world) {
		super(type, world);
	}

	public ProjectileLineEntity(Level worldIn, LivingEntity shooter) {
		super(MYFEntities.PROJECTILE_LINE.get(), shooter, 0, 0, 0, worldIn);
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(PROJECTILE_VARIANT, 0);
	}
	
	public void setUp(int delay, double dirX, double dirY, double dirZ, double startX, double startY, double startZ) {
		fired = false;
		timer = delay;
		this.dirX = dirX;
		this.dirY = dirY;
		this.dirZ = dirZ;
		this.startX = startX;
		this.startY = startY;
		this.startZ = startZ;
	}
	
	public void setUpTowards(int delay, double startX, double startY, double startZ, double endX, double endY, double endZ, double speed) {
		Vec3 vec = new Vec3(endX - startX, endY - startY, endZ - startZ).normalize().scale(speed);
		setUp(delay, vec.x, vec.y, vec.z, startX, startY, startZ);
	}

	@Override
	protected float getDamage(LivingEntity shooter, Entity hit) {
		return getVariant() == VAR_BELLRINGER ? 10 : 16;
	}
	
	@Override
	protected void particles(double targetx, double targety, double targetz) {
		level().addParticle(ParticleTypes.END_ROD, targetx, targety + 0.5, targetz, 0, 0, 0);
	}

	@Override
	protected boolean canHitEntity(Entity target) {
		if (getVariant() == VAR_ROSALYNE && (target instanceof RoseSpiritEntity || target instanceof RosalyneEntity)) return false;
		return super.canHitEntity(target);
	}
	
	@Override
	protected void projTick() {
		if (timer <= 0) {
			if (fired) remove(RemovalReason.KILLED);
			else {
				fired = true;
				setDeltaMovement(new Vec3(0, 0, 0));
				timer = 30;
			}
		}
		Vec3 motion = getDeltaMovement();
		double d0 = getX();
		double d1 = getY();
		double d2 = getZ();

		if (fired) {
			if (motion.lengthSqr() <= 16) setDeltaMovement(motion.add(dirX * 0.1, dirY * 0.1, dirZ * 0.1));
		}
		else {
			setDeltaMovement(new Vec3(startX - d0, startY - d1, startZ - d2).scale(1.0 / timer));
		}
	}

	public void setVariant(int variant) {
		entityData.set(PROJECTILE_VARIANT, variant);
	}

	public int getVariant() {
		return entityData.get(PROJECTILE_VARIANT);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putDouble("DX", dirX);
		compound.putDouble("DY", dirY);
		compound.putDouble("DZ", dirZ);
		compound.putDouble("SX", startX);
		compound.putDouble("SY", startY);
		compound.putDouble("SZ", startZ);
		compound.putInt("Variant", getVariant());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		dirX = compound.getDouble("DX");
		dirY = compound.getDouble("DY");
		dirZ = compound.getDouble("DZ");
		startX = compound.getDouble("SX");
		startY = compound.getDouble("SY");
		startZ = compound.getDouble("SZ");
		setVariant(compound.getInt("Variant"));
	}

}
