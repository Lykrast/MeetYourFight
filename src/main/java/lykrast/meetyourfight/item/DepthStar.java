package lykrast.meetyourfight.item;

import java.util.List;

import lykrast.meetyourfight.registry.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.UseAction;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class DepthStar extends SwordItem {
	private static final IItemTier TIER = new CustomTier(2, 693, 6, 2, 14, () -> Ingredient.of(ModItems.mossyTooth));

	public DepthStar(Properties builderIn) {
		super(TIER, 6, -3.1f, builderIn);
	}

	@Override
	public void releaseUsing(ItemStack stack, World world, LivingEntity entityLiving, int timeLeft) {
		if (entityLiving instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) entityLiving;
			float strength = getShockwaveStrength(getUseDuration(stack) - timeLeft);
			if (strength >= 0.25) {
				if (!world.isClientSide) {
					Vector3d start = new Vector3d(player.getX(), player.getEyeY(), player.getZ());
					Vector3d end = player.getLookAngle().scale(2).add(start);
					BlockRayTraceResult raytrace = world.clip(new RayTraceContext(start, end, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, player));
					if (raytrace.getType() != RayTraceResult.Type.MISS) end = raytrace.getLocation();

					world.explode(player, end.x, end.y, end.z, strength * 1.5f, Explosion.Mode.NONE);

					stack.hurtAndBreak(2, player, (entity) -> entity.broadcastBreakEvent(player.getUsedItemHand()));
				}
				
				player.causeFoodExhaustion(strength);
				world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1, 0.8F + strength * 0.5F);
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
	public UseAction getUseAnimation(ItemStack stack) {
		return UseAction.BOW;
	}

	@Override
	public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
		playerIn.startUsingItem(handIn);
		return ActionResult.consume(playerIn.getItemInHand(handIn));
	}

	@Override
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		tooltip.add(new TranslationTextComponent(getDescriptionId() + ".desc").withStyle(TextFormatting.GRAY));
	}

}
