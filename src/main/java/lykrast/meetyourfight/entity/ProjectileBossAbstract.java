package lykrast.meetyourfight.entity;

import javax.annotation.Nonnull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
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

public abstract class ProjectileBossAbstract extends AbstractHurtingProjectile {
	/**
	 * Internal timer that ticks down every tick
	 */
	protected int timer;
	/**
	 * Projectile only hurts when true
	 */
	protected boolean fired;
	
	public ProjectileBossAbstract(EntityType<? extends ProjectileBossAbstract> type, Level world) {
		super(type, world);
	}

	public ProjectileBossAbstract(EntityType<? extends ProjectileBossAbstract> type, LivingEntity shooter, double accelX, double accelY, double accelZ, Level worldIn) {
		super(type, shooter, accelX, accelY, accelZ, worldIn);
	}
	
	protected abstract float getDamage(LivingEntity shooter, Entity hit);

	@SuppressWarnings("resource")
	@Override
	protected void onHitEntity(EntityHitResult raytrace) {
		super.onHitEntity(raytrace);
		if (!level().isClientSide && fired) {
			Entity hit = raytrace.getEntity();
			Entity shooter = this.getOwner();
			boolean wasHit;
			if (shooter instanceof LivingEntity) {
				LivingEntity livingentity = (LivingEntity) shooter;
				wasHit = hit.hurt(damageSources().mobProjectile(this, livingentity), getDamage(livingentity, hit));
				if (wasHit) {
					if (hit.isAlive()) doEnchantDamageEffects(livingentity, hit);
				}
				remove(RemovalReason.KILLED);
			}
			else {
				wasHit = hit.hurt(damageSources().magic(), 5);
			}
		}
	}
	
	/**
	 * Called server side after decreasing the timer, to make movement.
	 * Don't forget to remove when timer hits 0 (that's bad practice but it's just me).
	 */
	protected abstract void projTick();
	
	protected void particles(double targetx, double targety, double targetz) {}

	@SuppressWarnings({ "deprecation", "resource" })
	@Override
	public void tick() {
		if (!level().isClientSide) {
			timer--;
			projTick();
		}

		// Started from copy of the above tick
		Entity shooter = this.getOwner();
		if (level().isClientSide || (shooter == null || !shooter.isRemoved()) && level().hasChunkAt(blockPosition())) {
			superTick();
			HitResult raytraceresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
			if (raytraceresult.getType() != HitResult.Type.MISS && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
				onHit(raytraceresult);
			}

			checkInsideBlocks();
			Vec3 vector3d = getDeltaMovement();
			double d0 = getX() + vector3d.x;
			double d1 = getY() + vector3d.y;
			double d2 = getZ() + vector3d.z;
			ProjectileUtil.rotateTowardsMovement(this, 0.2F);
			particles(d0, d1, d2);
			setPos(d0, d1, d2);
		}
		else {
			remove(RemovalReason.KILLED);
		}
	}
	
	//Inlined tick() from stuff above because I need to bypass quite a bit
	@SuppressWarnings("resource")
	private void superTick() {
	      if (!level().isClientSide) {
	         setSharedFlag(6, isCurrentlyGlowing());
	      }
	      baseTick();
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putInt("Timer", timer);
		compound.putBoolean("Fired", fired);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		timer = compound.getInt("Timer");
		fired = compound.getBoolean("Fired");
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
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

}