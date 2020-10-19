package lykrast.meetyourfight.registry;

import lykrast.meetyourfight.item.compat.PhantasmalRifle;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.registries.IForgeRegistry;

public class CompatGWRItems {
	public static Item phantasmalRifle;
	
	public static void registerItems(IForgeRegistry<Item> reg) {
		//Does not have the increased projectile speed of the sniper for more reliable hit through walls
		phantasmalRifle = ModItems.initItem(reg, 
				new PhantasmalRifle(ModItems.noStack().maxDamage(5276), 0, 2.5, 22, 0, 22)
				.fireSound(lykrast.gunswithoutroses.registry.ModSounds.sniper)
				.repair(() -> Ingredient.fromItems(ModItems.phantoplasm)), 
				"phantasmal_rifle");
	}
}
