package lykrast.meetyourfight.item;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class PassagesToll extends Item {

	public PassagesToll(Properties properties) {
		super(properties);
	}

	@Override
	public ActionResultType useOn(ItemUseContext context) {
		World world = context.getLevel();
		if (world.isClientSide) return ActionResultType.SUCCESS;

		BlockPos pos = context.getClickedPos();
		Direction dir = context.getClickedFace().getOpposite();
		PlayerEntity player = context.getPlayer();
		
		player.getCooldowns().addCooldown(this, 20);

		//Query the wall to see if there's a hole
		BlockPos.Mutable mut = new BlockPos.Mutable(pos.getX(), pos.getY(), pos.getZ());
		boolean found = false;
		for (int i = 0; i < 16; i++) {
			mut.move(dir);
			//Prevent players getting in the void
			if (mut.getY() <= 0) break;
			if (!world.getBlockState(mut).getMaterial().blocksMotion()) {
				found = true;
				//Bring down to player's height as much as possible
				double targetY = player.getY();
				while (mut.getY() > targetY && mut.getY() > 1) {
					mut.move(0, -1, 0);
					if (world.getBlockState(mut).getMaterial().blocksMotion()) {
						mut.move(0, 1, 0);
						break;
					}
				}
				break;
			}
		}
		
		if (!found) return ActionResultType.FAIL;
		else {
			player.teleportTo(mut.getX() + 0.5, mut.getY() + 0.5, mut.getZ() + 0.5);
			player.fallDistance = 0;
			world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1, 1);
			player.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 1, 1);
			return ActionResultType.SUCCESS;
		}

	}

	@Override
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		tooltip.add(new TranslationTextComponent(getDescriptionId() + ".desc").withStyle(TextFormatting.GRAY));
	}

}
