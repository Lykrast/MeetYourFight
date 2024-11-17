package lykrast.meetyourfight.item;

import java.util.List;

import lykrast.meetyourfight.misc.MYFConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class PassagesToll extends Item {
	public PassagesToll(Properties properties) {
		super(properties);
	}

	@SuppressWarnings("deprecation")
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level world = context.getLevel();
		if (world.isClientSide) return InteractionResult.SUCCESS;

		BlockPos pos = context.getClickedPos();
		Direction dir = context.getClickedFace().getOpposite();
		Player player = context.getPlayer();
		
		player.getCooldowns().addCooldown(this, 20);

		//Query the wall to see if there's a hole
		BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
		boolean found = false;
		for (int i = 0; i < MYFConstants.PASSAGES_TOLL_RANGE; i++) {
			mut.move(dir);
			//Prevent players getting in the void
			if (mut.getY() <= world.getMinBuildHeight()) break;
			//I dunno why blocksmotion is deprecated and I have no sign of what to replace it with
			if (!world.getBlockState(mut).blocksMotion()) {
				found = true;
				//Bring down to player's height as much as possible
				double targetY = player.getY();
				while (mut.getY() > targetY && mut.getY() > 1) {
					mut.move(0, -1, 0);
					if (world.getBlockState(mut).blocksMotion()) {
						mut.move(0, 1, 0);
						break;
					}
				}
				break;
			}
		}
		
		if (!found) return InteractionResult.FAIL;
		else {
			player.teleportTo(mut.getX() + 0.5, mut.getY() + 0.5, mut.getZ() + 0.5);
			player.fallDistance = 0;
			world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1, 1);
			player.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 1, 1);
			return InteractionResult.SUCCESS;
		}

	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(Component.translatable(getDescriptionId() + ".desc", MYFConstants.PASSAGES_TOLL_RANGE).withStyle(ChatFormatting.GRAY));
	}

}
