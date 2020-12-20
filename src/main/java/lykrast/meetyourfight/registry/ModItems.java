package lykrast.meetyourfight.registry;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.*;
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
	public static Item devilsAnte, fortunesFavor, slicersDice, aceOfIron, cocktailCutlass;
	public static Item fossilBait, mossyTooth, boneRaker;
	public static Item eggBellringer, eggDameFortuna, eggSwampjaw;
	
	@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> reg = event.getRegistry();
		
		hauntedBell = initItem(reg, new SummonItem(noStack(), BellringerEntity::spawn), "haunted_bell");
		phantoplasm = initItem(reg, new Item(defP()), "phantoplasm");
		passagesToll = initItem(reg, new PassagesToll(noStack()), "passages_toll");
		spectresEye = initItem(reg, new SpectresEye(noStack()), "spectres_eye");
		spectresGrasp = initItem(reg, new SpectresGrasp(noStack()), "spectres_grasp");
		
		devilsAnte = initItem(reg, new SummonItem(noStack(), DameFortunaEntity::spawn), "devils_ante");
		fortunesFavor = initItem(reg, new Item(defP()), "fortunes_favor");
		slicersDice = initItem(reg, new LuckCurio(noStack()), "slicers_dice");
		aceOfIron = initItem(reg, new LuckCurio(noStack()), "ace_of_iron");
		cocktailCutlass = initItem(reg, new CocktailCutlass(noStack()), "cocktail_cutlass");
		
		fossilBait = initItem(reg, new SummonItem(noStack(), SwampjawEntity::spawn), "fossil_bait");
		mossyTooth = initItem(reg, new Item(defP()), "mossy_tooth");
		boneRaker = initItem(reg, new BoneRaker(noStack()), "bone_raker");
		
		eggBellringer = initItem(reg, new SpawnEggItem(ModEntities.BELLRINGER, 0x560080, 0xDFFFF9, defP()), "bellringer_spawn_egg");
		eggDameFortuna = initItem(reg, new SpawnEggItem(ModEntities.DAME_FORTUNA, 0xFE0000, 0xEEEEEE, defP()), "dame_fortuna_spawn_egg");
		eggSwampjaw = initItem(reg, new SpawnEggItem(ModEntities.SWAMPJAW, 0xFCFBED, 0x738552, defP()), "swampjaw_spawn_egg");
		
		if (MeetYourFight.loadedGunsWithoutRoses()) CompatGWRItems.registerItems(reg);
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
