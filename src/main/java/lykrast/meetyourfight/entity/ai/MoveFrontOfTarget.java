package lykrast.meetyourfight.entity.ai;

import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

//Stay in front of attack target
//Used first by Bellringer
public class MoveFrontOfTarget extends Goal {
	private Mob mob;
	private int moveCooldown;

	public MoveFrontOfTarget(Mob mob) {
		setFlags(EnumSet.of(Goal.Flag.MOVE));
		this.mob = mob;
	}

	@Override
	public boolean canUse() {
		return mob.getTarget() != null && !mob.getMoveControl().hasWanted();
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void start() {
		moveCooldown = 20;
		
		LivingEntity target = mob.getTarget();
		BlockPos targetP = target.blockPosition();
		Vec3 look = Vec3.directionFromRotation(0, target.getYRot());

		mob.getMoveControl().setWantedPosition(
				targetP.getX() + look.x * 4 - 0.5 + mob.getRandom().nextDouble() * 2, 
				targetP.getY() + 2 + mob.getRandom().nextDouble() * 2, 
				targetP.getZ() + look.z * 4 - 0.5 + mob.getRandom().nextDouble() * 2,
				1);
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