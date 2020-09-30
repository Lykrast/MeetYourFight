package lykrast.meetyourfight.item;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class CurioBaseItem extends Item implements ICurio {
	private boolean hasDescription;

	public CurioBaseItem(Properties properties, boolean hasDescription) {
		super(properties);
		this.hasDescription = hasDescription;
	}

	@Override
	public boolean canRightClickEquip() {
		return true;
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (hasDescription) tooltip.add(new TranslationTextComponent(getTranslationKey() + ".desc").mergeStyle(TextFormatting.GRAY));
	}

}
