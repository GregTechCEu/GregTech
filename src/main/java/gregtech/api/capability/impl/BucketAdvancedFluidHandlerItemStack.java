package gregtech.api.capability.impl;

import gregtech.api.capability.FluidContainmentInfo;
import gregtech.api.capability.IAdvancedFluidContainer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

/**
 * Advanced fluid handler for item stacks, which behaves like vanilla buckets (no partial fill).
 * <p>
 * Works on all FluidHandlerItemStacks, does not require a bucket!
 */
public class BucketAdvancedFluidHandlerItemStack extends GTSimpleFluidHandlerItemStack implements IAdvancedFluidContainer {

    private final FluidContainmentInfo info;

    public BucketAdvancedFluidHandlerItemStack(@Nonnull ItemStack container, int capacity, @Nonnull FluidContainmentInfo info) {
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
