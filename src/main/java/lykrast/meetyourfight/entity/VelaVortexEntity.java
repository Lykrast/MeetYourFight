package lykrast.meetyourfight.entity;

import javax.annotation.Nonnull;

import lykrast.meetyourfight.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class VelaVortexEntity extends AbstractHurtingProjectile {
	public static final int ACTIVATION = 2*20, LIFESPAN = 10*20;
	private double dirX, dirY, dirZ;
	private int timer;
	private boolean fired;
	
	public VelaVortexEntity(EntityType<? extends VelaVortexEntity> type, Level world) {
		super(type, world);
		timer = ACTIVATION;
	}

	public VelaVortexEntity(Level worldIn, LivingEntity shooter) {
		super(ModEntities.VELA_VORTEX, shooter, 0, 0, 0, worldIn);
		timer = ACTIVATION;
	}

	private void onHit(Entity hit) {
		Entity shooter = this.getOwner();
		boolean wasHit;
		if (shooter instanceof LivingEntity) {
			LivingEntity livingentity = (LivingEntity) shooter;
			wasHit = hit.hurt(DamageSource.indirectMobAttack(this, livingentity).setProjectile().setMagic(), 5);
			if (wasHit && hit.isAlive()) doEnchantDamageEffects(livingentity, hit);
		}
		else {
			wasHit = hit.hurt(DamageSource.MAGIC, 5);
		}
		if (wasHit && hit instanceof LivingEntity) {
			LivingEntity livinghit = (LivingEntity) hit;
			livinghit.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60));
			livinghit.setDeltaMovement(livinghit.getDeltaMovement().add(0, 0.2, 0));
		}
	}
	
	public void setUp(double dirX, double dirY, double dirZ) {
		fired = false;
		this.dirX = dirX;
		this.dirY = dirY;
		this.dirZ = dirZ;
	}
	
	public void setUpTowards(double tX, double tY, double tZ, double speed) {
		Vec3 direction = new Vec3(tX - getX(), tY - getY(), tZ - getZ()).normalize().scale(speed);
		setUp(direction.x, direction.y, direction.z);
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
					timer = LIFESPAN;
				}
			}
			else {
				setDeltaMovement(dirX, dirY, dirZ);
			}
		}

		// Started from copy of the above tick
		Entity shooter = this.getOwner();
		if (level.isClientSide || (shooter == null || !shooter.isRemoved()) && level.hasChunkAt(blockPosition())) {
			superTick();
			
			//Well the issue here is that diagonal movement will feel different, but I'll see how it goes in game
			if (!level.isClientSide && fired) {
				for (Entity e : level.getEntities(this, getBoundingBox().expandTowards(getDeltaMovement()).deflate(0.5), this::canHitEntity)) {
					onHit(e);
				}
			}

			checkInsideBlocks();
			Vec3 vector3d = getDeltaMovement();
			double d0 = getX() + vector3d.x;
			double d1 = getY() + vector3d.y;
			double d2 = getZ() + vector3d.z;
			//level.addParticle(ParticleTypes.DRIPPING_WATER, d0 + (random.nextDouble() - 0.5)*3, d1 + 0.5D, d2 + (random.nextDouble() - 0.5)*3, 0.0D, 0.0D, 0.0D);
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
		compound.putInt("Timer", timer);
		compound.putBoolean("Fired", fired);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		dirX = compound.getDouble("DX");
		dirY = compound.getDouble("DY");
		dirZ = compound.getDouble("DZ");
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
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

}
