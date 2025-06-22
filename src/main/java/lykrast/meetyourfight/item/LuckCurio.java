package lykrast.meetyourfight.item;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class LuckCurio extends CurioBaseItem {
	public static final String TOOLTIP_LUCK = "item.meetyourfight.desc.luck";
	private Supplier<Boolean> hasLuck;
	
	public LuckCurio(Properties properties, Supplier<Boolean> hasLuck, Supplier<Object[]> args) {
		super(properties, true, args);
		this.hasLuck = hasLuck;
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		if (hasLuck.get()) tooltip.add(Component.translatable(TOOLTIP_LUCK).withStyle(ChatFormatting.GRAY));
	}

}
