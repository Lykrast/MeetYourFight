package lykrast.meetyourfight.config;

import lykrast.meetyourfight.entity.BellringerEntity;
import lykrast.meetyourfight.entity.DameFortunaEntity;
import lykrast.meetyourfight.entity.RosalyneEntity;
import lykrast.meetyourfight.entity.RoseSpiritEntity;
import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraftforge.fml.config.ModConfig;

public class MYFConfigValues {
	//stats
	public static boolean SWAMPJAW_CHANGED = false, BELLRINGER_CHANGED = false, FORTUNA_CHANGED = false, ROSALYNE_CHANGED = false;
	public static double SWAMPJAW_HEALTH_MOD = 0, SWAMPJAW_DMG_MOD = 0, SWAMPJAW_EXPLOSION = 2.5;
	public static double BELLRINGER_HEALTH_MOD = 0, BELLRINGER_DMG_MULT = 1;
	public static double FORTUNA_HEALTH_MOD = 0, FORTUNA_DMG_MULT = 1;
	public static double ROSALYNE_HEALTH_MOD = 0, ROSE_SPIRIT_HEALTH_MOD = 0, ROSALYNE_MELEE_MOD = 0, ROSALYNE_PROJECTILE_MULT = 1;
	//items
	//Swampjaw
	public static double CAGED_HEART_TRESHOLD = 1.0 / 4, CAGED_HEART_MULT = 0.5, BONE_RAKER_BONUS = 2;
	//Bellringer
	public static int SPECTRES_EYE_RANGE = 32, PASSAGES_TOLL_RANGE = 16;
	//Dame Fortuna
	public static double SLICER_DICE_MULT = 2;
	//Those are mostly for the tooltip, cause if luck is not 0 then the formula kicks in
	public static double SLICER_DICE_CHANCE = 1.0 / 6, ACE_OF_IRON_CHANCE = 1.0 / 6,
			COCKTAIL_CUTLASS_CHANCE = 1.0 / 5, COCKTAIL_SHOTGUN_CHANCE = 1.0 / 3;
	public static boolean SLICER_DICE_LUCK = true, ACE_OF_IRON_LUCK = true, COCKTAIL_CUTLASS_LUCK = true, COCKTAIL_SHOTGUN_LUCK = true;
	//Rosalyne
	public static double WILTED_IDEALS_MULT = 1.5, WILTED_IDEALS_PENALTY = -0.5;
	public static double BLOSSOMING_MIND_BONUS = 1;
	public static int BLOSSOMING_MIND_CAP = 10;

	public static int percent(double percent) {
		return (int)(100.0*percent);
	}
	
	public static void refresh(ModConfig config) {
		SWAMPJAW_HEALTH_MOD = SwampjawEntity.HP*MYFConfig.COMMON.swampjawHealth.get() - SwampjawEntity.HP;
		SWAMPJAW_DMG_MOD = SwampjawEntity.DMG_CHARGE*MYFConfig.COMMON.swampjawMelee.get() - SwampjawEntity.DMG_CHARGE;
		SWAMPJAW_EXPLOSION = MYFConfig.COMMON.swampjawExplosion.get();
		if (Math.abs(SWAMPJAW_HEALTH_MOD) >= 1 || Math.abs(SWAMPJAW_DMG_MOD) >= 1 || Math.abs(2.5 - SWAMPJAW_EXPLOSION) >= 0.1) SWAMPJAW_CHANGED = true;
		else SWAMPJAW_CHANGED = false;
		
		BELLRINGER_HEALTH_MOD = BellringerEntity.HP*MYFConfig.COMMON.bellringerHealth.get() - BellringerEntity.HP;
		BELLRINGER_DMG_MULT = MYFConfig.COMMON.bellringerDamage.get();
		if (Math.abs(BELLRINGER_HEALTH_MOD) >= 1 || Math.abs(1 - BELLRINGER_DMG_MULT) >= 0.1) BELLRINGER_CHANGED = true;
		else BELLRINGER_CHANGED = false;
		
		FORTUNA_HEALTH_MOD = DameFortunaEntity.HP*MYFConfig.COMMON.fortunaHealth.get() - DameFortunaEntity.HP;
		FORTUNA_DMG_MULT = MYFConfig.COMMON.fortunaDamage.get();
		if (Math.abs(FORTUNA_HEALTH_MOD) >= 1 || Math.abs(1 - FORTUNA_DMG_MULT) >= 0.1) FORTUNA_CHANGED = true;
		else FORTUNA_CHANGED = false;
		
		ROSALYNE_HEALTH_MOD = RosalyneEntity.HP*MYFConfig.COMMON.rosalyneHealth.get() - RosalyneEntity.HP;
		ROSE_SPIRIT_HEALTH_MOD = RoseSpiritEntity.HP*MYFConfig.COMMON.rosalyneHealth.get() - RoseSpiritEntity.HP;
		ROSALYNE_MELEE_MOD = RosalyneEntity.DMG*MYFConfig.COMMON.rosalyneMelee.get() - RosalyneEntity.DMG;
		ROSALYNE_PROJECTILE_MULT = MYFConfig.COMMON.rosalyneProjectile.get();
		if (Math.abs(ROSALYNE_HEALTH_MOD) >= 1 || Math.abs(ROSE_SPIRIT_HEALTH_MOD) >= 1 || Math.abs(ROSALYNE_MELEE_MOD) >= 1 || Math.abs(1 - ROSALYNE_PROJECTILE_MULT) >= 0.1) ROSALYNE_CHANGED = true;
		else ROSALYNE_CHANGED = false;
		
		CAGED_HEART_TRESHOLD = MYFConfig.COMMON.cagedHeartTreshold.get();
		CAGED_HEART_MULT = MYFConfig.COMMON.cagedHeartMultiplier.get().floatValue();
		BONE_RAKER_BONUS = MYFConfig.COMMON.boneRakerBonus.get();
		
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
		WILTED_IDEALS_PENALTY = -MYFConfig.COMMON.wiltedIdealsPenalty.get(); //minus for the attribute to be good
		BLOSSOMING_MIND_BONUS = MYFConfig.COMMON.blossomingMindBonus.get();
		BLOSSOMING_MIND_CAP = MYFConfig.COMMON.blossomingMindCap.get();
	}
}
