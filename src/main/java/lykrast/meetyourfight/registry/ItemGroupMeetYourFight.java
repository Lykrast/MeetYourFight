package lykrast.meetyourfight.registry;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ItemGroupMeetYourFight extends ItemGroup {
	public static final ItemGroup INSTANCE = new ItemGroupMeetYourFight(ItemGroup.getGroupCountSafe(), "meetyourfight");

	public ItemGroupMeetYourFight(int index, String label) {
		super(index, label);
	}

	@Override
	public ItemStack createIcon() {
		return new ItemStack(Items.STICK);
	}

}
