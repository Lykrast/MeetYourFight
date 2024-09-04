package lykrast.meetyourfight.item;

import java.util.List;

import lykrast.meetyourfight.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class DepthStar extends SwordItem {
	private static final Tier TIER = new CustomTier(2, 693, 6, 0, 14, () -> Ingredient.of(ModItems.mossyTooth.get()));

	public DepthStar(Properties builderIn) {
		super(TIER, 9, -3.2f, builderIn);
	}

	@Override
	public void releaseUsing(ItemStack stack, Level world, LivingEntity entityLiving, int timeLeft) {
		if (entityLiving instanceof Player) {
			Player player = (Player) entityLiving;
			float strength = getShockwaveStrength(getUseDuration(stack) - timeLeft);
			if (strength >= 0.25) {
				Vec3 start = new Vec3(player.getX(), player.getEyeY(), player.getZ());
				Vec3 end = player.getLookAngle().scale(2).add(start);
				BlockHitResult raytrace = world.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
				if (raytrace.getType() != HitResult.Type.MISS) end = raytrace.getLocation();
				
				if (!world.isClientSide) {
					world.playSound(null, end.x, end.y, end.z, SoundEvents.GENERIC_EXPLODE, entityLiving.getSoundSource(), 1, (1 + (world.random.nextFloat() - world.random.nextFloat()) * 0.2F) * 0.7F);
					
					double damage = entityLiving.getAttributeValue(Attributes.ATTACK_DAMAGE);

					//world.explode(player, end.x, end.y, end.z, strength * 2, Explosion.BlockInteraction.NONE);
					for (LivingEntity ent : world.getEntitiesOfClass(LivingEntity.class, new AABB(end.add(-3, -3, -3), end.add(3, 3, 3)))) {
						if (ent.isAlive() && !ent.isInvulnerable() && ent != entityLiving) {
							if (ent.hurt(DamageSource.explosion(entityLiving), (float)((damage+EnchantmentHelper.getDamageBonus(stack, ent.getMobType()))*strength))) {
								double mult = Math.max(0, 1 - ent.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
								ent.setDeltaMovement(ent.getDeltaMovement().add(0, 0.4*mult*strength, 0));
								//fire aspect, like how vanilla applies it
								//dunno what method to call to make it work with modded enchants like poison aspect or something
								int fire = stack.getEnchantmentLevel(Enchantments.FIRE_ASPECT);
								if (fire > 0 && !ent.fireImmune()) ent.setSecondsOnFire(fire*4);
							}
						}
					}

					stack.hurtAndBreak(2, player, (entity) -> entity.broadcastBreakEvent(player.getUsedItemHand()));
				}
				else {
					world.addParticle(ParticleTypes.EXPLOSION_EMITTER, end.x, end.y, end.z, 1, 0, 0);
				}
				
				player.causeFoodExhaustion(strength*2);
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
		tooltip.add(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
	}

}
