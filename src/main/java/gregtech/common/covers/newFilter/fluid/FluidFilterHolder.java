package gregtech.common.covers.newFilter.fluid;

import gregtech.api.util.IDirtyNotifiable;
import gregtech.common.covers.newFilter.Filter;
import gregtech.common.covers.newFilter.FilterHolder;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;

public class FluidFilterHolder extends FilterHolder<FluidStack, Filter<FluidStack>> {

    public FluidFilterHolder(IDirtyNotifiable dirtyNotifiable) {
        super(dirtyNotifiable);
    }

    public FluidFilterHolder(IItemHandlerModifiable filterInventory, int filterSlotIndex, IDirtyNotifiable dirtyNotifiable) {
        super(filterInventory, filterSlotIndex, dirtyNotifiable);
    }
}
