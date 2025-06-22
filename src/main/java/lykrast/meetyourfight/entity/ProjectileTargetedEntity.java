package lykrast.meetyourfight.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import lykrast.meetyourfight.config.MYFConfigValues;
import lykrast.meetyourfight.registry.MYFEntities;
import lykrast.meetyourfight.registry.MYFSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ProjectileTargetedEntity extends ProjectileBossAbstract {
	//Projectile goes to a point, waits, then goes at player's current location
	//For now just Fortuna's chips
	private static final EntityDataAccessor<Byte> COLOR = SynchedEntityData.defineId(ProjectileTargetedEntity.class, EntityDataSerializers.BYTE);

	private double startX, startY, startZ, speed, angleOffset;
	private int setupTime;
	@Nullable
	private Entity finalTarget;
	@Nullable
	private UUID targetId;

	public ProjectileTargetedEntity(EntityType<? extends ProjectileTargetedEntity> type, Level world) {
		super(type, world);
	}

	public ProjectileTargetedEntity(Level worldIn, LivingEntity shooter) {
		super(MYFEntities.PROJECTILE_TARGETED.get(), shooter, 0, 0, 0, worldIn);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(COLOR, (byte) 0);
	}

	public void setUp(int delay, int setupTime, Entity target, double speed, double startX, double startY, double startZ) {
		setUp(delay, setupTime, target, speed, startX, startY, startZ, 0);
	}

	public void setUp(int delay, int setupTime, Entity target, double speed, double startX, double startY, double startZ, double angleOffset) {
		fired = false;
		timer = delay;
		this.setupTime = setupTime;
		finalTarget = target;
		this.speed = speed;
		this.startX = startX;
		this.startY = startY;
		this.startZ = startZ;
		this.angleOffset = angleOffset;
	}

	@Override
	protected float getDamage(LivingEntity shooter, Entity hit) {
		return (float) (DameFortunaEntity.DMG * MYFConfigValues.FORTUNA_DMG_MULT);
	}

	@Override
	protected void projTick() {
		//Copied from shulker bullets
		if (finalTarget == null && targetId != null) {
			finalTarget = ((ServerLevel) level()).getEntity(targetId);
			if (finalTarget == null) targetId = null;
		}

		double x = getX();
		double y = getY();
		double z = getZ();

		if (timer <= 0) {
			//Disappear if about to be fired without a target
			if (fired || finalTarget == null) remove(RemovalReason.KILLED);
			else {
				fired = true;
				timer = 30;
				setDeltaMovement(new Vec3(finalTarget.getX() - x, finalTarget.getEyeY() - y, finalTarget.getZ() - z).normalize().scale(speed).yRot((float)angleOffset * Mth.DEG_TO_RAD));
				playSound(MYFSounds.dameFortunaChipsFire.get(), 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
			}
		}

		if (!fired && setupTime > 0) {
			setupTime--;
			if (setupTime == 0) setDeltaMovement(0,0,0);
			else setDeltaMovement(new Vec3(startX - x, startY - y, startZ - z).scale(1.0 / setupTime));
		}
	}

	public void setVariant(int variant) {
		entityData.set(COLOR, (byte) variant);
	}

	public int getVariant() {
		return entityData.get(COLOR);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putDouble("speed", speed);
		compound.putDouble("SX", startX);
		compound.putDouble("SY", startY);
		compound.putDouble("SZ", startZ);
		compound.putDouble("angle", angleOffset);
		compound.putInt("Variant", getVariant());
		compound.putInt("setuptime", setupTime);
		if (finalTarget != null) compound.putUUID("Target", finalTarget.getUUID());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		speed = compound.getDouble("speed");
		startX = compound.getDouble("SX");
		startY = compound.getDouble("SY");
		startZ = compound.getDouble("SZ");
		angleOffset = compound.getDouble("angle");
		setupTime = compound.getInt("setuptime");
		setVariant(compound.getInt("Variant"));
		if (compound.hasUUID("Target")) targetId = compound.getUUID("Target");
	}

}
