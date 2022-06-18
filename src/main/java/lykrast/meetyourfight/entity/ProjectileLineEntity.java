package lykrast.meetyourfight.entity;

import javax.annotation.Nonnull;

import lykrast.meetyourfight.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class ProjectileLineEntity extends AbstractHurtingProjectile {
	//Projectile goes to a point over a set duration, then activates and accelerates in a given straight line
	private static final EntityDataAccessor<Integer> PROJECTILE_VARIANT = SynchedEntityData.defineId(ProjectileLineEntity.class, EntityDataSerializers.INT);
	public static final int VAR_BELLRINGER = 0, VAR_DAME_FORTUNA = 1;
	
	private double dirX, dirY, dirZ;
	private double startX, startY, startZ;
	private int timer;
	private boolean fired;
	
	public ProjectileLineEntity(EntityType<? extends ProjectileLineEntity> type, Level world) {
		super(type, world);
	}

	public ProjectileLineEntity(Level worldIn, LivingEntity shooter, double accelX, double accelY, double accelZ) {
		super(ModEntities.PROJECTILE_LINE.get(), shooter, accelX, accelY, accelZ, worldIn);
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(PROJECTILE_VARIANT, 0);
	}

	@Override
	protected void onHitEntity(EntityHitResult raytrace) {
		super.onHitEntity(raytrace);
		if (!level.isClientSide && fired) {
			Entity hit = raytrace.getEntity();
			Entity shooter = this.getOwner();
			boolean wasHit;
			if (shooter instanceof LivingEntity) {
				LivingEntity livingentity = (LivingEntity) shooter;
				wasHit = hit.hurt(DamageSource.indirectMobAttack(this, livingentity).setProjectile().setMagic(), 16);
				if (wasHit) {
					if (hit.isAlive()) doEnchantDamageEffects(livingentity, hit);
				}
				remove(RemovalReason.KILLED);
			}
			else {
				wasHit = hit.hurt(DamageSource.MAGIC, 5);
			}
		}
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

	@SuppressWarnings("deprecation")
	@Override
	public void tick() {
		if (!level.isClientSide) {
			timer--;
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

		// Started from copy of the above tick
		Entity shooter = this.getOwner();
		if (level.isClientSide || (shooter == null || !shooter.isRemoved()) && level.hasChunkAt(blockPosition())) {
			superTick();
			HitResult raytraceresult = ProjectileUtil.getHitResult(this, this::canHitEntity);
			if (raytraceresult.getType() != HitResult.Type.MISS && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
				onHit(raytraceresult);
			}

			checkInsideBlocks();
			Vec3 vector3d = getDeltaMovement();
			double d0 = getX() + vector3d.x;
			double d1 = getY() + vector3d.y;
			double d2 = getZ() + vector3d.z;
			ProjectileUtil.rotateTowardsMovement(this, 0.2F);
			level.addParticle(ParticleTypes.END_ROD, d0, d1 + 0.5D, d2, 0.0D, 0.0D, 0.0D);
			setPos(d0, d1, d2);
		}
		else {
			remove(RemovalReason.KILLED);
		}
	}
	
	//Inlined tick() from stuff above because I need to bypass quite a bit
	private void superTick() {
	      if (!level.isClientSide) {
	         setSharedFlag(6, isCurrentlyGlowing());
	      }
	      baseTick();
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
		compound.putInt("Timer", timer);
		compound.putBoolean("Fired", fired);
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
		timer = compound.getInt("Timer");
		fired = compound.getBoolean("Fired");
		setVariant(compound.getInt("Variant"));
	}

	@Override
	public SoundSource getSoundSource() {
		return SoundSource.HOSTILE;
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		return false;
	}

	@Override
	protected boolean shouldBurn() {
		return false;
	}

	@Nonnull
	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

}
