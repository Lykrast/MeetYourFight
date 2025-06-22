package lykrast.meetyourfight.registry;

import lykrast.gunswithoutroses.registry.GWRSounds;
import lykrast.meetyourfight.config.MYFConfigValues;
import lykrast.meetyourfight.item.compat.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.RegistryObject;

public class CompatGWRItems {
	public static RegistryObject<Item> phantasmalRifle, cocktailShotgun;
	
	public static void registerItems() {
		phantasmalRifle = MYFItems.initItem("phantasmal_rifle",
				() -> new PhantasmalRifle(MYFItems.bossNS().durability(2376), 0, 1.6, 24, 0, 22).headshotMult(1.5).projectileSpeed(4)
				.fireSound(GWRSounds.sniper)
				.repair(() -> Ingredient.of(MYFItems.phantoplasm.get())));
		cocktailShotgun = MYFItems.initItem("cocktail_shotgun",
				() -> new CocktailShotgun(MYFItems.bossNS().durability(3473), 0, 0.7, 20, 5, 14, () -> MYFConfigValues.COCKTAIL_SHOTGUN_LUCK).projectiles(4)
				.fireSound(GWRSounds.shotgun)
				.repair(() -> Ingredient.of(MYFItems.fortunesFavor.get())));
	}
}
