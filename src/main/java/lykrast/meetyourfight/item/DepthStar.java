package lykrast.meetyourfight.item;

import java.util.List;

import lykrast.meetyourfight.registry.ModItems;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

public class DepthStar extends SwordItem {
	private static final Tier TIER = new CustomTier(2, 693, 6, 2, 14, () -> Ingredient.of(ModItems.mossyTooth));

	public DepthStar(Properties builderIn) {
		super(TIER, 6, -3.1f, builderIn);
	}

	@Override
	public void releaseUsing(ItemStack stack, Level world, LivingEntity entityLiving, int timeLeft) {
		if (entityLiving instanceof Player) {
			Player player = (Player) entityLiving;
			float strength = getShockwaveStrength(getUseDuration(stack) - timeLeft);
			if (strength >= 0.25) {
				if (!world.isClientSide) {
					Vec3 start = new Vec3(player.getX(), player.getEyeY(), player.getZ());
					Vec3 end = player.getLookAngle().scale(2).add(start);
					BlockHitResult raytrace = world.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
					if (raytrace.getType() != HitResult.Type.MISS) end = raytrace.getLocation();

					world.explode(player, end.x, end.y, end.z, strength * 1.5f, Explosion.BlockInteraction.NONE);

					stack.hurtAndBreak(2, player, (entity) -> entity.broadcastBreakEvent(player.getUsedItemHand()));
				}
				
				player.causeFoodExhaustion(strength);
				world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1, 0.8F + strength * 0.5F);
			}
		}
	}

	private float getShockwaveStrength(int charge) {
		float str = charge / 20.0F;
		if (str > 1) str = 1;
		str = (str * str + str * 2.0F) / 3.0F;
		return str;
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000;
	}
	
	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		playerIn.startUsingItem(handIn);
		return InteractionResultHolder.consume(playerIn.getItemInHand(handIn));
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(new TranslatableComponent(getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
	}

}
