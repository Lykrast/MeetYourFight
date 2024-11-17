package lykrast.meetyourfight.entity;

import javax.annotation.Nullable;

import lykrast.meetyourfight.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class FortunaBombEntity extends Entity {
	//Dice that explodes into the 8 directions
	private LivingEntity bomber;
	//copied from tnt to display the fuse
	private static final EntityDataAccessor<Integer> DATA_FUSE_ID = SynchedEntityData.defineId(FortunaBombEntity.class, EntityDataSerializers.INT);
	private int moveFuse;

	private static final double GRAVITY = -0.08;

	public FortunaBombEntity(EntityType<? extends FortunaBombEntity> entityTypeIn, Level worldIn) {
		super(entityTypeIn, worldIn);
	}

	public FortunaBombEntity(Level worldIn, double x, double y, double z, @Nullable LivingEntity igniter) {
		this(ModEntities.FORTUNA_BOMB.get(), worldIn);
		this.setPos(x, y, z);
		xo = x;
		yo = y;
		zo = z;
		bomber = igniter;
	}

	public void setup(int fuse, int moveFuse, double tx, double ty, double tz) {
		setFuse(fuse);
		this.moveFuse = moveFuse;
		//tick n the speed will have n gravity added to the velocity
		//because we do moveFuse movement and gravity is after movement,
		//that's a total of n(n+1)/2 of gravity added
		//so we add 1/moveFuse of that to the initial y velocity to perfectly compensate
		//giving us a total of (n+1)/2
		double compensation = (-(moveFuse + 1) / 2.0) * GRAVITY;
		double scale = 1.0 / moveFuse;
		setDeltaMovement((tx - getX()) * scale, (ty - getY()) * scale + compensation, (tz - getZ()) * scale);
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(DATA_FUSE_ID, 80);
	}

	public void setFuse(int value) {
		this.entityData.set(DATA_FUSE_ID, value);
	}

	public int getFuse() {
		return this.entityData.get(DATA_FUSE_ID);
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@SuppressWarnings("resource")
	@Override
	public void tick() {
		if (moveFuse > 0) {
			setDeltaMovement(getDeltaMovement().add(0, GRAVITY, 0));
		}
		//just in case to not mess things up still move even if we should be stationary
		move(MoverType.SELF, getDeltaMovement());
		if (moveFuse > 0) {
			moveFuse--;
			if (moveFuse == 0) setDeltaMovement(0, 0, 0);
		}

		int remaining = getFuse() - 1;
		setFuse(remaining);
		//if (remaining == 10) playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
		if (remaining <= 0) {
			remove(RemovalReason.KILLED);
			playSound(SoundEvents.GENERIC_EXPLODE, 1.0F, 0.5F);
			if (!level().isClientSide) explode();
		}
		else {
			updateInWaterStateAndDoFluidPushing();
			if (level().isClientSide) {
				level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5D, this.getZ(), 0.0D, 0.0D, 0.0D);
			}
		}

	}

	//For diagonals
	private static final double HALF_SQRT2 = Math.sqrt(2) / 2;

	private void explode() {
		if (bomber == null) return;
		projectile(1, 0);
		projectile(-1, 0);
		projectile(0, 1);
		projectile(0, -1);
		projectile(HALF_SQRT2, HALF_SQRT2);
		projectile(-HALF_SQRT2, HALF_SQRT2);
		projectile(HALF_SQRT2, -HALF_SQRT2);
		projectile(-HALF_SQRT2, -HALF_SQRT2);
	}

	private void projectile(double dx, double dz) {
		ProjectileLineEntity proj = new ProjectileLineEntity(level(), bomber);
		proj.setOwner(bomber);
		proj.setPos(position());
		proj.setVariant(ProjectileLineEntity.VAR_DAME_FORTUNA);
		proj.setUp(1, dx, 0, dz, getX() + dx * 0.1, getY(), getZ() + dz * 0.1);
		level().addFreshEntity(proj);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compound) {
		compound.putShort("Fuse", (short) getFuse());
		compound.putShort("MFuse", (short) moveFuse);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compound) {
		setFuse(compound.getShort("Fuse"));
		moveFuse = compound.getShort("MFuse");
	}

	@Override
	protected float getEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
		return 0.15F;
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

}
