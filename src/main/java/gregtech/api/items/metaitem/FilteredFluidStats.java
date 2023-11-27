package gregtech.api.items.metaitem;

import gregtech.api.capability.IFilter;
import gregtech.api.capability.impl.GTFluidHandlerItemStack;
import gregtech.api.capability.impl.GTSimpleFluidHandlerItemStack;
import gregtech.api.capability.impl.PropertyFluidFilter;
import gregtech.api.items.metaitem.stats.IItemCapabilityProvider;
import gregtech.api.items.metaitem.stats.IItemComponent;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

public class FilteredFluidStats implements IItemComponent, IItemCapabilityProvider {

    public final int capacity;
    public final boolean allowPartialFill;
    @Nullable
    public final IFilter<FluidStack> filter;

    public FilteredFluidStats(int capacity, boolean allowPartialFill, @Nullable IFilter<FluidStack> filter) {
        this.capacity = capacity;
        this.allowPartialFill = allowPartialFill;
        this.filter = filter;
    }

    public FilteredFluidStats(int capacity, int maxFluidTemperature, boolean gasProof, boolean acidProof,
                              boolean cryoProof, boolean plasmaProof, boolean allowPartialFill) {
        this(capacity, allowPartialFill,
                new PropertyFluidFilter(maxFluidTemperature, gasProof, acidProof, cryoProof, plasmaProof));
    }

    @Override
    public ICapabilityProvider createProvider(ItemStack itemStack) {
        return allowPartialFill ?
                new GTFluidHandlerItemStack(itemStack, this.capacity).setFilter(this.filter) :
                new GTSimpleFluidHandlerItemStack(itemStack, this.capacity).setFilter(this.filter);
    }
}
