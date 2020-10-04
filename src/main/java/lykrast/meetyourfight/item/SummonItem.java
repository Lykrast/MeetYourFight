package lykrast.meetyourfight.item;

import java.util.List;
import java.util.Random;

import lykrast.meetyourfight.entity.BellringerEntity;
import lykrast.meetyourfight.registry.ModEntities;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class SummonItem extends Item {

	public SummonItem(Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, LivingEntity entityLiving) {
		if (!(entityLiving instanceof PlayerEntity)) return stack;
		PlayerEntity player = (PlayerEntity)entityLiving;
		if (!world.isRemote) {
			if (!world.getLoadedEntitiesWithinAABB(MobEntity.class, entityLiving.getBoundingBox().grow(32), e -> !e.isNonBoss() && e.isAlive()).isEmpty()) {
				player.sendStatusMessage(new TranslationTextComponent("status.meetyourfight.boss_nearby"), true);
				return stack;
			}

			//TODO abstract to reuse for other bosses, for now just using that for bellringer
			Random rand = player.getRNG();
			BellringerEntity bellringer = ModEntities.BELLRINGER.create(world);
			bellringer.setLocationAndAngles(entityLiving.getPosX() + rand.nextInt(15) - 7, entityLiving.getPosY() + rand.nextInt(9) - 1, entityLiving.getPosZ() + rand.nextInt(15) - 7, rand.nextFloat() * 360 - 180, 0);
			bellringer.attackCooldown = 100;
			if (!player.abilities.isCreativeMode) bellringer.setAttackTarget(entityLiving);
			bellringer.addPotionEffect(new EffectInstance(Effects.RESISTANCE, 100, 2));
			
			bellringer.onInitialSpawn((ServerWorld) world, world.getDifficultyForLocation(bellringer.getPosition()), SpawnReason.EVENT, null, null);
			world.addEntity(bellringer);
			
			if (!player.abilities.isCreativeMode) {
				stack.shrink(1);
				entityLiving.sendBreakAnimation(entityLiving.getActiveHand());
			}
			world.playSound(null, entityLiving.getPosX(), entityLiving.getPosY(), entityLiving.getPosZ(), SoundEvents.BLOCK_BELL_USE, SoundCategory.PLAYERS, 2, 1);
			player.addStat(Stats.ITEM_USED.get(this));
		}
		return stack;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
		player.setActiveHand(hand);
		return ActionResult.resultConsume(player.getHeldItem(hand));
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 20;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BOW;
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		tooltip.add(new TranslationTextComponent(getTranslationKey() + ".desc").mergeStyle(TextFormatting.GRAY));
	}

}
