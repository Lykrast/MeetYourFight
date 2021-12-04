package lykrast.meetyourfight.item;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class LuckCurio extends CurioBaseItem {
	public static final String TOOLTIP_LUCK = "item.meetyourfight.desc.luck";
	
	public LuckCurio(Properties properties) {
		super(properties, true);
	}

	@Override
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(new TranslationTextComponent(TOOLTIP_LUCK).withStyle(TextFormatting.GRAY));
	}

}
