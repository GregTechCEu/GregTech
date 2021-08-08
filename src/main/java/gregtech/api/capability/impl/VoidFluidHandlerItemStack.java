package gregtech.api.capability.impl;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import javax.annotation.Nonnull;

import static net.minecraftforge.fluids.capability.templates.EmptyFluidHandler.EMPTY_TANK_PROPERTIES_ARRAY;

/**
 * Implements an item that voids fluids
 */
public class VoidFluidHandlerItemStack extends FluidHandlerItemStack {

    /**
     * Voids as much fluid as you can throw at it
     *
     * @param container The container itemStack.
     */
    public VoidFluidHandlerItemStack(@Nonnull ItemStack container) {
        this(container, Integer.MAX_VALUE);
    }

    /**
     * Voids fluid a certain amount at a time
     *
     * @param container The container itemStack.
     * @param capacity  max amount to void in each operation
     */
    public VoidFluidHandlerItemStack(@Nonnull ItemStack container, final int capacity) {
        super(container, capacity);
    }

    @Override
    public FluidStack getFluid() {
        return null;
    }

    @Override
    protected void setFluid(FluidStack fluid) {
        // No NBT tag
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return EMPTY_TANK_PROPERTIES_ARRAY;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0)
            return 0;
        return Math.min(this.capacity, resource.amount);
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return null;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return null;
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        return true;
    }

    @Override
    public boolean canDrainFluidType(FluidStack fluid) {
        return false;
    }
}
