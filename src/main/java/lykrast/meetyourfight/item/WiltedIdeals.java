package lykrast.meetyourfight.item;

import java.util.UUID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import lykrast.meetyourfight.config.MYFConfigValues;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class WiltedIdeals extends CurioBaseItem {
	public WiltedIdeals(Properties properties) {
		super(properties, true, () -> new Object[] {MYFConfigValues.percent(MYFConfigValues.WILTED_IDEALS_MULT - 1)});
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
		Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
		map.put(Attributes.MAX_HEALTH, new AttributeModifier(uuid, "Wilted Ideals", MYFConfigValues.WILTED_IDEALS_PENALTY, AttributeModifier.Operation.MULTIPLY_TOTAL));

		return map;
	}
	
	@Override
	public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
		//That should help the health update on the UI
		if (prevStack != stack && slotContext.entity() != null) slotContext.entity().hurt(slotContext.entity().damageSources().starve(), 1);
	}

}
