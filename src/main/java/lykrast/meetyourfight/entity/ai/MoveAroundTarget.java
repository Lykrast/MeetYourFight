package lykrast.meetyourfight.entity.ai;

import java.util.EnumSet;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

//Rotate around attack target
//Used first by Dame Fortuna
public class MoveAroundTarget extends Goal {
	private Mob mob;

	public MoveAroundTarget(Mob mob) {
		setFlags(EnumSet.of(Goal.Flag.MOVE));
		this.mob = mob;
	}

	@Override
	public boolean canUse() {
		return mob.getTarget() != null && !mob.getMoveControl().hasWanted();
	}

	@Override
	public void start() {			
		LivingEntity target = mob.getTarget();
		RandomSource rand = mob.getRandom();
		float angle = (rand.nextInt(4) + 2) * 10f * ((float)Math.PI / 180F);
		if (rand.nextBoolean()) angle *= -1;
		Vec3 offset = new Vec3(mob.getX() - target.getX(), 0, mob.getZ() - target.getZ()).normalize().yRot(angle);
		double distance = rand.nextDouble() * 2 + 4;

		mob.getMoveControl().setWantedPosition(
				target.getX() + offset.x * distance, 
				target.getY() + 1 + rand.nextDouble() * 2, 
				target.getZ() + offset.z * distance,
				1);
	}

	@Override
	public boolean canContinueToUse() {
		return false;
	}

}