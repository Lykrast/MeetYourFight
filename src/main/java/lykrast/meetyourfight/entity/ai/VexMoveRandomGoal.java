package lykrast.meetyourfight.entity.ai;

import java.util.EnumSet;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.core.BlockPos;

public class VexMoveRandomGoal extends Goal {
	//The Vex's MoveRandom but separated
	private Mob mob;

    public VexMoveRandomGoal(Mob mob) {
       setFlags(EnumSet.of(Goal.Flag.MOVE));
       this.mob = mob;
    }

    @Override
	public boolean canUse() {
       return !mob.getMoveControl().hasWanted() && mob.getRandom().nextInt(7) == 0;
    }

    @Override
	public boolean canContinueToUse() {
       return false;
    }

    @Override
	public void tick() {
       BlockPos blockpos = mob.blockPosition();

       for(int i = 0; i < 3; ++i) {
          BlockPos blockpos1 = blockpos.offset(mob.getRandom().nextInt(15) - 7, mob.getRandom().nextInt(11) - 5, mob.getRandom().nextInt(15) - 7);
          if (mob.level.isEmptyBlock(blockpos1)) {
             mob.getMoveControl().setWantedPosition(blockpos1.getX() + 0.5, blockpos1.getY() + 0.5, blockpos1.getZ() + 0.5, 0.25);
             if (mob.getTarget() == null) {
                mob.getLookControl().setLookAt(blockpos1.getX() + 0.5, blockpos1.getY() + 0.5, blockpos1.getZ() + 0.5, 180.0F, 20.0F);
             }
             break;
          }
       }

    }

}
