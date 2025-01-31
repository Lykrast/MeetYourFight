package lykrast.meetyourfight.entity.ai;

import java.util.Comparator;
import java.util.List;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

public class PhantomAttackPlayer extends Goal {
	//Phantom's targeting except I make it see through walls
	private final TargetingConditions predicate = TargetingConditions.forCombat().ignoreLineOfSight();
	public static final TargetingConditions DEFAULT_BUT_THROUGH_WALLS = TargetingConditions.forCombat().ignoreLineOfSight();
	private int tickDelay = 20;
	private Mob entity;

	public PhantomAttackPlayer(Mob entity) {
		this.entity = entity;
	}

	@Override
	public boolean canUse() {
		if (this.tickDelay > 0) {
			--this.tickDelay;
			return false;
		}
		else {
			this.tickDelay = 60;
			List<Player> list = entity.level().getNearbyPlayers(predicate, entity, entity.getBoundingBox().inflate(16.0D, 64.0D, 16.0D));
			if (!list.isEmpty()) {
				list.sort(Comparator.<Entity, Double>comparing(Entity::getY).reversed());

				for (Player playerentity : list) {
					if (entity.canAttack(playerentity, DEFAULT_BUT_THROUGH_WALLS)) {
						entity.setTarget(playerentity);
						return true;
					}
				}
			}

			return false;
		}
	}

	@Override
	public boolean canContinueToUse() {
		LivingEntity livingentity = entity.getTarget();
		return livingentity != null ? entity.canAttack(livingentity, DEFAULT_BUT_THROUGH_WALLS) : false;
	}

}
