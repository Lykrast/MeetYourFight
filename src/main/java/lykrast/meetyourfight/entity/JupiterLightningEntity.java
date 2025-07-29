package lykrast.meetyourfight.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import lykrast.meetyourfight.registry.MYFEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.Level;

public class JupiterLightningEntity extends Entity implements TraceableEntity {
	//Base copied from evoker fangs
	public static final int ATTACK_DURATION = 20;
	public static final int LIFE_OFFSET = 2;
	public static final int ATTACK_TRIGGER_TICKS = 14;
	private int warmupDelayTicks;
	private boolean sentSpikeEvent;
	private int lifeTicks = 22;
	private boolean clientSideAttackStarted;
	@Nullable
	private LivingEntity owner;
	@Nullable
	private UUID ownerUUID;
	//this from lightning
	public long seed;

	public JupiterLightningEntity(EntityType<? extends JupiterLightningEntity> type, Level level) {
		super(type, level);
	}

	public JupiterLightningEntity(Level level, double x, double y, double z, float rotation, int delay, LivingEntity owner) {
		this(MYFEntities.JUPITER_LIGHTNING.get(), level);
		warmupDelayTicks = delay;
		setOwner(owner);
		setYRot(rotation * Mth.RAD_TO_DEG);
		setPos(x, y, z);
		//this from lightning
		seed = random.nextLong();
	}

	@Override
	protected void defineSynchedData() {
	}

	public void setOwner(@Nullable LivingEntity owner) {
		this.owner = owner;
		ownerUUID = owner == null ? null : owner.getUUID();
	}

	@Override
	@Nullable
	public LivingEntity getOwner() {
		if (owner == null && ownerUUID != null && level() instanceof ServerLevel) {
			Entity entity = ((ServerLevel) level()).getEntity(ownerUUID);
			if (entity instanceof LivingEntity) owner = (LivingEntity) entity;
		}

		return owner;
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		warmupDelayTicks = tag.getInt("Warmup");
		if (tag.hasUUID("Owner")) ownerUUID = tag.getUUID("Owner");
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		tag.putInt("Warmup", warmupDelayTicks);
		if (ownerUUID != null) tag.putUUID("Owner", ownerUUID);
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) {
			if (clientSideAttackStarted) {
				--lifeTicks;
				seed = random.nextLong();
				if (lifeTicks == 14) {
					//TODO particles
					for (int i = 0; i < 12; ++i) {
						double d0 = this.getX() + (this.random.nextDouble() * 2.0D - 1.0D) * (double) this.getBbWidth() * 0.5D;
						double d1 = this.getY() + 0.05D + this.random.nextDouble();
						double d2 = this.getZ() + (this.random.nextDouble() * 2.0D - 1.0D) * (double) this.getBbWidth() * 0.5D;
						double d3 = (this.random.nextDouble() * 2.0D - 1.0D) * 0.3D;
						double d4 = 0.3D + this.random.nextDouble() * 0.3D;
						double d5 = (this.random.nextDouble() * 2.0D - 1.0D) * 0.3D;
						this.level().addParticle(ParticleTypes.CRIT, d0, d1 + 1.0D, d2, d3, d4, d5);
					}
					level().playLocalSound(getX(), getY(), getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, getSoundSource(), 1.0F, random.nextFloat() * 0.2F + 0.85F, false);
				}
			}
		}
		else if (--warmupDelayTicks < 0) {
			if (warmupDelayTicks == -8) {
				//TODO damage area
				//hit a bit below in case they spawn in the air
				//that removes the top part but eeeeh who will be 28 blocks above the strikes?
				for (LivingEntity livingentity : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().move(0, -4, 0).inflate(0.2D, 0.0D, 0.2D))) {
					dealDamageTo(livingentity);
				}
			}

			if (!sentSpikeEvent) {
				level().broadcastEntityEvent(this, (byte) 4);
				sentSpikeEvent = true;
			}

			if (--lifeTicks < 0) {
				discard();
			}
		}

	}

	private void dealDamageTo(LivingEntity target) {
		LivingEntity owner = getOwner();
		if (target.isAlive() && !target.isInvulnerable() && target != owner) {
			//TODO change damage
			if (owner == null) {
				target.hurt(damageSources().magic(), 6.0F);
			}
			else {
				if (owner.isAlliedTo(target)) { return; }

				target.hurt(damageSources().indirectMagic(this, owner), 6.0F);
			}

		}
	}

	@Override
	public void handleEntityEvent(byte evt) {
		super.handleEntityEvent(evt);
		if (evt == 4) {
			clientSideAttackStarted = true;
			if (!isSilent()) {
				//TODO start sound
				level().playLocalSound(getX(), getY(), getZ(), SoundEvents.EVOKER_FANGS_ATTACK, getSoundSource(), 1.0F, random.nextFloat() * 0.2F + 0.85F, false);
			}
		}

	}

	public float getAnimationProgress(float partialTicks) {
		if (!clientSideAttackStarted) return 0;
		else {
			int i = lifeTicks - 2;
			return i <= 0 ? 1 : 1 - (i - partialTicks) / 20.0F;
		}
	}

	public boolean lightningTime() {
		if (!clientSideAttackStarted) return false;
		else {
			//real lightning lasts 2 ticks
			int i = lifeTicks - 2;
			return i >= 10 && i <= 12;
		}
	}

}
