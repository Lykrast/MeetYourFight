package lykrast.meetyourfight.item;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;

import lykrast.meetyourfight.misc.MYFConstants;
import lykrast.meetyourfight.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

public class CocktailCutlass extends SwordItem {
	private static final Tier TIER = new CustomTier(3, 3168, 8, 3, 14, () -> Ingredient.of(ModItems.fortunesFavor.get()));
	private static final List<Triple<MobEffect, Integer, Boolean>> EFFECTS = new ArrayList<>();
	
	public static void initEffects() {
		//Effect, duration, scale duration instead of amplifier (for like Fire Resistance)
		EFFECTS.add(Triple.of(MobEffects.MOVEMENT_SPEED, 60*20, false));
		EFFECTS.add(Triple.of(MobEffects.DIG_SPEED, 60*20, false));
		EFFECTS.add(Triple.of(MobEffects.DAMAGE_BOOST, 60*20, false));
		EFFECTS.add(Triple.of(MobEffects.REGENERATION, 10*20, false));
		EFFECTS.add(Triple.of(MobEffects.DAMAGE_RESISTANCE, 60*20, false));
		EFFECTS.add(Triple.of(MobEffects.FIRE_RESISTANCE, 60*20, true));
		EFFECTS.add(Triple.of(MobEffects.WATER_BREATHING, 60*20, true));
		EFFECTS.add(Triple.of(MobEffects.INVISIBILITY, 60*20, true));
		EFFECTS.add(Triple.of(MobEffects.ABSORPTION, 60*20, false));
		EFFECTS.add(Triple.of(MobEffects.LUCK, 60*20, false));
	}

	public CocktailCutlass(Properties builderIn) {
		super(TIER, 3, -2.4f, builderIn);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (target != null && attacker instanceof Player) {
			float luck = ((Player)attacker).getLuck();
			double chance = MYFConstants.COCKTAIL_CUTLASS_CHANCE;
			if (luck >= 0) chance = (2.0 + luck) / (10.0 + luck);
			else chance = 1.0 / (5.0 - luck);
			int effectLevel = -1;
			//Using the Item random cause it seems like that's what vanilla item uses (and at least for bonemeal it's used a different amount of times in client)
			if (attacker.level().getRandom().nextDouble() <= chance) {
				effectLevel = 0;
				//Roll for extra strength
				for (int i = 0; i < 2; i++) {
					chance *= 0.5;
					if (attacker.level().getRandom().nextDouble() <= chance) effectLevel++;
					else break;
				}
			}
			if (effectLevel >= 0) {
				//Choose effect
				Triple<MobEffect, Integer, Boolean> triple = EFFECTS.get(attacker.level().getRandom().nextInt(EFFECTS.size()));
				//If the effect doesn't scale with potency, increase duration instead
				int duration = triple.getRight() ? triple.getMiddle() * (1 + effectLevel) : triple.getMiddle();
				int potency = triple.getRight() ? 0 : effectLevel;
				attacker.addEffect(new MobEffectInstance(triple.getLeft(), duration, potency, false, false, true));
				attacker.level().playSound(null, attacker.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1, 1);
			}
		}
		return super.hurtEnemy(stack, target, attacker);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(Component.translatable(getDescriptionId() + ".desc", MYFConstants.percent(MYFConstants.COCKTAIL_CUTLASS_CHANCE)).withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable(LuckCurio.TOOLTIP_LUCK).withStyle(ChatFormatting.GRAY));
	}

}
