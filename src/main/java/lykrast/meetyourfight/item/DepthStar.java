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
	private static final IItemTier TIER = new CustomTier(2, 693, 6, 2, 14, () -> Ingredient.fromItems(ModItems.mossyTooth));

	public DepthStar(Properties builderIn) {
		super(TIER, 6, -3.1f, builderIn);
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, LivingEntity entityLiving, int timeLeft) {
		if (entityLiving instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) entityLiving;
			float strength = getShockwaveStrength(getUseDuration(stack) - timeLeft);
			if (strength >= 0.25) {
				if (!world.isRemote) {
					Vector3d start = new Vector3d(player.getPosX(), player.getPosYEye(), player.getPosZ());
					Vector3d end = player.getLookVec().scale(2).add(start);
					BlockRayTraceResult raytrace = world.rayTraceBlocks(new RayTraceContext(start, end, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, player));
					if (raytrace.getType() != RayTraceResult.Type.MISS) end = raytrace.getHitVec();

					world.createExplosion(player, end.x, end.y, end.z, strength * 1.5f, Explosion.Mode.NONE);

					stack.damageItem(2, player, (entity) -> entity.sendBreakAnimation(player.getActiveHand()));
				}
				
				player.addExhaustion(strength);
				world.playSound(null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1, 0.8F + strength * 0.5F);
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
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BOW;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		playerIn.setActiveHand(handIn);
		return ActionResult.resultConsume(playerIn.getHeldItem(handIn));
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		tooltip.add(new TranslationTextComponent(getTranslationKey() + ".desc").mergeStyle(TextFormatting.GRAY));
	}

}
