package lykrast.meetyourfight.item.compat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Triple;

import lykrast.gunswithoutroses.item.IBullet;
import lykrast.gunswithoutroses.item.ShotgunItem;
import lykrast.meetyourfight.item.LuckCurio;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class CocktailShotgun extends ShotgunItem {
	private static final List<Triple<Effect, Integer, Boolean>> EFFECTS = new ArrayList<>();
	
	public static void initEffects() {
		//Effect, duration, scale duration instead of amplifier (for like Glowing)
		EFFECTS.add(Triple.of(Effects.MOVEMENT_SLOWDOWN, 20*20, false));
		EFFECTS.add(Triple.of(Effects.WEAKNESS, 20*20, false));
		EFFECTS.add(Triple.of(Effects.POISON, 20*20, false));
		EFFECTS.add(Triple.of(Effects.WITHER, 20*20, false));
		EFFECTS.add(Triple.of(Effects.GLOWING, 20*20, true));
		EFFECTS.add(Triple.of(Effects.LEVITATION, 5*20, false));
	}

	public CocktailShotgun(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay, double inaccuracy, int enchantability, int bulletCount) {
		super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability, bulletCount);
	}
	
	@Override
	protected void shoot(World world, PlayerEntity player, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
		super.shoot(world, player, gun, ammo, bulletItem, bulletFree);
		//Roll for potion
		float luck = player.getLuck();
		double chance = 1.0 / 3.0;
		if (luck >= 0) chance = (2.0 + luck) / (6.0 + luck);
		else chance = 1.0 / (3.0 - luck);
		int effectLevel = -1;
		//Using the Item random cause it seems like that's what vanilla item uses (and at least for bonemeal it's used a different amount of times in client)
		if (random.nextDouble() <= chance) {
			effectLevel = 0;
			//Roll for extra strength
			for (int i = 0; i < 2; i++) {
				chance *= 0.5;
				if (random.nextDouble() <= chance) effectLevel++;
				else break;
			}
		}
		if (effectLevel >= 0) {
			//Choose effect
			Triple<Effect, Integer, Boolean> triple = EFFECTS.get(random.nextInt(EFFECTS.size()));
			//If the effect doesn't scale with potency, increase duration instead
			int duration = triple.getRight() ? triple.getMiddle() * (1 + effectLevel) : triple.getMiddle();
			int potency = triple.getRight() ? 0 : effectLevel;
			
	        PotionEntity potionentity = new PotionEntity(world, player);
	        potionentity.setItem(PotionUtils.setCustomEffects(new ItemStack(Items.SPLASH_POTION), Collections.singleton(new EffectInstance(triple.getLeft(), duration, potency))));
	        potionentity.shootFromRotation(player, player.xRot, player.yRot, -5, (float)getProjectileSpeed(gun, player), 1);
	        world.addFreshEntity(potionentity);
	        
	        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SPLASH_POTION_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
		}
	}

	@Override
	protected void addExtraStatsTooltip(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip) {
		super.addExtraStatsTooltip(stack, world, tooltip);
		tooltip.add(new TranslationTextComponent(getDescriptionId() + ".desc").withStyle(TextFormatting.GRAY));
		tooltip.add(new TranslationTextComponent(LuckCurio.TOOLTIP_LUCK).withStyle(TextFormatting.GRAY));
	}

}
