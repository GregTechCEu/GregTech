package gregtech.api.items.metaitem.stats;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class ItemFluidContainer implements IItemContainerItemProvider {

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        IFluidHandlerItem handler = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (handler != null) {
            FluidStack drained = handler.drain(1000, false);
            if (drained == null || drained.amount != 1000) return ItemStack.EMPTY;
            handler.drain(1000, true);
            return handler.getContainer().copy();
        }
        return itemStack;
    }
}
