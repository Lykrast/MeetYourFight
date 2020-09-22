package lykrast.meetyourfight.item;

import lykrast.meetyourfight.entity.GhostLineEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemTester extends Item {

	public ItemTester(Properties properties) {
		super(properties);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
		ItemStack itemstack = player.getHeldItem(hand);
		world.playSound((PlayerEntity) null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
		if (!world.isRemote) {
			BlockPos pla = player.getPosition();
			double px = pla.getX();
			double py = pla.getY();
			double pz = pla.getZ();
			for (int i = -4; i <= 4; i++) {
				GhostLineEntity ghost = new GhostLineEntity(world, player, 0, 0, 0);
				ghost.setShooter(player);
				ghost.setPosition(px - 2 + random.nextDouble() * 4, py - 2 + random.nextDouble() * 4, pz - 2 + random.nextDouble() * 4);
				ghost.setUp(20 + i, 1, 0, 0, px - 6, py - 3, pz + i);
				world.addEntity(ghost);
			}
		}

		return ActionResult.func_233538_a_(itemstack, world.isRemote());
	}

}
