package lykrast.meetyourfight.misc;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.registry.ModItems;
import lykrast.meetyourfight.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = MeetYourFight.MODID)
public class EventHandler {
	@SubscribeEvent
	public static void entityDamage(final LivingHurtEvent event) {
		//Full damage prevention
		LivingEntity attacked = event.getEntity();
		if (attacked instanceof Player) {
			Player pattacked = (Player) attacked;
			//Ace of Iron
			if (!event.isCanceled() && !event.getSource().isBypassInvul() && CuriosApi.getCuriosHelper().findFirstCurio(pattacked, ModItems.aceOfIron.get()).isPresent()) {
				float luck = pattacked.getLuck();
				double chance = 1.0 / 6.0;
				if (luck >= 0) chance = (1.0 + luck) / (6.0 + 2 * luck);
				else chance = 1.0 / (6.0 - 3 * luck);
				if (pattacked.getRandom().nextDouble() <= chance) {
					event.setCanceled(true);
					pattacked.level.playSound(null, attacked.blockPosition(), ModSounds.aceOfIronProc.get(), SoundSource.PLAYERS, 1, 1);
				}
			}
		}

		//No need to further modify damage if the damage is cancelled
		if (event.isCanceled()) return;

		//Damage increases
		Entity attacker = event.getSource().getEntity();
		if (attacker != null && attacker instanceof Player) {
			Player pattacker = (Player) attacker;
			//Slicer's Dice
			if (CuriosApi.getCuriosHelper().findFirstCurio(pattacker, ModItems.slicersDice.get()).isPresent()) {
				float luck = pattacker.getLuck();
				double chance = 0.2;
				if (luck >= 0) chance = (1.0 + luck) / (5.0 + luck);
				else chance = 1.0 / (5.0 - 3 * luck);
				if (pattacker.getRandom().nextDouble() <= chance) {
					event.setAmount(event.getAmount() * 2);
					pattacker.level.playSound(null, attacked.blockPosition(), ModSounds.slicersDiceProc.get(), SoundSource.PLAYERS, 1, 1);
					((ServerLevel) pattacker.level).sendParticles(ParticleTypes.CRIT, attacked.getX(), attacked.getEyeY(), attacked.getZ(), 15, 0.2, 0.2, 0.2, 0);
				}
			}
			//Wilted Ideals
			if (CuriosApi.getCuriosHelper().findFirstCurio(pattacker, ModItems.wiltedIdeals.get()).isPresent()) {
				event.setAmount(event.getAmount() * 1.5f);
			}
		}

		//Damage decreases
		if (attacked instanceof Player) {
			Player pattacked = (Player) attacked;
			//Caged Heart
			if (CuriosApi.getCuriosHelper().findFirstCurio(pattacked, ModItems.cagedHeart.get()).isPresent()) {
				float treshold = pattacked.getMaxHealth() / 4.0f;
				if (event.getAmount() > treshold) {
					event.setAmount((event.getAmount() - treshold) * 0.5f + treshold);
					pattacked.level.playSound(null, attacked.blockPosition(), ModSounds.cagedHeartProc.get(), SoundSource.PLAYERS, 1, 1);
				}
			}
		}
	}

	@SubscribeEvent
	public static void entityDeath(final LivingDeathEvent event) {
		if (event.isCanceled()) return;
		Entity killer = event.getSource().getEntity();
		if (killer != null && killer instanceof Player) {
			Player pkiller = (Player) killer;
			LivingEntity killed = event.getEntity();
			//Tomb Planter
			if (CuriosApi.getCuriosHelper().findFirstCurio(pkiller, ModItems.tombPlanter.get()).isPresent()) {
				Level lvl = killed.level;
				BlockPos pos = killed.blockPosition();
				//Only works if the target is at most 1 block above bonemealabe ground
				if (!lvl.isEmptyBlock(pos) || !lvl.isEmptyBlock(pos = pos.below())) {
					//Dummy stack just in case one of the events expected actual bonemeal
					ItemStack dummy = new ItemStack(Items.BONE_MEAL);
					//Mostly copied from villagers' UseBonemeal
					if (BoneMealItem.applyBonemeal(dummy, lvl, pos, pkiller) || BoneMealItem.growWaterPlant(dummy, lvl, pos, null)) {
						if (!lvl.isClientSide) lvl.levelEvent(1505, pos, 0);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void livingExperienceDrop(LivingExperienceDropEvent event) {
		if (event.isCanceled()) return;
		Player killer = event.getAttackingPlayer();
		if (killer != null) {
			//Blossoming Mind
			if (event.getOriginalExperience() >= 2 && CuriosApi.getCuriosHelper().findFirstCurio(killer, ModItems.blossomingMind.get()).isPresent()) {
				//Ensorcellation Insight I is 1-5 per kill and Insight III 3-15
				//A monster is 5, blaze 10, ravager 20
				//50% to 150% bonus, cap at 15 bonus cause I don't want it to be optimal on bosses
				int amt = Math.min(event.getOriginalExperience() / 2, 5);
				event.setDroppedExperience(event.getDroppedExperience() + amt + killer.level.random.nextInt(amt+1) + killer.level.random.nextInt(amt+1));
			}
		}
	}
}
