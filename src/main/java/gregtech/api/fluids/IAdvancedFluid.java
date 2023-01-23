package gregtech.api.fluids;

import gregtech.api.fluids.info.FluidData;
import gregtech.api.fluids.info.FluidState;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import java.util.Collection;

public interface IAdvancedFluid {

    /**
     * @return the fluid state for this fluid
     */
    @Nonnull
    FluidState getState();

    /**
     * @return the fluid data for this fluid
     */
    @Nonnull
    Collection<FluidData> getData();

    /**
     * New implementations of this should not return values less than or equal to 0.
     * <p>
     * Note that calling {@link Fluid#getTemperature()} or equivalent on an object which is not an IAdvancedFluid,
     * can return values below zero.
     * </p>
     * @return the temperature of the fluid in Kelvin.
     */
    int getTemperature();
}
