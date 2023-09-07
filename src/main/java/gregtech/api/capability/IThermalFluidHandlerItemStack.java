package gregtech.api.capability;

import gregtech.api.fluids.MaterialFluid;
import gregtech.api.fluids.fluidType.FluidType;
import gregtech.api.fluids.fluidType.FluidTypes;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

/**
 * Interface for FluidHandlerItemStacks which handle GT's unique fluid mechanics
 *
 * @see FluidType
 * @see FluidTypes
 * @see MaterialFluid
 * @deprecated use {@link IPropertyFluidFilter}
 */
@Deprecated
public interface IThermalFluidHandlerItemStack {

    /**
     * @param stack the {@link FluidStack} to check
     * @return whether the FluidStack can be used to fill this fluid container
     */
    default boolean canFillFluidType(@Nullable FluidStack stack) {
        if (stack == null || stack.getFluid() == null) return false;

        Fluid fluid = stack.getFluid();
        if (fluid.getTemperature() > getMaxFluidTemperature()) return false;
        // fluids less than 120K are cryogenic
        if (fluid.getTemperature() < 120 && !isCryoProof()) return false;
        if (fluid.isGaseous() && !isGasProof()) return false;

        if (fluid instanceof MaterialFluid) {
            FluidType fluidType = ((MaterialFluid) fluid).getFluidType();
            if (fluidType == FluidTypes.ACID && !isAcidProof()) return false;
            if (fluidType == FluidTypes.PLASMA && !isPlasmaProof()) return false;
        }
        return true;
    }

    /**
     * This is always checked, regardless of the contained fluid being a {@link MaterialFluid} or not
     *
     * @return the maximum allowed temperature for a fluid to be stored in this container
     */
    int getMaxFluidTemperature();

    /**
     * This is always checked, regardless of the contained fluid being a {@link MaterialFluid} or not
     *
     * @return true if this fluid container allows gases, otherwise false
     */
    boolean isGasProof();

    /**
     * @return true if this fluid container allows acids, otherwise false
     * @see FluidTypes
     */
    boolean isAcidProof();

    /**
     * @return true if this fluid container allows cryogenics, otherwise false
     * @see FluidTypes
     */
    boolean isCryoProof();

    /**
     * @return true if this fluid container allows plasmas, otherwise false
     * @see FluidTypes
     */
    boolean isPlasmaProof();
}
