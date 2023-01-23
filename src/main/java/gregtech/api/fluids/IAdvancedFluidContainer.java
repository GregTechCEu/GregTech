package gregtech.api.fluids;

import gregtech.api.fluids.info.FluidData;
import gregtech.api.fluids.info.FluidState;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

public interface IAdvancedFluidContainer {

    /**
     * @param data the data to check
     * @return if this container can contain specific fluid data
     */
    boolean canContain(@Nonnull FluidData data);

    /**
     * @param state the state to check
     * @return if this container can contain a fluid of the state
     */
    boolean canContain(@Nonnull FluidState state);

    /**
     * Provides access to the fluid in order to ret
     *
     * @param data        the relevant fluid data
     * @param state       the state of the fluid
     * @param temperature the temperature to check
     * @return if the temperature can be contained according to the provided data
     */
    boolean canHandleTemperature(@Nonnull Collection<FluidData> data, @Nonnull FluidState state, int temperature);

    /**
     * Append relevant translated tooltips for fluid containment
     * @param list the list to append to
     */
    default void appendContainmentInfo(@Nonnull List<String> list) {/**/}
}
