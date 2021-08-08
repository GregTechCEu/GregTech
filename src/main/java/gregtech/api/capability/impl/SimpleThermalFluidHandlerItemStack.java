package gregtech.api.capability.impl;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStackSimple;

import javax.annotation.Nonnull;

public class SimpleThermalFluidHandlerItemStack extends FluidHandlerItemStackSimple {

    public final int minFluidTemperature;
    public final int maxFluidTemperature;

    /**
     * @param container The container itemStack, data is stored on it directly as NBT.
     * @param capacity  The maximum capacity of this fluid tank.
     */
    public SimpleThermalFluidHandlerItemStack(@Nonnull ItemStack container, int capacity, int minFluidTemperature, int maxFluidTemperature) {
        super(container, capacity);
        this.minFluidTemperature = minFluidTemperature;
        this.maxFluidTemperature = maxFluidTemperature;
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        int liquidTemperature = fluid.getFluid().getTemperature();
        return liquidTemperature >= minFluidTemperature && liquidTemperature <= maxFluidTemperature;
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

    private void removeTagWhenEmpty(Boolean doDrain) {
        if (doDrain && this.getFluid() == null) {
            this.container.setTagCompound(null);
        }
    }
}
