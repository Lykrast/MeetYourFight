package lykrast.meetyourfight.registry;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.*;
import lykrast.meetyourfight.item.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.RecordItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
	public static final DeferredRegister<Item> REG = DeferredRegister.create(ForgeRegistries.ITEMS, MeetYourFight.MODID);
	public static RegistryObject<Item> hauntedBell, phantoplasm, passagesToll, spectresEye, spectresGrasp, aetherGlazedCupcake;
	public static RegistryObject<Item> devilsAnte, fortunesFavor, slicersDice, aceOfIron, cocktailCutlass, velvetFortune;
	public static RegistryObject<Item> fossilBait, mossyTooth, boneRaker, depthStar, cagedHeart, marshyDelight;
	public static RegistryObject<Item> duskKey, violetBloom, twilightsThorn, wiltedIdeals, blossomingMind, tombPlanter;
	public static RegistryObject<Item> discMagnum, discFrogPunch;
	public static RegistryObject<Item> eggBellringer, eggDameFortuna, eggSwampjaw, eggRosalyne, eggVela;
	
	static {
		hauntedBell = REG.register("haunted_bell", () -> new SummonItem(noStack(), BellringerEntity::spawn));
		phantoplasm = REG.register("phantoplasm", () -> new Item(boss()));
		passagesToll = REG.register("passages_toll", () -> new PassagesToll(bossNS()));
		spectresEye = REG.register("spectres_eye", () -> new SpectresEye(bossNS()));
		spectresGrasp = REG.register("spectres_grasp", () -> new SpectresGrasp(bossNS()));
		aetherGlazedCupcake = REG.register("aether_glazed_cupcake", () -> new Item(boss().food((new FoodProperties.Builder().nutrition(5).saturationMod(0.6f).alwaysEat().effect(() -> new MobEffectInstance(MobEffects.LEVITATION, 5*20), 1).build()))));
		
		devilsAnte = REG.register("devils_ante", () -> new SummonItem(noStack(), DameFortunaEntity::spawn));
		fortunesFavor = REG.register("fortunes_favor", () -> new Item(boss()));
		slicersDice = REG.register("slicers_dice", () -> new LuckCurio(bossNS()));
		aceOfIron = REG.register("ace_of_iron", () -> new LuckCurio(bossNS()));
		cocktailCutlass = REG.register("cocktail_cutlass", () -> new CocktailCutlass(bossNS()));
		velvetFortune = REG.register("velvet_fortune", () -> new Item(boss().food((new FoodProperties.Builder().nutrition(2).saturationMod(0.1f).alwaysEat().effect(() -> new MobEffectInstance(MobEffects.LUCK, 10*60*20), 1).build()))));
		
		fossilBait = REG.register("fossil_bait", () -> new SummonItem(noStack(), SwampjawEntity::spawn));
		mossyTooth = REG.register("mossy_tooth", () -> new Item(boss()));
		boneRaker = REG.register("bone_raker", () -> new BoneRaker(bossNS()));
		depthStar = REG.register("depth_star", () -> new DepthStar(bossNS()));
		cagedHeart = REG.register("caged_heart", () -> new CurioBaseItem(bossNS(), true));
		marshyDelight = REG.register("marshy_delight", () -> new Item(boss().food((new FoodProperties.Builder().nutrition(14).saturationMod(0.9f).meat().build()))));
		
		duskKey = REG.register("dusk_key", () -> new SummonItem(noStack(), RosalyneEntity::spawn));
		violetBloom = REG.register("violet_bloom", () -> new Item(boss()));
		twilightsThorn = REG.register("twilights_thorn", () -> new TwilightsThorn(bossNS()));
		wiltedIdeals = REG.register("wilted_ideals", () -> new WiltedIdeals(bossNS()));
		blossomingMind = REG.register("blossoming_mind", () -> new CurioBaseItem(bossNS(), true));
		tombPlanter = REG.register("tomb_planter", () -> new CurioBaseItem(bossNS(), true));
		
		//Lasts 1:30.92
		discMagnum = REG.register("music_disc_magnum", () -> new RecordItem(1, ModSounds.musicMagnum, disc(), 1818));
		//Lasts 3:40.44
		discFrogPunch = REG.register("music_disc_frogpunch", () -> new RecordItem(1, ModSounds.musicFrogPunch, disc(), 4408));
		
		eggBellringer = REG.register("bellringer_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.BELLRINGER, 0x560080, 0xDFFFF9, defP()));
		eggDameFortuna = REG.register("dame_fortuna_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.DAME_FORTUNA, 0xFE0000, 0xEEEEEE, defP()));
		eggSwampjaw = REG.register("swampjaw_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.SWAMPJAW, 0xFCFBED, 0x738552, defP()));
		//TODO Rose
		eggRosalyne = REG.register("rosalyne_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.ROSALYNE, 0xEBEBEB, 0xD78EFF, defP()));
		//REG.register("rose_spirit_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.ROSE_SPIRIT, 0xFF0000, 0xD3ECF1, defP()));
		//TODO Vela
		//eggVela = REG.register("vela_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.VELA, 0xFFF9F5, 0xD3ECF1, defP()));
		
		if (MeetYourFight.loadedGunsWithoutRoses()) CompatGWRItems.registerItems();
	}

	public static Item.Properties defP() {
		return new Item.Properties().tab(ItemGroupMeetYourFight.INSTANCE);
	}

	public static Item.Properties boss() {
		//Same rarity as Nether Star for boss drops
		return new Item.Properties().tab(ItemGroupMeetYourFight.INSTANCE).rarity(Rarity.UNCOMMON).fireResistant();
	}

	public static Item.Properties noStack() {
		return new Item.Properties().tab(ItemGroupMeetYourFight.INSTANCE).stacksTo(1);
	}

	public static Item.Properties bossNS() {
		//Same rarity as Nether Star for boss drops
		return new Item.Properties().tab(ItemGroupMeetYourFight.INSTANCE).stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant();
	}

	public static Item.Properties disc() {
		return noStack().rarity(Rarity.RARE).fireResistant();
	}
}
