package lykrast.meetyourfight.registry;

import lykrast.meetyourfight.item.compat.*;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.registries.IForgeRegistry;

public class CompatGWRItems {
	public static Item phantasmalRifle, cocktailShotgun;
	
	public static void registerItems(IForgeRegistry<Item> reg) {
		//Does not have the increased projectile speed of the sniper for more reliable hit through walls
		phantasmalRifle = ModItems.initItem(reg, 
				new PhantasmalRifle(ModItems.bossNS().maxDamage(2376), 0, 1.6, 22, 0, 22)
				.fireSound(lykrast.gunswithoutroses.registry.ModSounds.sniper)
				.repair(() -> Ingredient.fromItems(ModItems.phantoplasm)), 
				"phantasmal_rifle");
		cocktailShotgun = ModItems.initItem(reg, 
				new CocktailShotgun(ModItems.bossNS().maxDamage(3473), 0, 0.45, 16, 5, 14, 6).ignoreInvulnerability(true)
				.fireSound(lykrast.gunswithoutroses.registry.ModSounds.shotgun)
				.repair(() -> Ingredient.fromItems(ModItems.fortunesFavor)), 
				"cocktail_shotgun");
	}
}
