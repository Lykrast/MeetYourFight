package lykrast.meetyourfight.entity.ai;

import java.util.EnumSet;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;

public class VexMoveRandomGoal extends Goal {
	//The Vex's MoveRandom but separated
	private MobEntity mob;

    public VexMoveRandomGoal(MobEntity mob) {
       this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
       this.mob = mob;
    }

    @Override
	public boolean shouldExecute() {
       return !mob.getMoveHelper().isUpdating() && mob.getRNG().nextInt(7) == 0;
    }

    @Override
	public boolean shouldContinueExecuting() {
       return false;
    }

    @Override
	public void tick() {
       BlockPos blockpos = mob.getPosition();

       for(int i = 0; i < 3; ++i) {
          BlockPos blockpos1 = blockpos.add(mob.getRNG().nextInt(15) - 7, mob.getRNG().nextInt(11) - 5, mob.getRNG().nextInt(15) - 7);
          if (mob.world.isAirBlock(blockpos1)) {
             mob.getMoveHelper().setMoveTo((double)blockpos1.getX() + 0.5D, (double)blockpos1.getY() + 0.5D, (double)blockpos1.getZ() + 0.5D, 0.25D);
             if (mob.getAttackTarget() == null) {
                mob.getLookController().setLookPosition((double)blockpos1.getX() + 0.5D, (double)blockpos1.getY() + 0.5D, (double)blockpos1.getZ() + 0.5D, 180.0F, 20.0F);
             }
             break;
          }
       }

    }

}
