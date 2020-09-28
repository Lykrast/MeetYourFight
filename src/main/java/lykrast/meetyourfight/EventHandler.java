package lykrast.meetyourfight;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICurio;

@Mod.EventBusSubscriber(modid = MeetYourFight.MODID)
public class EventHandler {		
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
