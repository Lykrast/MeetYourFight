package lykrast.meetyourfight.entity.ai;

import java.util.Comparator;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;

public class PhantomAttackPlayer extends Goal {
	//Phantom's targeting except I make it see through walls
	//setLineOfSiteRequired is backwards btw, it should be like "ignoreLineOfSight"
	private final EntityPredicate predicate = (new EntityPredicate()).setDistance(64.0D).setLineOfSiteRequired();
	public static final EntityPredicate DEFAULT_BUT_THROUGH_WALLS = (new EntityPredicate()).setLineOfSiteRequired();
	private int tickDelay = 20;
	private MobEntity entity;

	public PhantomAttackPlayer(MobEntity entity) {
		this.entity = entity;
	}

	@Override
	public boolean shouldExecute() {
		if (this.tickDelay > 0) {
			--this.tickDelay;
			return false;
		}
		else {
			this.tickDelay = 60;
			List<PlayerEntity> list = entity.world.getTargettablePlayersWithinAABB(predicate, entity, entity.getBoundingBox().grow(16.0D, 64.0D, 16.0D));
			if (!list.isEmpty()) {
				list.sort(Comparator.<Entity, Double>comparing(Entity::getPosY).reversed());

				for (PlayerEntity playerentity : list) {
					if (entity.canAttack(playerentity, DEFAULT_BUT_THROUGH_WALLS)) {
						entity.setAttackTarget(playerentity);
						return true;
					}
				}
			}

			return false;
		}
	}

	@Override
	public boolean shouldContinueExecuting() {
		LivingEntity livingentity = entity.getAttackTarget();
		return livingentity != null ? entity.canAttack(livingentity, DEFAULT_BUT_THROUGH_WALLS) : false;
	}

}
