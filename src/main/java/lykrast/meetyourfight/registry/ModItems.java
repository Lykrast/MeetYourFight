package lykrast.meetyourfight.registry;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.*;
import lykrast.meetyourfight.item.*;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MeetYourFight.MODID)
public class ModItems {
	public static Item hauntedBell, phantoplasm, passagesToll, spectresEye, spectresGrasp, aetherGlazedCupcake;
	public static Item devilsAnte, fortunesFavor, slicersDice, aceOfIron, cocktailCutlass, velvetFortune;
	public static Item fossilBait, mossyTooth, boneRaker, depthStar, cagedHeart, marshyDelight;
	public static Item discMagnum;
	public static Item eggBellringer, eggDameFortuna, eggSwampjaw;
	
	@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> reg = event.getRegistry();
		
		hauntedBell = initItem(reg, new SummonItem(noStack(), BellringerEntity::spawn), "haunted_bell");
		phantoplasm = initItem(reg, new Item(boss()), "phantoplasm");
		passagesToll = initItem(reg, new PassagesToll(bossNS()), "passages_toll");
		spectresEye = initItem(reg, new SpectresEye(bossNS()), "spectres_eye");
		spectresGrasp = initItem(reg, new SpectresGrasp(bossNS()), "spectres_grasp");
		aetherGlazedCupcake = initItem(reg, new Item(boss().food((new FoodProperties.Builder().nutrition(5).saturationMod(0.6f).alwaysEat().effect(() -> new MobEffectInstance(MobEffects.LEVITATION, 5*20), 1).build()))), "aether_glazed_cupcake");
		
		devilsAnte = initItem(reg, new SummonItem(noStack(), DameFortunaEntity::spawn), "devils_ante");
		fortunesFavor = initItem(reg, new Item(boss()), "fortunes_favor");
		slicersDice = initItem(reg, new LuckCurio(bossNS()), "slicers_dice");
		aceOfIron = initItem(reg, new LuckCurio(bossNS()), "ace_of_iron");
		cocktailCutlass = initItem(reg, new CocktailCutlass(bossNS()), "cocktail_cutlass");
		velvetFortune = initItem(reg, new Item(boss().food((new FoodProperties.Builder().nutrition(2).saturationMod(0.1f).alwaysEat().effect(() -> new MobEffectInstance(MobEffects.LUCK, 10*60*20), 1).build()))), "velvet_fortune");
		
		fossilBait = initItem(reg, new SummonItem(noStack(), SwampjawEntity::spawn), "fossil_bait");
		mossyTooth = initItem(reg, new Item(boss()), "mossy_tooth");
		boneRaker = initItem(reg, new BoneRaker(bossNS()), "bone_raker");
		depthStar = initItem(reg, new DepthStar(bossNS()), "depth_star");
		cagedHeart = initItem(reg, new CurioBaseItem(bossNS(), true), "caged_heart");
		marshyDelight = initItem(reg, new Item(boss().food((new FoodProperties.Builder().nutrition(14).saturationMod(0.9f).meat().build()))), "marshy_delight");
		
		discMagnum = initItem(reg, new RecordItem(1, ModSounds::supplyMagnum, noStack().rarity(Rarity.RARE)), "music_disc_magnum");
		
		eggBellringer = initItem(reg, new ForgeSpawnEggItem(() -> ModEntities.BELLRINGER, 0x560080, 0xDFFFF9, defP()), "bellringer_spawn_egg");
		eggDameFortuna = initItem(reg, new ForgeSpawnEggItem(() -> ModEntities.DAME_FORTUNA, 0xFE0000, 0xEEEEEE, defP()), "dame_fortuna_spawn_egg");
		eggSwampjaw = initItem(reg, new ForgeSpawnEggItem(() -> ModEntities.SWAMPJAW, 0xFCFBED, 0x738552, defP()), "swampjaw_spawn_egg");
		
		if (MeetYourFight.loadedGunsWithoutRoses()) CompatGWRItems.registerItems(reg);
	}

	public static Item.Properties defP() {
		return new Item.Properties().tab(ItemGroupMeetYourFight.INSTANCE);
	}

	public static Item.Properties boss() {
		//Same rarity as Nether Star for boss drops
		return new Item.Properties().tab(ItemGroupMeetYourFight.INSTANCE).rarity(Rarity.UNCOMMON);
	}

	public static Item.Properties noStack() {
		return new Item.Properties().tab(ItemGroupMeetYourFight.INSTANCE).stacksTo(1);
	}

	public static Item.Properties bossNS() {
		//Same rarity as Nether Star for boss drops
		return new Item.Properties().tab(ItemGroupMeetYourFight.INSTANCE).stacksTo(1).rarity(Rarity.UNCOMMON);
	}

	public static <I extends Item> I initItem(IForgeRegistry<Item> reg, I item, String name) {
		item.setRegistryName(MeetYourFight.MODID, name);
		reg.register(item);
		return item;
	}
}
