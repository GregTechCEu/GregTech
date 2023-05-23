package gregtech.api.capability.impl;

import gregtech.api.capability.IFilter;
import gregtech.api.capability.IFiltered;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GTFluidHandlerItemStack extends FluidHandlerItemStack implements IFiltered {

    @Nullable
    private IFilter<FluidStack> filter;

    /**
     * @param container The container itemStack, data is stored on it directly as NBT.
     * @param capacity  The maximum capacity of this fluid tank.
     */
    public GTFluidHandlerItemStack(@Nonnull ItemStack container, int capacity) {
        super(container, capacity);
    }

    @Nullable
    @Override
    public IFilter<FluidStack> getFilter() {
        return this.filter;
    }

    @Nonnull
    public GTFluidHandlerItemStack setFilter(@Nullable IFilter<FluidStack> filter) {
        this.filter = filter;
        return this;
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        FluidStack drained = super.drain(resource, doDrain);
        this.removeTagWhenEmpty(doDrain);
        return drained;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        FluidStack drained = super.drain(maxDrain, doDrain);
        this.removeTagWhenEmpty(doDrain);
        return drained;
    }

    private void removeTagWhenEmpty(boolean doDrain) {
        if (doDrain && this.getFluid() == null) {
            this.container.setTagCompound(null);
        }
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        return this.filter == null || this.filter.test(fluid);
    }
}
