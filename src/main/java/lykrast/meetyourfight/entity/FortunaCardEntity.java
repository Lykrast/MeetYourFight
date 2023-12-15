package lykrast.meetyourfight.entity;

import javax.annotation.Nullable;

import lykrast.meetyourfight.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class FortunaCardEntity extends Entity {
	//Card for the shuffle attack
	private LivingEntity bomber;
	private static final EntityDataAccessor<Byte> VARIANT = SynchedEntityData.defineId(FortunaCardEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Byte> ANIMATION = SynchedEntityData.defineId(FortunaCardEntity.class, EntityDataSerializers.BYTE);
	private int phase, timer = 20, hideTime;
	private boolean correct;
	private static final int PHASE_START = 0, PHASE_SPIN = 1, PHASE_GOTODEST = 2, PHASE_ACTIVE = 3, PHASE_REVEAL = 4;
	private static final int SHOW_TIME = 5*20, SPIN_TIME = 5*20;
	//Stuff for movement
	private double spinX, spinY, spinZ, destX, destY, destZ;
	private int spinOffset;
	private static final Vec3 SPINVEC = new Vec3(3, 0, 0);
	//Animation
	public int clientAnim, animTimer;
	public static final int ANIM_NOTHERE = 0, ANIM_APPEAR = 1, ANIM_IDLE_SHOW = 2, ANIM_HIDE = 3, ANIM_IDLE_HIDDEN = 4, ANIM_REVEAL = 5;
	public static final int ANIM_APPEAR_DUR = 10, ANIM_HIDE_DUR = 10;

	public FortunaCardEntity(EntityType<? extends FortunaCardEntity> entityTypeIn, Level worldIn) {
		super(entityTypeIn, worldIn);
	}

	public FortunaCardEntity(Level worldIn, double x, double y, double z, @Nullable LivingEntity igniter) {
		this(ModEntities.FORTUNA_CARD.get(), worldIn);
		setPos(x, y, z);
		xo = x;
		yo = y;
		zo = z;
		bomber = igniter;
	}

	//You know what I'm sure some code analyzer would yell at me for putting that many arguments there
	//Anyway set the variant, whether it's a correct card or not, delay before it's revealed,
	//center of the shuffle spin, angle offset for the spin, and final destination after shuffling
	public void setup(int variant, boolean correct, int preflipTime, double spinX, double spinY, double spinZ, int spinOffset, double destX, double destY, double destZ) {
		setVariant(variant);
		this.correct = correct;
		phase = PHASE_START;
		timer = SHOW_TIME;
		hideTime = preflipTime;
		this.spinX = spinX;
		this.spinY = spinY;
		this.spinZ = spinZ;
		this.spinOffset = spinOffset;
		this.destX = destX;
		this.destY = destY;
		this.destZ = destZ;
		setAnimation(ANIM_NOTHERE);
	}

	@Override
	protected void defineSynchedData() {
		entityData.define(VARIANT, (byte)0);
		entityData.define(ANIMATION, (byte)0);
	}

	public void setVariant(int value) {
		entityData.set(VARIANT, (byte)value);
	}

	public int getVariant() {
		return entityData.get(VARIANT);
	}

	public void setAnimation(int value) {
		entityData.set(ANIMATION, (byte)value);
	}

	public int getAnimation() {
		return entityData.get(ANIMATION);
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public void tick() {
		if (!level.isClientSide) {
			timer--;
			if (timer <= 0) {
				switch (phase) {
					case PHASE_START:
						phase = PHASE_SPIN;
						timer = SPIN_TIME;
						break;
					case PHASE_SPIN:
						setAnimation(ANIM_IDLE_HIDDEN);
						phase = PHASE_GOTODEST;
						timer = 40;
						setDeltaMovement(0,0,0);
						break;
					case PHASE_GOTODEST:
						phase = PHASE_ACTIVE;
						//timer = 10*20;
						timer = 20;
						setDeltaMovement(0,0,0);
						break;
					case PHASE_ACTIVE:
					case PHASE_REVEAL:
						remove(RemovalReason.KILLED);
						break;
				}
			}
			if (phase == PHASE_START) {
				if (timer == SHOW_TIME - hideTime) setAnimation(ANIM_APPEAR);
				else if (timer == SHOW_TIME - hideTime - ANIM_APPEAR_DUR) setAnimation(ANIM_IDLE_SHOW);
				else if (timer == 20) setAnimation(ANIM_HIDE);
				else if (timer == 20 - ANIM_HIDE_DUR) setAnimation(ANIM_IDLE_HIDDEN);
			}
			//Phases that have movement
			if (phase == PHASE_SPIN) {
				//18° per tick = 360° per second
				Vec3 offset = SPINVEC.yRot(((timer*18+spinOffset) % 360) * Mth.DEG_TO_RAD);
				double tx = spinX + offset.x;
				double tz = spinZ + offset.z;
				//Accelerate to spin at the start
				if (SPIN_TIME - timer < 20) {
					Vec3 speed = new Vec3(tx - getX(), spinY - getY(), tz - getZ());
					double len = speed.lengthSqr();
					double maxSpeed = (SPIN_TIME - timer)*0.1;
					if (len > maxSpeed*maxSpeed) speed = speed.normalize().scale(maxSpeed);
					setDeltaMovement(speed);
				}
				else setDeltaMovement(tx - getX(), spinY - getY(), tz - getZ());
			}
			if (phase == PHASE_GOTODEST) {
				//delay dependend on the spin offset, so like a 180 offset takes 10 ticks more before going to the dest
				int timeOffset = spinOffset / 18 + 1;
				Vec3 speed = new Vec3(destX - getX(), destY - getY(), destZ - getZ());
				if (timer <= timeOffset) {
					setDeltaMovement(speed);
				}
				else if (timer <= timeOffset + 10) {
					setDeltaMovement(speed.scale(1.0/(timer - timeOffset)));
				}
			}
		}
		move(MoverType.SELF, getDeltaMovement());
		if (level.isClientSide) updateClientAnimation();
	}
	
	private void updateClientAnimation() {
		if (clientAnim != getAnimation()) {
			clientAnim = getAnimation();
			switch (clientAnim) {
				case ANIM_APPEAR:
					animTimer = ANIM_APPEAR_DUR;
					break;
				case ANIM_HIDE:
					animTimer = ANIM_HIDE_DUR;
					break;
				default:
					animTimer = 20;
					break;
			}
		}
		else animTimer--;
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compound) {
		compound.putByte("Variant", (byte)getVariant());
		compound.putInt("Phase", phase);
		compound.putInt("Timer", timer);
		compound.putInt("HTime", hideTime);
		compound.putBoolean("Correct", correct);
		compound.putDouble("spinX", spinX);
		compound.putDouble("spinY", spinY);
		compound.putDouble("spinZ", spinZ);
		compound.putInt("spinOff", spinOffset);
		compound.putDouble("destX", destX);
		compound.putDouble("destY", destY);
		compound.putDouble("destZ", destZ);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compound) {
		setVariant(compound.getByte("Variant"));
		phase = compound.getInt("Phase");
		timer = compound.getInt("Timer");
		hideTime = compound.getInt("HTime");
		correct = compound.getBoolean("Correct");
		spinX = compound.getDouble("spinX");
		spinY = compound.getDouble("spinY");
		spinZ = compound.getDouble("spinZ");
		spinOffset = compound.getInt("spinOff");
		destX = compound.getDouble("destX");
		destY = compound.getDouble("destY");
		destZ = compound.getDouble("destZ");
	}

	@Override
	protected float getEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
		return 1f;
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

}
