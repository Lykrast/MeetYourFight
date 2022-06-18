package lykrast.meetyourfight.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class LuckCurio extends CurioBaseItem {
	public static final String TOOLTIP_LUCK = "item.meetyourfight.desc.luck";
	
	public LuckCurio(Properties properties) {
		super(properties, true);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(Component.translatable(TOOLTIP_LUCK).withStyle(ChatFormatting.GRAY));
	}

}
