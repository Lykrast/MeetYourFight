package lykrast.meetyourfight.entity;

import javax.annotation.Nonnull;

import lykrast.meetyourfight.registry.ModEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class ProjectileLineEntity extends DamagingProjectileEntity {
	//Projectile goes to a point over a set duration, then activates and accelerates in a given straight line
	private static final DataParameter<Integer> PROJECTILE_VARIANT = EntityDataManager.createKey(ProjectileLineEntity.class, DataSerializers.VARINT);
	public static final int VAR_BELLRINGER = 0, VAR_DAME_FORTUNA = 1;
	
	private double dirX, dirY, dirZ;
	private double startX, startY, startZ;
	private int timer;
	private boolean fired;
	
	public ProjectileLineEntity(EntityType<? extends ProjectileLineEntity> type, World world) {
		super(type, world);
	}

	public ProjectileLineEntity(World worldIn, LivingEntity shooter, double accelX, double accelY, double accelZ) {
		super(ModEntities.PROJECTILE_LINE, shooter, accelX, accelY, accelZ, worldIn);
	}
	
	@Override
	protected void registerData() {
		super.registerData();
		dataManager.register(PROJECTILE_VARIANT, 0);
	}

	@Override
	protected void onEntityHit(EntityRayTraceResult raytrace) {
		super.onEntityHit(raytrace);
		if (!world.isRemote && fired) {
			Entity hit = raytrace.getEntity();
			Entity shooter = this.func_234616_v_();
			boolean wasHit;
			if (shooter instanceof LivingEntity) {
				LivingEntity livingentity = (LivingEntity) shooter;
				wasHit = hit.attackEntityFrom(DamageSource.causeIndirectDamage(this, livingentity).setProjectile().setMagicDamage(), 8.0F);
				if (wasHit) {
					if (hit.isAlive()) applyEnchantments(livingentity, hit);
				}
				remove();
			}
			else {
				wasHit = hit.attackEntityFrom(DamageSource.MAGIC, 5.0F);
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
		Vector3d vec = new Vector3d(endX - startX, endY - startY, endZ - startZ).normalize().scale(speed);
		setUp(delay, vec.x, vec.y, vec.z, startX, startY, startZ);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void tick() {
		if (!world.isRemote) {
			timer--;
			if (timer <= 0) {
				if (fired) remove();
				else {
					fired = true;
					setMotion(new Vector3d(0, 0, 0));
					timer = 30;
				}
			}
			Vector3d motion = getMotion();
			double d0 = getPosX();
			double d1 = getPosY();
			double d2 = getPosZ();

			if (fired) {
				if (motion.lengthSquared() <= 16) setMotion(motion.add(dirX * 0.1, dirY * 0.1, dirZ * 0.1));
			}
			else {
				setMotion(new Vector3d(startX - d0, startY - d1, startZ - d2).scale(1.0 / timer));
			}
		}

		// Started from copy of the above tick
		Entity shooter = this.func_234616_v_();
		if (world.isRemote || (shooter == null || !shooter.removed) && world.isBlockLoaded(getPosition())) {
			superTick();
			RayTraceResult raytraceresult = ProjectileHelper.func_234618_a_(this, this::func_230298_a_);
			if (raytraceresult.getType() != RayTraceResult.Type.MISS && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
				onImpact(raytraceresult);
			}

			doBlockCollisions();
			Vector3d vector3d = getMotion();
			double d0 = getPosX() + vector3d.x;
			double d1 = getPosY() + vector3d.y;
			double d2 = getPosZ() + vector3d.z;
			ProjectileHelper.rotateTowardsMovement(this, 0.2F);
			world.addParticle(ParticleTypes.END_ROD, d0, d1 + 0.5D, d2, 0.0D, 0.0D, 0.0D);
			setPosition(d0, d1, d2);
		}
		else {
			remove();
		}
	}
	
	//Inlined tick() from stuff above because I need to bypass quite a bit
	private void superTick() {
	      if (!world.isRemote) {
	         setFlag(6, isGlowing());
	      }
	      baseTick();
	}

	public void setVariant(int variant) {
		dataManager.set(PROJECTILE_VARIANT, variant);
	}

	public int getVariant() {
		return dataManager.get(PROJECTILE_VARIANT);
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
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
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
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
	public SoundCategory getSoundCategory() {
		return SoundCategory.HOSTILE;
	}

	@Override
	public boolean canBeCollidedWith() {
		return false;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		return false;
	}

	@Override
	protected boolean isFireballFiery() {
		return false;
	}

	@Nonnull
	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

}
