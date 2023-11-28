package gregtech.api.capability.impl;

import gregtech.api.capability.IFilter;
import gregtech.api.capability.IFilteredFluidContainer;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GTFluidHandlerItemStack extends FluidHandlerItemStack implements IFilteredFluidContainer {

    @Nullable
    private IFilter<FluidStack> filter;

    private boolean canFill = true;
    private boolean canDrain = true;

    @Nullable
    private IFluidTankProperties[] properties;

    /**
     * @param container The container itemStack, data is stored on it directly as NBT.
     * @param capacity  The maximum capacity of this fluid tank.
     */
    public GTFluidHandlerItemStack(@NotNull ItemStack container, int capacity) {
        super(container, capacity);
    }

    @Nullable
    @Override
    public IFilter<FluidStack> getFilter() {
        return this.filter;
    }

    @NotNull
    public GTFluidHandlerItemStack setFilter(@Nullable IFilter<FluidStack> filter) {
        this.filter = filter;
        return this;
    }

    public boolean canFill() {
        return canFill;
    }

    public GTFluidHandlerItemStack setCanFill(boolean canFill) {
        this.canFill = canFill;
        return this;
    }

    public boolean canDrain() {
        return canDrain;
    }

    public GTFluidHandlerItemStack setCanDrain(boolean canDrain) {
        this.canDrain = canDrain;
        return this;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        if (properties == null) {
            return properties = new IFluidTankProperties[] { new TankProperties() };
        }
        return properties;
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
        return canFill() && (this.filter == null || this.filter.test(fluid));
    }

    @Override
    public boolean canDrainFluidType(FluidStack fluid) {
        return canDrain();
    }

    private final class TankProperties implements IFluidTankProperties {

        @Nullable
        @Override
        public FluidStack getContents() {
            return GTFluidHandlerItemStack.this.getFluid();
        }

        @Override
        public int getCapacity() {
            return GTFluidHandlerItemStack.this.capacity;
        }

        @Override
        public boolean canFill() {
            return GTFluidHandlerItemStack.this.canFill();
        }

        @Override
        public boolean canDrain() {
            return GTFluidHandlerItemStack.this.canDrain();
        }

        @Override
        public boolean canFillFluidType(FluidStack fluidStack) {
            return GTFluidHandlerItemStack.this.canFillFluidType(fluidStack);
        }

        @Override
        public boolean canDrainFluidType(FluidStack fluidStack) {
            return GTFluidHandlerItemStack.this.canDrainFluidType(fluidStack);
        }
    }
}
