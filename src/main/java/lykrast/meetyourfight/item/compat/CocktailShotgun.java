package lykrast.meetyourfight.item.compat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Triple;

import lykrast.gunswithoutroses.item.GunItem;
import lykrast.gunswithoutroses.item.IBullet;
import lykrast.meetyourfight.config.MYFConfigValues;
import lykrast.meetyourfight.item.LuckCurio;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;

public class CocktailShotgun extends GunItem {
	private static final List<Triple<MobEffect, Integer, Boolean>> EFFECTS = new ArrayList<>();
	private Supplier<Boolean> hasLuck;
	
	public static void initEffects() {
		//Effect, duration, scale duration instead of amplifier (for like Glowing)
		EFFECTS.add(Triple.of(MobEffects.MOVEMENT_SLOWDOWN, 20*20, false));
		EFFECTS.add(Triple.of(MobEffects.WEAKNESS, 20*20, false));
		EFFECTS.add(Triple.of(MobEffects.POISON, 20*20, false));
		EFFECTS.add(Triple.of(MobEffects.WITHER, 20*20, false));
		EFFECTS.add(Triple.of(MobEffects.GLOWING, 20*20, true));
		EFFECTS.add(Triple.of(MobEffects.LEVITATION, 5*20, false));
	}

	public CocktailShotgun(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay, double inaccuracy, int enchantability, Supplier<Boolean> hasLuck) {
		super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability);
		this.hasLuck = hasLuck;
	}
	
	@Override
	protected void shoot(Level world, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
		super.shoot(world, player, gun, ammo, bulletItem, bulletFree);
		//Roll for potion
		float luck = player.getLuck();
		double chance = MYFConfigValues.COCKTAIL_SHOTGUN_CHANCE;
		if (MYFConfigValues.COCKTAIL_SHOTGUN_LUCK) {
			if (luck >= 0) chance = (1.0 + 0.5*luck) / ((1.0/MYFConfigValues.COCKTAIL_SHOTGUN_CHANCE) + 0.5*luck);
			else chance = 1.0 / ((1.0/MYFConfigValues.COCKTAIL_SHOTGUN_CHANCE) - luck);
		}
		int effectLevel = -1;
		//Using the Item random cause it seems like that's what vanilla item uses (and at least for bonemeal it's used a different amount of times in client)
		if (world.getRandom().nextDouble() <= chance) {
			effectLevel = 0;
			//Roll for extra strength
			for (int i = 0; i < 2; i++) {
				chance *= 0.5;
				if (world.getRandom().nextDouble() <= chance) effectLevel++;
				else break;
			}
		}
		if (effectLevel >= 0) {
			//Choose effect
			Triple<MobEffect, Integer, Boolean> triple = EFFECTS.get(world.getRandom().nextInt(EFFECTS.size()));
			//If the effect doesn't scale with potency, increase duration instead
			int duration = triple.getRight() ? triple.getMiddle() * (1 + effectLevel) : triple.getMiddle();
			int potency = triple.getRight() ? 0 : effectLevel;
			
	        ThrownPotion potionentity = new ThrownPotion(world, player);
	        potionentity.setItem(PotionUtils.setCustomEffects(new ItemStack(Items.SPLASH_POTION), Collections.singleton(new MobEffectInstance(triple.getLeft(), duration, potency))));
	        potionentity.shootFromRotation(player, player.getXRot(), player.getYRot(), -5, (float)getProjectileSpeed(gun, player), 1);
	        world.addFreshEntity(potionentity);
	        
	        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
		}
	}

	@Override
	protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level world, List<Component> tooltip) {
		super.addExtraStatsTooltip(stack, world, tooltip);
		tooltip.add(Component.translatable(getDescriptionId() + ".desc", MYFConfigValues.percent(MYFConfigValues.COCKTAIL_SHOTGUN_CHANCE)).withStyle(ChatFormatting.GRAY));
		if (hasLuck.get()) tooltip.add(Component.translatable(LuckCurio.TOOLTIP_LUCK).withStyle(ChatFormatting.GRAY));
	}

}
