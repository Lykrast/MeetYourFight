package lykrast.meetyourfight.entity.ai;

import java.util.EnumSet;

import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

//Rotate around attack target, but staying aligned to the grid
//Used first by Vela
public class MoveAroundTargetOrthogonal extends Goal {
	private Mob mob;
	private int moveCooldown;
	private double speed;

	public MoveAroundTargetOrthogonal(Mob mob, double speed) {
		setFlags(EnumSet.of(Goal.Flag.MOVE));
		this.mob = mob;
		this.speed = speed;
	}

	@Override
	public boolean canUse() {
		return mob.getTarget() != null && !mob.getMoveControl().hasWanted();
	}

	@Override
	public void start() {
		moveCooldown = 20;
		LivingEntity target = mob.getTarget();
		RandomSource rand = mob.getRandom();

		Direction dir = Direction.getNearest(mob.getX() - target.getX(), 0, mob.getZ() - target.getZ());
		//25% chance to switch orientation
		//75% chance to just snap back
		switch (rand.nextInt(8)) {
			case 0:
				dir = dir.getClockWise();
				break;
			case 1:
				dir = dir.getCounterClockWise();
				break;
		}
		double distance = rand.nextDouble() * 2 + 4;

		mob.getMoveControl().setWantedPosition(
				target.getX() + dir.getStepX() * distance, 
				target.getY() + 1 + rand.nextDouble() * 2, 
				target.getZ() + dir.getStepZ() * distance,
				speed);
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		return moveCooldown > 0;
	}

	@Override
	public void tick() {
		moveCooldown--;
	}

}