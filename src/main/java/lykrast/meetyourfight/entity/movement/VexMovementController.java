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
       if (operation == MovementController.Action.MOVE_TO) {
          Vector3d vector3d = new Vector3d(wantedX - mob.getX(), wantedY - mob.getY(), wantedZ - mob.getZ());
          double d0 = vector3d.length();
          if (d0 < mob.getBoundingBox().getSize()) {
             operation = MovementController.Action.WAIT;
             mob.setDeltaMovement(mob.getDeltaMovement().scale(0.5D));
          } else {
             mob.setDeltaMovement(mob.getDeltaMovement().add(vector3d.scale(this.speedModifier * 0.05D / d0)));
             if (mob.getTarget() == null) {
                Vector3d vector3d1 = mob.getDeltaMovement();
                mob.yRot = -((float)MathHelper.atan2(vector3d1.x, vector3d1.z)) * (180F / (float)Math.PI);
                mob.yBodyRot = mob.yRot;
             } else {
                double d2 = mob.getTarget().getX() - mob.getX();
                double d1 = mob.getTarget().getZ() - mob.getZ();
                mob.yRot = -((float)MathHelper.atan2(d2, d1)) * (180F / (float)Math.PI);
                mob.yBodyRot = mob.yRot;
             }
          }

       }
    }

}
