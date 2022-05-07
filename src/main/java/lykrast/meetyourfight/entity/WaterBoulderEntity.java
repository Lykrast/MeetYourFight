package lykrast.meetyourfight.entity;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lykrast.meetyourfight.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
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

public class WaterBoulderEntity extends AbstractHurtingProjectile {	
	private double dirX, dirY, dirZ;
	private double offsetX, offsetY, offsetZ;
	private int timer;
	private boolean fired;
	@Nullable
	private Entity target;
	@Nullable
	private UUID targetId;
	
	public WaterBoulderEntity(EntityType<? extends WaterBoulderEntity> type, Level world) {
		super(type, world);
	}

	public WaterBoulderEntity(Level worldIn, LivingEntity shooter, Entity target) {
		super(ModEntities.WATER_BOULDER, shooter, 0, 0, 0, worldIn);
		this.target = target;
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
				wasHit = hit.hurt(DamageSource.indirectMobAttack(this, livingentity).setProjectile().setMagic(), 40);
				if (wasHit) {
					if (hit.isAlive()) doEnchantDamageEffects(livingentity, hit);
				}
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
		this.offsetX = startX;
		this.offsetY = startY;
		this.offsetZ = startZ;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void tick() {
		if (!level.isClientSide) {
			if (target == null && targetId != null) {
				target = ((ServerLevel)level).getEntity(targetId);
				if (target == null) targetId = null;
			}
			timer--;
			if (timer <= 0) {
				if (fired) remove(RemovalReason.KILLED);
				else {
					fired = true;
					timer = 40;
				}
			}

			if (fired) {
				setDeltaMovement(dirX, dirY, dirZ);
			}
			else {
				if (target != null && !target.isRemoved()) {
					double scale = 1;
					if (timer > 40) scale = 1.0/(timer - 40);
					setDeltaMovement(new Vec3(
							target.getX() + offsetX - getX(), 
							target.getY() + offsetY - getY(), 
							target.getZ() + offsetZ - getZ())
							.scale(scale));
				}
				else {
					setDeltaMovement(0, 0, 0);
				}
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
			level.addParticle(ParticleTypes.DRIPPING_WATER, d0 + (random.nextDouble() - 0.5)*3, d1 + 0.5D, d2 + (random.nextDouble() - 0.5)*3, 0.0D, 0.0D, 0.0D);
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

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putDouble("DX", dirX);
		compound.putDouble("DY", dirY);
		compound.putDouble("DZ", dirZ);
		compound.putDouble("SX", offsetX);
		compound.putDouble("SY", offsetY);
		compound.putDouble("SZ", offsetZ);
		compound.putInt("Timer", timer);
		compound.putBoolean("Fired", fired);
		if (target != null) compound.putUUID("Target", target.getUUID());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		dirX = compound.getDouble("DX");
		dirY = compound.getDouble("DY");
		dirZ = compound.getDouble("DZ");
		offsetX = compound.getDouble("SX");
		offsetY = compound.getDouble("SY");
		offsetZ = compound.getDouble("SZ");
		timer = compound.getInt("Timer");
		fired = compound.getBoolean("Fired");
		if (compound.hasUUID("Target")) targetId = compound.getUUID("Target");
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
