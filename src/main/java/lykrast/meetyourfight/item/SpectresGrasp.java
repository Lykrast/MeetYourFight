package lykrast.meetyourfight.item;

import java.util.UUID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;

public class SpectresGrasp extends CurioBaseItem {
	private static final UUID AT_ID = UUID.fromString("930f68d4-3550-4dca-92c3-f1b20df720ff");

	public SpectresGrasp(Properties properties) {
		super(properties, false);
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(String identifier) {
		Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
		map.put(ForgeMod.REACH_DISTANCE.get(), new AttributeModifier(AT_ID, "Reach bonus", 2, AttributeModifier.Operation.ADDITION));

		return map;
	}

}
