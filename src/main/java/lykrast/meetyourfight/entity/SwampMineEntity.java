package lykrast.meetyourfight.entity;

import javax.annotation.Nullable;

import lykrast.meetyourfight.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class SwampMineEntity extends Entity {
	//Mostly copied from TNT
	private LivingEntity bomber;
	public int fuse = 200;

	public SwampMineEntity(EntityType<? extends SwampMineEntity> entityTypeIn, Level worldIn) {
		super(entityTypeIn, worldIn);
		blocksBuilding = true;
	}

	public SwampMineEntity(Level worldIn, double x, double y, double z, @Nullable LivingEntity igniter) {
		this(ModEntities.SWAMP_MINE, worldIn);
		this.setPos(x, y, z);
		double angle = worldIn.random.nextDouble() * Math.PI * 2;
		setDeltaMovement(-Math.sin(angle) * 0.06, 0.05, -Math.cos(angle) * 0.06);
		fuse = 200;
		xo = x;
		yo = y;
		zo = z;
		bomber = igniter;
	}

	@Override
	public boolean isPickable() {
		return !this.isRemoved();
	}

	@Override
	public void tick() {
		if (!isNoGravity()) {
			setDeltaMovement(getDeltaMovement().add(0.0D, -0.04D, 0.0D));
		}

		move(MoverType.SELF, getDeltaMovement());
		//if (getMotion().y < 0) setMotion(getMotion().mul(0.98, 0.8, 0.98));
		//else setMotion(getMotion().scale(0.98));
		setDeltaMovement(getDeltaMovement().scale(0.98));
		
		if (onGround) fuse = 0;
		else --fuse;
		if (fuse <= 0) {
			remove(RemovalReason.KILLED);
			if (!level.isClientSide) explode();
		}
		else {
			updateInWaterStateAndDoFluidPushing();
			if (level.isClientSide) {
				level.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5D, this.getZ(), 0.0D, 0.0D, 0.0D);
			}
		}

	}

	protected void explode() {
		level.explode(bomber != null ? bomber : this, getX(), getY(0.0625D), getZ(), 3, Explosion.BlockInteraction.NONE);
	}

	@Override
	protected void defineSynchedData() {
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compound) {
		compound.putShort("Fuse", (short) fuse);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compound) {
		fuse = compound.getShort("Fuse");
	}

	@Override
	protected float getEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
		return 0.15F;
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

}
