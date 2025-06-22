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

public class BoneRaker extends CurioBaseItem {
	public BoneRaker(Properties properties) {
		super(properties, false);
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
		Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
		map.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(uuid, "Melee bonus", MYFConfigValues.BONE_RAKER_BONUS, AttributeModifier.Operation.ADDITION));

		return map;
	}

}
