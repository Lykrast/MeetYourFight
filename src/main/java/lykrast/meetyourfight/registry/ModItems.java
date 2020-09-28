package lykrast.meetyourfight.registry;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.item.*;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MeetYourFight.MODID)
public class ModItems {
	public static Item hauntedBell, phantoplasm, passagesToll, spectresEye, spectresGrasp;
	public static Item eggBellringer;
	
	@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> reg = event.getRegistry();
		
		hauntedBell = initItem(reg, new SummonItem(noStack()), "haunted_bell");
		phantoplasm = initItem(reg, new Item(defP()), "phantoplasm");
		passagesToll = initItem(reg, new PassagesToll(noStack()), "passages_toll");
		spectresEye = initItem(reg, new SpectresEye(noStack()), "spectres_eye");
		spectresGrasp = initItem(reg, new SpectresGrasp(noStack()), "spectres_grasp");
		
		eggBellringer = initItem(reg, new SpawnEggItem(ModEntities.BELLRINGER, 0x560080, 0xDFFFF9, defP()), "bellringer_spawn_egg");
	}

	public static Item.Properties defP() {
		return new Item.Properties().group(ItemGroupMeetYourFight.INSTANCE);
	}

	public static Item.Properties noStack() {
		return new Item.Properties().group(ItemGroupMeetYourFight.INSTANCE).maxStackSize(1);
	}

	public static <I extends Item> I initItem(IForgeRegistry<Item> reg, I item, String name) {
		item.setRegistryName(MeetYourFight.MODID, name);
		reg.register(item);
		return item;
	}
}
