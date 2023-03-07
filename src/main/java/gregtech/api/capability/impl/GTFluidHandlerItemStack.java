package gregtech.api.capability.impl;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import javax.annotation.Nonnull;

public class GTFluidHandlerItemStack extends FluidHandlerItemStack {

    /**
     * @param container The container itemStack, data is stored on it directly as NBT.
     * @param capacity  The maximum capacity of this fluid tank.
     */
    public GTFluidHandlerItemStack(@Nonnull ItemStack container, int capacity) {
        super(container, capacity);
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
}
