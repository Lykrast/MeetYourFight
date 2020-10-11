package lykrast.meetyourfight.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Triple;

import lykrast.meetyourfight.registry.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class CocktailCutlass extends SwordItem {
	private static final IItemTier TIER = new CustomTier(3, 3168, 8, 3, 14, () -> Ingredient.fromItems(ModItems.fortunesFavor));
	private static final List<Triple<Effect, Integer, Boolean>> EFFECTS = new ArrayList<>();
	
	public static void initEffects() {
		//Effect, duration, scale duration instead of amplifier (for like Fire Resistance)
		EFFECTS.add(Triple.of(Effects.SPEED, 60*20, false));
		EFFECTS.add(Triple.of(Effects.HASTE, 60*20, false));
		EFFECTS.add(Triple.of(Effects.STRENGTH, 60*20, false));
		EFFECTS.add(Triple.of(Effects.REGENERATION, 10*20, false));
		EFFECTS.add(Triple.of(Effects.RESISTANCE, 60*20, false));
		EFFECTS.add(Triple.of(Effects.FIRE_RESISTANCE, 60*20, true));
		EFFECTS.add(Triple.of(Effects.WATER_BREATHING, 60*20, true));
		EFFECTS.add(Triple.of(Effects.INVISIBILITY, 60*20, true));
		EFFECTS.add(Triple.of(Effects.ABSORPTION, 60*20, false));
		EFFECTS.add(Triple.of(Effects.LUCK, 60*20, false));
	}

	public CocktailCutlass(Properties builderIn) {
		super(TIER, 3, -2.4f, builderIn);
	}

	@Override
	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (target != null && attacker instanceof PlayerEntity) {
			Random rand = attacker.getRNG();
			double luck = attacker.getAttributeValue(Attributes.LUCK);
			double chance = 1.0 / 6.0;
			if (luck >= 0) chance = (2.0 + luck) / (12.0 + luck);
			else chance = 1.0 / (6.0 - luck);
			int effectLevel = -1;
			if (rand.nextDouble() <= chance) {
				effectLevel = 0;
				//Roll for extra strength
				for (int i = 0; i < 2; i++) {
					chance *= 0.5;
					if (rand.nextDouble() <= chance) effectLevel++;
					else break;
				}
			}
			if (effectLevel >= 0) {
				//Choose effect
				Triple<Effect, Integer, Boolean> triple = EFFECTS.get(rand.nextInt(EFFECTS.size()));
				//If the effect doesn't scale with potency, increase duration instead
				int duration = triple.getRight() ? triple.getMiddle() * (1 + effectLevel) : triple.getMiddle();
				int potency = triple.getRight() ? 0 : effectLevel;
				attacker.addPotionEffect(new EffectInstance(triple.getLeft(), duration, potency, false, false, true));
				attacker.world.playSound(null, attacker.getPosition(), SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.PLAYERS, 1, 1);
			}
		}
		return super.hitEntity(stack, target, attacker);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		tooltip.add(new TranslationTextComponent(getTranslationKey() + ".desc").mergeStyle(TextFormatting.GRAY));
		tooltip.add(new TranslationTextComponent(LuckCurio.TOOLTIP_LUCK).mergeStyle(TextFormatting.GRAY));
	}

}
