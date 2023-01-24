package gregtech.api.capability;

import gregtech.api.capability.impl.FilteredFluidHandler;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class AdvancedFilteredFluidHandler extends FilteredFluidHandler implements IAdvancedFluidContainer {

    private final FluidContainmentInfo info;

    public AdvancedFilteredFluidHandler(int capacity, @Nonnull FluidContainmentInfo info) {
        super(capacity);
        this.info = info;
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        return super.canFillFluidType(fluid) && canHoldFluid(fluid);
    }

    @Nonnull
    @Override
    public FluidContainmentInfo getContainmentInfo() {
        return this.info;
    }
}
