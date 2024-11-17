package lykrast.meetyourfight.registry;

import lykrast.gunswithoutroses.registry.GWRSounds;
import lykrast.meetyourfight.item.compat.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.RegistryObject;

public class CompatGWRItems {
	public static RegistryObject<Item> phantasmalRifle, cocktailShotgun;
	
	public static void registerItems() {
		//Does not have the increased projectile speed of the sniper for more reliable hit through walls
		phantasmalRifle = ModItems.initItem("phantasmal_rifle",
				() -> new PhantasmalRifle(ModItems.bossNS().durability(2376), 0, 1.6, 22, 0, 22)
				.fireSound(GWRSounds.sniper)
				.repair(() -> Ingredient.of(ModItems.phantoplasm.get())));
		cocktailShotgun = ModItems.initItem("cocktail_shotgun",
				() -> new CocktailShotgun(ModItems.bossNS().durability(3473), 0, 0.45, 16, 5, 14, 6)	
				.fireSound(GWRSounds.shotgun)
				.repair(() -> Ingredient.of(ModItems.fortunesFavor.get())));
	}
}
