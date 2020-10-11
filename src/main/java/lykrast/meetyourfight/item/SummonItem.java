package lykrast.meetyourfight.item;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class SummonItem extends Item {
	private BossSpawner spawner;

	public SummonItem(Properties properties, BossSpawner spawner) {
		super(properties);
		this.spawner = spawner;
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, LivingEntity entityLiving) {
		if (!(entityLiving instanceof PlayerEntity)) return stack;
		PlayerEntity player = (PlayerEntity)entityLiving;
		if (!world.isRemote) {
			if (!world.getLoadedEntitiesWithinAABB(MobEntity.class, player.getBoundingBox().grow(32), e -> !e.isNonBoss() && e.isAlive()).isEmpty()) {
				player.sendStatusMessage(new TranslationTextComponent("status.meetyourfight.boss_nearby"), true);
				return stack;
			}
			
			spawner.spawn(player, world);
			
			if (!player.abilities.isCreativeMode) {
				stack.shrink(1);
				player.sendBreakAnimation(player.getActiveHand());
			}
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
	
	@FunctionalInterface
	public static interface BossSpawner {
		void spawn(PlayerEntity player, World world);
	}

}
