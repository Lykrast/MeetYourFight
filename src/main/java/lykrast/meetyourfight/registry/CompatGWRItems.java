package lykrast.meetyourfight.registry;

import lykrast.meetyourfight.item.compat.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.RegistryObject;

public class CompatGWRItems {
	public static RegistryObject<Item> phantasmalRifle, cocktailShotgun;
	
	public static void registerItems() {
		//Does not have the increased projectile speed of the sniper for more reliable hit through walls
		phantasmalRifle = ModItems.REG.register("phantasmal_rifle",
				() -> new PhantasmalRifle(ModItems.bossNS().durability(2376), 0, 1.6, 22, 0, 22)
				.fireSound(lykrast.gunswithoutroses.registry.ModSounds.sniper)
				.repair(() -> Ingredient.of(ModItems.phantoplasm.get())));
		cocktailShotgun = ModItems.REG.register("cocktail_shotgun",
				() -> new CocktailShotgun(ModItems.bossNS().durability(3473), 0, 0.45, 16, 5, 14, 6).ignoreInvulnerability(true)
				.fireSound(lykrast.gunswithoutroses.registry.ModSounds.shotgun)
				.repair(() -> Ingredient.of(ModItems.fortunesFavor.get())));
	}
}
