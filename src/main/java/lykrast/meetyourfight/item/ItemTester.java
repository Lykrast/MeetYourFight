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
			double d0 = pla.getX();
			double d1 = pla.getY();
			double d2 = pla.getZ();
			GhostLineEntity ghost = new GhostLineEntity(world, player, 0, 0, 0);
			ghost.setShooter(player);
			ghost.setPosition(d0, d1, d2);
			ghost.setUp(10, 1, 0, 0, d0 - 7, d1, d2);
			world.addEntity(ghost);
		}

		return ActionResult.func_233538_a_(itemstack, world.isRemote());
	}

}
