package lykrast.meetyourfight.item;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;

public class SummonItem extends Item {
	private BossSpawner spawner;
	private Supplier<Boolean> wasConfig;

	public SummonItem(Properties properties, BossSpawner spawner, Supplier<Boolean> wasConfig) {
		super(properties);
		this.spawner = spawner;
		this.wasConfig = wasConfig;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entityLiving) {
		if (!(entityLiving instanceof Player)) return stack;
		Player player = (Player)entityLiving;
		if (!world.isClientSide) {
			if (!world.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(32), e -> e.getType().is(Tags.EntityTypes.BOSSES) && e.isAlive()).isEmpty()) {
				player.displayClientMessage(Component.translatable("status.meetyourfight.boss_nearby"), true);
				return stack;
			}
			
			spawner.spawn(player, world);
			
			if (!player.getAbilities().instabuild) {
				stack.shrink(1);
				player.broadcastBreakEvent(player.getUsedItemHand());
			}
			player.awardStat(Stats.ITEM_USED.get(this));
		}
		return stack;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		player.startUsingItem(hand);
		return InteractionResultHolder.consume(player.getItemInHand(hand));
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 20;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}
	
	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("item.meetyourfight.summon.use").withStyle(ChatFormatting.GRAY));
		if (wasConfig.get()) tooltip.add(Component.translatable("item.meetyourfight.summon.configured").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		else tooltip.add(Component.translatable("item.meetyourfight.summon.gear", Component.translatable(getDescriptionId() + ".gear").withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.GOLD));
	}
	
	@FunctionalInterface
	public static interface BossSpawner {
		void spawn(Player player, Level world);
	}

}
