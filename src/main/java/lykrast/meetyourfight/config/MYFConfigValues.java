package lykrast.meetyourfight.config;

import net.minecraftforge.fml.config.ModConfig;

public class MYFConfigValues {
	//Swampjaw
	public static double CAGED_HEART_TRESHOLD = 1.0 / 4, CAGED_HEART_MULT = 0.5;
	//Bellringer
	public static int SPECTRES_EYE_RANGE = 32, PASSAGES_TOLL_RANGE = 16;
	//Dame Fortuna
	public static double SLICER_DICE_MULT = 2;
	//Those are mostly for the tooltip, cause if luck is not 0 then the formula kicks in
	public static double SLICER_DICE_CHANCE = 1.0 / 6, ACE_OF_IRON_CHANCE = 1.0 / 6,
			COCKTAIL_CUTLASS_CHANCE = 1.0 / 5, COCKTAIL_SHOTGUN_CHANCE = 1.0 / 3;
	public static boolean SLICER_DICE_LUCK = true, ACE_OF_IRON_LUCK = true, COCKTAIL_CUTLASS_LUCK = true, COCKTAIL_SHOTGUN_LUCK = true;
	//Rosalyne
	public static double WILTED_IDEALS_MULT = 1.5;
	public static double BLOSSOMING_MIND_BONUS = 1;
	public static int BLOSSOMING_MIND_CAP = 10;

	public static int percent(double percent) {
		return (int)(100.0*percent);
	}
	
	public static void refresh(ModConfig config) {
		CAGED_HEART_TRESHOLD = MYFConfig.COMMON.cagedHeartTreshold.get();
		CAGED_HEART_MULT = MYFConfig.COMMON.cagedHeartMultiplier.get().floatValue();
		
		SPECTRES_EYE_RANGE = MYFConfig.COMMON.spectresEyeRange.get();
		PASSAGES_TOLL_RANGE = MYFConfig.COMMON.passagesTollRange.get();
		
		SLICER_DICE_CHANCE = MYFConfig.COMMON.slicerDiceChance.get();
		SLICER_DICE_MULT = MYFConfig.COMMON.slicerDiceMultiplier.get();
		SLICER_DICE_LUCK = MYFConfig.COMMON.slicerDiceLuck.get();
		ACE_OF_IRON_CHANCE = MYFConfig.COMMON.aceOfIronChance.get();
		ACE_OF_IRON_LUCK = MYFConfig.COMMON.aceOfIronLuck.get();
		COCKTAIL_CUTLASS_CHANCE = MYFConfig.COMMON.cocktailCutlassChance.get();
		COCKTAIL_CUTLASS_LUCK = MYFConfig.COMMON.cocktailCutlassLuck.get();
		COCKTAIL_SHOTGUN_CHANCE = MYFConfig.COMMON.jagershotChance.get();
		COCKTAIL_SHOTGUN_LUCK = MYFConfig.COMMON.jagershotLuck.get();
		
		WILTED_IDEALS_MULT = MYFConfig.COMMON.wiltedIdealsMultiplier.get();
		BLOSSOMING_MIND_BONUS = MYFConfig.COMMON.blossomingMindBonus.get();
		BLOSSOMING_MIND_CAP = MYFConfig.COMMON.blossomingMindCap.get();
	}
}
