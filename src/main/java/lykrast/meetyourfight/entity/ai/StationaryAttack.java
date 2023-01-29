package lykrast.meetyourfight.entity.ai;

import java.util.EnumSet;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

public abstract class StationaryAttack extends Goal {
	//An attack where the boss stays still and maintains y position to not fall through the ground
	private Mob boss;
	private double stationaryY;
	
	public StationaryAttack(Mob boss) {
		setFlags(EnumSet.of(Goal.Flag.MOVE));
		this.boss = boss;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}
	
	@Override
	public void start() {
		if (boss.getTarget() != null) stationaryY = boss.getTarget().getY() + 1 + boss.getRandom().nextDouble() * 2;
		else stationaryY = boss.getY();
	}

	@Override
	public void tick() {
		if (!boss.getMoveControl().hasWanted()) {
			if (Math.abs(boss.getY() - stationaryY) >= 1) {
				boss.getMoveControl().setWantedPosition(boss.getX(), stationaryY, boss.getZ(), 1);
			}
		}
	}

}
