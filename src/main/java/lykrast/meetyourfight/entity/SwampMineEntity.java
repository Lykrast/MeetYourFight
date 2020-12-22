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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class SwampMineEntity extends Entity {
	//Mostly copied from TNT
	private LivingEntity bomber;
	public int fuse = 200;

	public SwampMineEntity(EntityType<? extends SwampMineEntity> entityTypeIn, World worldIn) {
		super(entityTypeIn, worldIn);
		preventEntitySpawning = true;
	}

	public SwampMineEntity(World worldIn, double x, double y, double z, @Nullable LivingEntity igniter) {
		this(ModEntities.SWAMP_MINE, worldIn);
		this.setPosition(x, y, z);
		double angle = worldIn.rand.nextDouble() * Math.PI * 2;
		setMotion(-Math.sin(angle) * 0.06, 0.2, -Math.cos(angle) * 0.06);
		//This one is ellpeck's idea
		if (igniter != null) {
			Vector3d motion = igniter.getMotion();
			setMotion(getMotion().add(motion.x * 0.5, 0, motion.z * 0.5));
		}
		fuse = 200;
		prevPosX = x;
		prevPosY = y;
		prevPosZ = z;
		bomber = igniter;
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean canBeCollidedWith() {
		return !this.removed;
	}

	@Override
	public void tick() {
		if (!hasNoGravity()) {
			setMotion(getMotion().add(0.0D, -0.04D, 0.0D));
		}

		move(MoverType.SELF, getMotion());
		//if (getMotion().y < 0) setMotion(getMotion().mul(0.98, 0.8, 0.98));
		//else setMotion(getMotion().scale(0.98));
		setMotion(getMotion().scale(0.98));
		
		if (onGround) fuse = 0;
		else --fuse;
		if (fuse <= 0) {
			remove();
			if (!world.isRemote) explode();
		}
		else {
			func_233566_aG_();
			if (world.isRemote) {
				world.addParticle(ParticleTypes.SMOKE, this.getPosX(), this.getPosY() + 0.5D, this.getPosZ(), 0.0D, 0.0D, 0.0D);
			}
		}

	}

	protected void explode() {
		world.createExplosion(bomber != null ? bomber : this, getPosX(), getPosYHeight(0.0625D), getPosZ(), 2, Explosion.Mode.NONE);
	}

	@Override
	protected void registerData() {
	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
		compound.putShort("Fuse", (short) fuse);
	}

	@Override
	protected void readAdditional(CompoundNBT compound) {
		fuse = compound.getShort("Fuse");
	}

	@Override
	protected float getEyeHeight(Pose poseIn, EntitySize sizeIn) {
		return 0.15F;
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

}
