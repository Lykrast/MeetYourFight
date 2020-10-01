package lykrast.meetyourfight.misc;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.registry.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICurio;

@Mod.EventBusSubscriber(modid = MeetYourFight.MODID)
public class EventHandler {	
	@SubscribeEvent
	public static void entityDamage(final LivingHurtEvent event) {
		Entity attacker = event.getSource().getTrueSource();
		LivingEntity attacked = event.getEntityLiving();
		if (attacker != null && attacked != null && attacker instanceof PlayerEntity) {
			PlayerEntity pattacker = (PlayerEntity)attacker;
			//Slicer's Dice
			if (CuriosApi.getCuriosHelper().findEquippedCurio(ModItems.slicersDice, pattacker).isPresent()) {
				double luck = pattacker.getAttributeValue(Attributes.LUCK);
				double chance = 0.1;
				if (luck >= 0) chance = (2.0 + luck) / (20.0 + luck);
				else chance = 1.0 / (10.0 - 4 * luck);
				if (pattacker.getRNG().nextDouble() <= chance) {
					event.setAmount(event.getAmount() * 2);
					//TODO proper sound event
					pattacker.world.playSound(null, attacked.getPosition(), SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, SoundCategory.PLAYERS, 1, 1);
					((ServerWorld)pattacker.world).spawnParticle(ParticleTypes.CRIT, attacked.getPosX(), attacked.getPosYEye(), attacked.getPosZ(), 15, 0.2, 0.2, 0.2, 0);
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void attachCapability(final AttachCapabilitiesEvent<ItemStack> event) {
		//Copied and adjusted from Enigmatic Legacy
		ItemStack stack = event.getObject();
		if (stack.getItem() instanceof ICurio && stack.getItem().getRegistryName().getNamespace().equals(MeetYourFight.MODID)) {
			event.addCapability(CuriosCapability.ID_ITEM, new Provider((ICurio) stack.getItem()));
		}
	}
	
	private static class Provider implements ICapabilityProvider {
		private LazyOptional<ICurio> curio;

		public Provider(ICurio curio) {
			this.curio = LazyOptional.of(() -> curio);
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			return CuriosCapability.ITEM.orEmpty(cap, curio);
		}
		
	}
}
