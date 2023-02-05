package gregtech.api.items.metaitem;

import gregtech.api.capability.FluidContainmentInfo;
import gregtech.api.capability.impl.AdvancedFluidHandlerItemStack;
import gregtech.api.capability.impl.BucketAdvancedFluidHandlerItemStack;
import gregtech.api.items.metaitem.stats.IItemCapabilityProvider;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;

public class AdvancedFluidStats implements IItemCapabilityProvider {

    public final int capacity;
    public final boolean allowPartialFill;
    public final FluidContainmentInfo info;

    public AdvancedFluidStats(int capacity, boolean allowPartialFill, @Nonnull FluidContainmentInfo info) {
        this.capacity = capacity;
        this.allowPartialFill = allowPartialFill;
        this.info = info;
    }

    @Override
    public ICapabilityProvider createProvider(ItemStack itemStack) {
        if (allowPartialFill) {
            return new AdvancedFluidHandlerItemStack(itemStack, capacity, info);
        }
        return new BucketAdvancedFluidHandlerItemStack(itemStack, capacity, info);
    }
}
