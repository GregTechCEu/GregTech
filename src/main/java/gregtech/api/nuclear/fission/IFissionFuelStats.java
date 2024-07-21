package gregtech.api.nuclear.fission;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public interface IFissionFuelStats {

    /**
     * @return The maximum temperature the fuel can handle before the reactor melts down.
     */
    int getMaxTemperature();

    /**
     * @return How long the fuel lasts in the reactor, in terms of the megajoules it makes.
     */
    int getDuration();

    /**
     * @return The cross section of slow neutrons that can be captured by this fuel to breed it.
     */
    double getSlowNeutronCaptureCrossSection();

    /**
     * @return The cross section of fast neutrons that can be captured by this fuel to breed it.
     */
    double getFastNeutronCaptureCrossSection();

    /**
     * @return The cross section of slow neutrons that can cause fission in this fuel.
     */
    double getSlowNeutronFissionCrossSection();

    /**
     * @return The cross section of fast neutrons that can cause fission in this fuel.
     */
    double getFastNeutronFissionCrossSection();

    /**
     * @return The average time for a neutron to be emitted during a fission event. Do not make this accurate.
     */
    double getNeutronGenerationTime();
}
