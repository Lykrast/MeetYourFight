package lykrast.meetyourfight.registry;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ItemGroupMeetYourFight extends CreativeModeTab {
	public static final CreativeModeTab INSTANCE = new ItemGroupMeetYourFight(CreativeModeTab.getGroupCountSafe(), "meetyourfight");

	public ItemGroupMeetYourFight(int index, String label) {
		super(index, label);
	}

	@Override
	public ItemStack makeIcon() {
		return new ItemStack(ModItems.hauntedBell);
	}

}
