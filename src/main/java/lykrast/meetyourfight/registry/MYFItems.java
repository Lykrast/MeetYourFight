package lykrast.meetyourfight.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.*;
import lykrast.meetyourfight.item.*;
import lykrast.meetyourfight.misc.MYFConstants;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

public class MYFItems {
	public static final DeferredRegister<Item> REG = DeferredRegister.create(ForgeRegistries.ITEMS, MeetYourFight.MODID);
	public static RegistryObject<Item> fossilBait, mossyTooth, boneRaker, depthStar, cagedHeart, marshyDelight;
	public static RegistryObject<Item> hauntedBell, phantoplasm, passagesToll, spectresEye, spectresGrasp, aetherGlazedCupcake;
	public static RegistryObject<Item> devilsAnte, fortunesFavor, slicersDice, aceOfIron, cocktailCutlass, velvetFortune;
	public static RegistryObject<Item> duskKey, violetBloom, twilightsThorn, wiltedIdeals, blossomingMind, tombPlanter, petalCream;
	public static RegistryObject<Item> discSwampjaw, discBellringer, discRosalyne;
	public static RegistryObject<Item> headSwampjaw, headBellringer, headFortuna, headRosalyne, headRosalyneCracked;
	public static RegistryObject<Item> eggSwampjaw, eggBellringer, eggDameFortuna, eggRosalyne;
	
	private static List<RegistryObject<? extends Item>> orderedItemsCreative = new ArrayList<>();
	
