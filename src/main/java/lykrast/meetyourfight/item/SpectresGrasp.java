package lykrast.meetyourfight.item;

import java.util.UUID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;
import top.theillusivec4.curios.api.SlotContext;

public class SpectresGrasp extends CurioBaseItem {
	public SpectresGrasp(Properties properties) {
		super(properties, false);
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
		Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
		map.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(uuid, "Reach bonus", 2, AttributeModifier.Operation.ADDITION));

		return map;
	}

}
