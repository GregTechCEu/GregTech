package gregtech.api.fluids.fluid;

import gregtech.api.fluids.info.FluidState;
import gregtech.api.fluids.info.FluidTag;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Interface defining advanced fluid capabilities.
 * <p>
 * Implement this on an {@link Fluid} or {@link gregtech.api.fluids.definition.FluidDefinition}
 */
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
    Collection<FluidTag> getTags();

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
