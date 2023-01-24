package gregtech.api.capability.impl;

import gregtech.api.capability.FluidContainmentInfo;
import gregtech.api.capability.IAdvancedFluidContainer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class AdvancedFluidHandlerItemStack extends GTFluidHandlerItemStack implements IAdvancedFluidContainer {

    private final FluidContainmentInfo info;

    public AdvancedFluidHandlerItemStack(@Nonnull ItemStack container, int capacity, @Nonnull FluidContainmentInfo info) {
        super(container, capacity);
        this.info = info;
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        return this.canHoldFluid(fluid);
    }

    @Nonnull
    @Override
    public FluidContainmentInfo getContainmentInfo() {
        return this.info;
    }
}
