package lykrast.meetyourfight.config;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public class MYFConfig {
	//just copying what I did for book wyrms
	public static final ForgeConfigSpec COMMON_SPEC;
	public static final MYFConfig COMMON;

	static {
		Pair<MYFConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(MYFConfig::new);
		COMMON = specPair.getLeft();
		COMMON_SPEC = specPair.getRight();
	}
	
	public final DoubleValue swampjawHealth, swampjawMelee, swampjawExplosion;
	public final DoubleValue bellringerHealth, bellringerDamage;
	public final DoubleValue fortunaHealth, fortunaDamage;
	public final DoubleValue rosalyneHealth, rosalyneMelee, rosalyneProjectile;
	
	public final DoubleValue cagedHeartTreshold, cagedHeartMultiplier, boneRakerBonus;
	public final IntValue spectresEyeRange, passagesTollRange;
	public final DoubleValue slicerDiceChance, slicerDiceMultiplier, aceOfIronChance, cocktailCutlassChance, jagershotChance;
	public final BooleanValue slicerDiceLuck, aceOfIronLuck, cocktailCutlassLuck, jagershotLuck;
	public final DoubleValue wiltedIdealsMultiplier, wiltedIdealsPenalty, blossomingMindBonus;
	public final IntValue blossomingMindCap;
	
	public MYFConfig(ForgeConfigSpec.Builder builder) {
		builder.comment("Boss stats");
		builder.comment("Changing any of these values will change the recommended gear in the summoning item's tooltip to a notice");
		builder.push("stats");
		builder.comment("Swampjaw");
		builder.push("swampjaw");
		swampjawHealth = doubleval(builder, "health", 1, 0.1, 1000, "Max health multiplier for Swampjaw", "eg 2 means x2 max health", "Values above ~10 require AttributeFix mod", "Will not apply to already spawned bosses");
		swampjawMelee = doubleval(builder, "melee", 1, 0.1, 1000, "Damage multiplier for Swampjaw's charge", "eg 2 means x2 damage", "Values above ~170 require AttributeFix mod", "Will not apply to already spawned bosses");
		swampjawExplosion = doubleval(builder, "explosion", 2.5, 0.1, 20, "Explosion strength for Swampjaw's mines", "eg ghast fireballs are 1, creepers are 3, tnt 4, charged creeper 6");
		builder.pop();
		builder.comment("Bellringer");
		builder.push("bellringer");
		bellringerHealth = doubleval(builder, "health", 1, 0.1, 1000, "Max health multiplier for Bellringer", "eg 2 means x2 max health", "Values above ~5 require AttributeFix mod", "Will not apply to already spawned bosses");
		bellringerDamage = doubleval(builder, "damage", 1, 0.1, 1000, "Damage multiplier for Bellringer's projectiles", "eg 2 means x2 damage");
		builder.pop();
		builder.comment("Dame Fortuna");
		builder.push("fortuna");
		fortunaHealth = doubleval(builder, "health", 1, 0.1, 1000, "Max health multiplier for Dame Fortuna", "eg 2 means x2 max health", "Values above ~3.4 require AttributeFix mod", "Will not apply to already spawned bosses");
		fortunaDamage = doubleval(builder, "damage", 1, 0.1, 1000, "Damage multiplier for Dame Fortuna's projectiles", "eg 2 means x2 damage");
		builder.pop();
		builder.comment("Rosalyne");
		builder.push("rosalyne");
		rosalyneHealth = doubleval(builder, "health", 1, 0.1, 1000, "Max health multiplier for Rosalyne and her spirits", "eg 2 means x2 max health", "Values above ~2 require AttributeFix mod", "Will not apply to already spawned bosses");
		rosalyneMelee = doubleval(builder, "melee", 1, 0.1, 1000, "Damage multiplier for Rosalyne's melee swings", "eg 2 means x2 damage", "Values above ~85 require AttributeFix mod", "Will not apply to already spawned bosses");
		rosalyneProjectile = doubleval(builder, "projectile", 1, 0.1, 1000, "Damage multiplier for Rosalyne's spirits' projectiles", "eg 2 means x2 damage");
		builder.pop();
		builder.pop();
		builder.comment("Boss items");
		builder.push("items");
		builder.comment("Swampjaw");
		builder.push("swampjaw");
		cagedHeartTreshold = doubleval(builder, "cagedHeartTreshold", 0.25, 0, 1, "Caged Heart will reduce damage over this fraction of max health", "eg 0.25 means it reduces damage over 25% max health");
		cagedHeartMultiplier = doubleval(builder, "cagedHeartMultiplier", 0.5, 0, 1, "Caged Heart's damage multiplier for damage above the treshold", "eg 0.25 multiplies damage by 0.25 = 75% reduction");
		boneRakerBonus = doubleval(builder, "boneRakerBonus", 2, 0, 100, "Bone Raker bonus attack damage", "Need to reequip or restart world to take effect");
		builder.pop();
		builder.comment("Bellringer");
		builder.push("bellringer");
		spectresEyeRange = intval(builder, "spectresEyeRange", 32, 1, 64, "Range of the Spectre's Eye in blocks");
		passagesTollRange = intval(builder, "passagesTollRange", 16, 1, 64, "Max range of the Passage's Toll in blocks");
		builder.pop();
		builder.comment("Dame Fortuna");
		builder.push("fortuna");
		slicerDiceChance = doubleval(builder, "slicerDiceChance", 1.0 / 6, 0.01, 1, "Chance that the Slicer's Dice increases damage", "eg 0.25 means 25% chance");
		slicerDiceMultiplier = doubleval(builder, "slicerDiceMultiplier", 2, 1, 100, "Damage multiplier of the Slicer's Dice", "eg 2 means x2 damage = +100% damage");
		slicerDiceLuck = boolval(builder, "slicerDiceLuck", true, "Whether the Slicer's Dice proc chance is affected by luck", "When positive luck, it's (1+luck)/((1/chance)+luck)", "When negative luck, it's 1/((1/chance)-3*luck)");
		aceOfIronChance = doubleval(builder, "aceOfIronChance", 1.0 / 6, 0.01, 1, "Chance that the Ace of Iron blocks damage", "eg 0.25 means 25% chance");
		aceOfIronLuck = boolval(builder, "aceOfIronLuck", true, "Whether the Ace of Iron proc chance is affected by luck", "There is a soft cap of 50% chance, so if the base chance exceeds that, positive luck won't do anything either way", "When positive luck, it's (1+luck)/((1/chance)+2*luck)", "When negative luck, it's 1/((1/chance)-3*luck)");
		cocktailCutlassChance = doubleval(builder, "cocktailCutlassChance", 1.0 / 5, 0.01, 1, "Chance that the Cocktail Cutlass gives a potion effect", "eg 0.25 means 25% chance");
		cocktailCutlassLuck = boolval(builder, "cocktailCutlassLuck", true, "Whether the Cocktail Cutlass proc chance is affected by luck", "When positive luck, it's (1+luck)/((1/chance)+0.5*luck)", "When negative luck, it's 1/((1/chance)-luck)");
		jagershotChance = doubleval(builder, "jagershotChance", 1.0 / 3, 0.01, 1, "Chance that the Jagershot (Guns Without Roses compat) shoots a splash potion", "eg 0.25 means 25% chance");
		jagershotLuck = boolval(builder, "jagershotLuck", true, "Whether the Jagershot proc chance is affected by luck", "When positive luck, it's (1+luck)/((1/chance)+0.5*luck)", "When negative luck, it's 1/((1/chance)-luck)");
		builder.pop();
		builder.comment("Rosalyne");
		builder.push("rosalyne");
		wiltedIdealsMultiplier = doubleval(builder, "wiltedIdealsMultiplier", 1.5, 1, 100, "Damage multiplier of the Wilted Ideals", "eg 2 means x2 damage = +100% damage");
		wiltedIdealsPenalty = doubleval(builder, "wiltedIdealsPenalty", 0.5, 0, 0.9, "Percent max health penalty from the Wilted Ideals", "eg 0.4 means -40% max health", "Need to reequip or restart world to take effect");
		blossomingMindBonus = doubleval(builder, "blossomingMindBonus", 1, 0, 100, "Average bonus experience (relative to the mob's xp drop) the Blossoming Mind gives", "eg 0.6 means adds an average of 0.6x the base xp drops (so +60%)");
		blossomingMindCap = intval(builder, "blossomingMindCap", 10, 1, 10000, "Maximum bonus experiece the Blossoming Mind can give per kill", "The actual amount can vary between 0.5 and 1.5 times that and still depends on the experience dropped by the monster", "I capped it so that it's not 'optimal' to use it for bosses");
		builder.pop();
		builder.pop();
	}
	
	private IntValue intval(ForgeConfigSpec.Builder builder, String name, int def, int min, int max, String... comments) {
		return builder.translation(name).comment(comments).comment("Default: " + def).defineInRange(name, def, min, max);
	}
	private DoubleValue doubleval(ForgeConfigSpec.Builder builder, String name, double def, double min, double max, String... comments) {
		return builder.translation(name).comment(comments).comment("Default: " + def).defineInRange(name, def, min, max);
	}
	private BooleanValue boolval(ForgeConfigSpec.Builder builder, String name, boolean def, String... comments) {
		return builder.translation(name).comment(comments).comment("Default: " + def).define(name, def);
	}

}