	public static void makeCreativeTab(RegisterEvent event) {
		event.register(Registries.CREATIVE_MODE_TAB, helper -> {
			helper.register(ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(MeetYourFight.MODID, "meetyourfight")),
					CreativeModeTab.builder()
					.title(Component.translatable("itemGroup.meetyourfight"))
					.icon(() -> new ItemStack(hauntedBell.get()))
					.displayItems((parameters, output) -> orderedItemsCreative.forEach(i -> output.accept(i.get())))
					.build());
		});
	}
	
	static {
		fossilBait = initItem("fossil_bait", () -> new SummonItem(noStack(), SwampjawEntity::spawn));
		mossyTooth = initItem("mossy_tooth", () -> new Item(boss()));
		boneRaker = initItem("bone_raker", () -> new BoneRaker(bossNS()));
		depthStar = initItem("depth_star", () -> new DepthStar(bossNS()));
		cagedHeart = initItem("caged_heart", () -> new CurioBaseItem(bossNS(), true, MYFConstants.percent(MYFConstants.CAGED_HEART_TRESHOLD), MYFConstants.percent(1 - MYFConstants.CAGED_HEART_MULT)));
		marshyDelight = initItem("marshy_delight", () -> new Item(boss().food((new FoodProperties.Builder().nutrition(14).saturationMod(0.9f).meat().build()))));
		
		hauntedBell = initItem("haunted_bell", () -> new SummonItem(noStack(), BellringerEntity::spawn));
		phantoplasm = initItem("phantoplasm", () -> new Item(boss()));
		passagesToll = initItem("passages_toll", () -> new PassagesToll(bossNS()));
		spectresEye = initItem("spectres_eye", () -> new SpectresEye(bossNS()));
		spectresGrasp = initItem("spectres_grasp", () -> new SpectresGrasp(bossNS()));
		aetherGlazedCupcake = initItem("aether_glazed_cupcake", () -> new Item(boss().food((new FoodProperties.Builder().nutrition(5).saturationMod(0.6f).alwaysEat().effect(() -> new MobEffectInstance(MobEffects.LEVITATION, 5*20), 1).build()))));
		
		devilsAnte = initItem("devils_ante", () -> new SummonItem(noStack(), DameFortunaEntity::spawn));
		fortunesFavor = initItem("fortunes_favor", () -> new Item(boss()));
		slicersDice = initItem("slicers_dice", () -> new LuckCurio(bossNS(), MYFConstants.percent(MYFConstants.SLICER_DICE_CHANCE), MYFConstants.percent(MYFConstants.SLICER_DICE_MULT - 1)));
		aceOfIron = initItem("ace_of_iron", () -> new LuckCurio(bossNS(), MYFConstants.percent(MYFConstants.ACE_OF_IRON_CHANCE)));
		cocktailCutlass = initItem("cocktail_cutlass", () -> new CocktailCutlass(bossNS()));
		velvetFortune = initItem("velvet_fortune", () -> new Item(boss().food((new FoodProperties.Builder().nutrition(2).saturationMod(0.1f).alwaysEat().effect(() -> new MobEffectInstance(MobEffects.LUCK, 10*60*20), 1).build()))));
		
		duskKey = initItem("dusk_key", () -> new SummonItem(noStack(), RosalyneEntity::spawn));
		violetBloom = initItem("violet_bloom", () -> new Item(boss()));
		twilightsThorn = initItem("twilights_thorn", () -> new TwilightsThorn(bossNS()));
		wiltedIdeals = initItem("wilted_ideals", () -> new WiltedIdeals(bossNS()));
		blossomingMind = initItem("blossoming_mind", () -> new CurioBaseItem(bossNS(), true, MYFConstants.BLOSSOMING_MIND_INCREASE));
		tombPlanter = initItem("tomb_planter", () -> new CurioBaseItem(bossNS(), true));
		petalCream = initItem("petal_cream", () -> new Item(boss().food((new FoodProperties.Builder().nutrition(4).saturationMod(0.8f).build()))));

		//Lasts 1:24.52
		discSwampjaw = initItem("music_disc_swampjaw", () -> new RecordItem(1, MYFSounds.musicShadows, disc(), 1691));
		//Lasts 1:30.92
		discBellringer = initItem("music_disc_bellringer", () -> new RecordItem(1, MYFSounds.musicMagnum, disc(), 1818));
		//Lasts 3:40.44
		discRosalyne = initItem("music_disc_rosalyne", () -> new RecordItem(1, MYFSounds.musicFrogPunch, disc(), 4408));

		headSwampjaw = initItem("swampjaw_head", skull(MYFBlocks.swampjawHead, MYFBlocks.swampjawHeadWall));
		headBellringer = initItem("bellringer_head", skull(MYFBlocks.bellringerHead, MYFBlocks.bellringerHeadWall));
		headFortuna = initItem("dame_fortuna_head", skull(MYFBlocks.fortunaHead, MYFBlocks.fortunaHeadWall));
		headRosalyne = initItem("rosalyne_head", skull(MYFBlocks.rosalyneHead, MYFBlocks.rosalyneHeadWall));
		headRosalyneCracked = initItem("rosalyne_head_cracked", skull(MYFBlocks.rosalyneCracked, MYFBlocks.rosalyneCrackedWall));

		eggSwampjaw = initItem("swampjaw_spawn_egg", () -> new ForgeSpawnEggItem(MYFEntities.SWAMPJAW, 0xFCFBED, 0x738552, defP()));
		eggBellringer = initItem("bellringer_spawn_egg", () -> new ForgeSpawnEggItem(MYFEntities.BELLRINGER, 0x560080, 0xDFFFF9, defP()));
		eggDameFortuna = initItem("dame_fortuna_spawn_egg", () -> new ForgeSpawnEggItem(MYFEntities.DAME_FORTUNA, 0xFE0000, 0xEEEEEE, defP()));
		eggRosalyne = initItem("rosalyne_spawn_egg", () -> new ForgeSpawnEggItem(MYFEntities.ROSALYNE, 0xEBEBEB, 0xD78EFF, defP()));
		//initItem("rose_spirit_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.ROSE_SPIRIT, 0xFF0000, 0xD3ECF1, defP()));
		
		if (MeetYourFight.loadedGunsWithoutRoses()) CompatGWRItems.registerItems();
	}

	public static Item.Properties defP() {
		return new Item.Properties();
	}

	public static Item.Properties boss() {
		//Same rarity as Nether Star for boss drops
		return new Item.Properties().rarity(Rarity.UNCOMMON).fireResistant();
	}

	public static Item.Properties noStack() {
		return new Item.Properties().stacksTo(1);
	}

	public static Item.Properties bossNS() {
		//Same rarity as Nether Star for boss drops
		return new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant();
	}

	public static Item.Properties disc() {
		return noStack().rarity(Rarity.RARE).fireResistant();
	}
	
	public static Supplier<Item> skull(RegistryObject<Block> normal, RegistryObject<Block> wall) {
		return () -> new StandingAndWallBlockItem(normal.get(), wall.get(), boss(), Direction.DOWN);
	}
	
	public static <I extends Item> RegistryObject<I> initItem(String name, Supplier<I> item) {
		RegistryObject<I> rego = REG.register(name, item);
		orderedItemsCreative.add(rego);
		return rego;
	}
}
