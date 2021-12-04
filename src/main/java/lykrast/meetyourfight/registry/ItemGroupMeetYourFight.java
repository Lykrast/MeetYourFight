package lykrast.meetyourfight.registry;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class ItemGroupMeetYourFight extends ItemGroup {
	public static final ItemGroup INSTANCE = new ItemGroupMeetYourFight(ItemGroup.getGroupCountSafe(), "meetyourfight");

	public ItemGroupMeetYourFight(int index, String label) {
		super(index, label);
	}

	@Override
	public ItemStack makeIcon() {
		return new ItemStack(ModItems.hauntedBell);
	}

}
