package lykrast.meetyourfight.entity.movement;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class VexMovementController extends MovementController {
	//This is just the Vex's movement controller but separated
	public VexMovementController(MobEntity mob) {
		super(mob);
	}

    @Override
	public void tick() {
       if (action == MovementController.Action.MOVE_TO) {
          Vector3d vector3d = new Vector3d(posX - mob.getPosX(), posY - mob.getPosY(), posZ - mob.getPosZ());
          double d0 = vector3d.length();
          if (d0 < mob.getBoundingBox().getAverageEdgeLength()) {
             action = MovementController.Action.WAIT;
             mob.setMotion(mob.getMotion().scale(0.5D));
          } else {
             mob.setMotion(mob.getMotion().add(vector3d.scale(this.speed * 0.05D / d0)));
             if (mob.getAttackTarget() == null) {
                Vector3d vector3d1 = mob.getMotion();
                mob.rotationYaw = -((float)MathHelper.atan2(vector3d1.x, vector3d1.z)) * (180F / (float)Math.PI);
                mob.renderYawOffset = mob.rotationYaw;
             } else {
                double d2 = mob.getAttackTarget().getPosX() - mob.getPosX();
                double d1 = mob.getAttackTarget().getPosZ() - mob.getPosZ();
                mob.rotationYaw = -((float)MathHelper.atan2(d2, d1)) * (180F / (float)Math.PI);
                mob.renderYawOffset = mob.rotationYaw;
             }
          }

       }
    }

}
