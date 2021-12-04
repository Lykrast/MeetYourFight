package lykrast.meetyourfight.entity;

import javax.annotation.Nullable;

import lykrast.meetyourfight.registry.ModEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class SwampMineEntity extends Entity {
	//Mostly copied from TNT
	private LivingEntity bomber;
	public int fuse = 200;

	public SwampMineEntity(EntityType<? extends SwampMineEntity> entityTypeIn, World worldIn) {
		super(entityTypeIn, worldIn);
		blocksBuilding = true;
	}

	public SwampMineEntity(World worldIn, double x, double y, double z, @Nullable LivingEntity igniter) {
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
	protected boolean isMovementNoisy() {
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isPickable() {
		return !this.removed;
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
			remove();
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
		level.explode(bomber != null ? bomber : this, getX(), getY(0.0625D), getZ(), 2, Explosion.Mode.NONE);
	}

	@Override
	protected void defineSynchedData() {
	}

	@Override
	protected void addAdditionalSaveData(CompoundNBT compound) {
		compound.putShort("Fuse", (short) fuse);
	}

	@Override
	protected void readAdditionalSaveData(CompoundNBT compound) {
		fuse = compound.getShort("Fuse");
	}

	@Override
	protected float getEyeHeight(Pose poseIn, EntitySize sizeIn) {
		return 0.15F;
	}

	@Override
	public IPacket<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

}
