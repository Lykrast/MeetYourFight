package lykrast.meetyourfight.misc;

public class MYFConstants {
	//Bellringer
	public static final int SPECTRES_EYE_RANGE = 32, PASSAGES_TOLL_RANGE = 16;
	//Dame Fortuna
	public static final float SLICER_DICE_MULT = 2;
	//Those are mostly for the tooltip, cause if luck is not 0 then the formula kicks in
	public static final double SLICER_DICE_CHANCE = 1.0 / 6, ACE_OF_IRON_CHANCE = 1.0 / 6,
			COCKTAIL_CUTLASS_CHANCE = 1.0 / 5, COCKTAIL_SHOTGUN_CHANCE = 1.0 / 3;
	//Swampjaw
	public static final float CAGED_HEART_TRESHOLD = 1f / 4, CAGED_HEART_MULT = 0.5f;
	//Rosalyne
	public static final float WILTED_IDEALS_MULT = 1.5f;
	//This one is just for the tooltip cause of the formula (it's random between 50 to 150% bonus, biasing towards the center)
	public static final int BLOSSOMING_MIND_INCREASE = 100;

	public static int percent(double percent) {
		return (int)(100.0*percent);
	}
}
