package gregtech.api.capability;

import gregtech.api.fluids.MaterialFluid;
import gregtech.api.fluids.fluidType.FluidType;
import gregtech.api.fluids.fluidType.FluidTypes;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

/**
 * Fluid filter based on fluid properties; i.e. temperature, fluid state, and various material flags such as acid
 * and plasma.
 *
 * @see FluidType
 * @see FluidTypes
 * @see MaterialFluid
 */
public interface IPropertyFluidFilter extends IFilter<FluidStack> {

    /**
     * Minimum temperature of the fluid in kelvin before it starts being considered 'cryogenic'; if a fluid has lower
     * temperature than this, it's considered cryogenic.
     */
    int CRYOGENIC_TEMPERATURE_THRESHOLD = 120;

    @Override
    default boolean test(@Nonnull FluidStack stack) {
        Fluid fluid = stack.getFluid();
        if (fluid.getTemperature() < CRYOGENIC_TEMPERATURE_THRESHOLD && !isCryoProof()) return false;
        if (fluid.isGaseous() && !isGasProof()) return false;

        if (fluid instanceof MaterialFluid materialFluid) {
            FluidType fluidType = materialFluid.getFluidType();
            if (fluidType == FluidTypes.ACID) {
                if (!isAcidProof()) return false;
            } else if (fluidType == FluidTypes.PLASMA) {
                return isPlasmaProof(); // bypass max temperature check below
            }
        }
        return fluid.getTemperature() <= getMaxFluidTemperature();
    }

    @Override
    default int getPriority() {
        return IFilter.blacklistLikePriority();
    }

    /**
     * This is always checked, regardless of the contained fluid being a {@link MaterialFluid} or not
     *
     * @return the maximum allowed temperature for a fluid
     */
    int getMaxFluidTemperature();

    /**
     * This is always checked, regardless of the contained fluid being a {@link MaterialFluid} or not
     *
     * @return whether this filter allows gases
     */
    boolean isGasProof();

    /**
     * @return whether this filter allows acids
     * @see FluidTypes
     */
    boolean isAcidProof();

    /**
     * @return whether this filter allows cryogenic fluids
     * @see FluidTypes
     */
    boolean isCryoProof();

    /**
     * @return whether this filter allows plasmas
     * @see FluidTypes
     */
    boolean isPlasmaProof();
}
