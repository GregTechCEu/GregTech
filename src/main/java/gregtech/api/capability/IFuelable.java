package gregtech.api.capability;

import java.util.Collection;

/**
 * For things that have fuel
 */
public interface IFuelable {

    /**
     * @return the fuels
     */
    Collection<IFuelInfo> getFuels();

    default boolean isOneProbeHidden() {
        return false;
    }
}