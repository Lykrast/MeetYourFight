package lykrast.meetyourfight.item;

import java.util.UUID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;

public class BoneRaker extends CurioBaseItem {
	private static final UUID AT_ID = UUID.fromString("83c51458-42ca-11eb-b378-0242ac130002");

	public BoneRaker(Properties properties) {
		super(properties, false);
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(String identifier) {
		Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
		map.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(AT_ID, "Melee bonus", 2, AttributeModifier.Operation.ADDITION));

		return map;
	}

}
