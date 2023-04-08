package lykrast.meetyourfight.item;

import java.util.UUID;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import lykrast.meetyourfight.registry.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeMod;

public class TwilightsThorn extends SwordItem {
	private static final Tier TIER = new CustomTier(3, 3873, 10, 4, 16, () -> Ingredient.of(ModItems.violetBloom.get()));
	// Well it's private and final in the constructor, so gotta remake it to add my own stuff
	private final Multimap<Attribute, AttributeModifier> defaultModifiers;
	public static final UUID RANGE = UUID.fromString("3080eef9-4ab7-4f8b-9a90-774208857fc9");

	public TwilightsThorn(Properties builderIn) {
		super(TIER, 3, -2.2f, builderIn);
		ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", getDamage(), AttributeModifier.Operation.ADDITION));
		builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.2, AttributeModifier.Operation.ADDITION));
		builder.put(ForgeMod.ATTACK_RANGE.get(), new AttributeModifier(RANGE, "Weapon modifier", 1, AttributeModifier.Operation.ADDITION));
		defaultModifiers = builder.build();
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
		return slot == EquipmentSlot.MAINHAND ? defaultModifiers : super.getDefaultAttributeModifiers(slot);
	}

}
