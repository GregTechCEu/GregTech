package gregtech.api.nuclear.fission;

import net.minecraftforge.fluids.Fluid;

public interface ICoolantStats {

    /**
     * @return The fluid with these cooling properties.
     */
    Fluid getFluid();

    /**
     * @return The specific heat capacity of the fluid in J/(kg*K).
     */
    double getSpecificHeatCapacity();

    /**
     * @return A factor relating to the neutron moderation properties; the higher the factor, the fewer neutrons pass
     * through.
     */
    double getModeratorFactor();

    /**
     * @return A rough heat transfer coefficient for the fluid.
     */
    double getCoolingFactor();

    /**
     * @return The boiling point of the fluid in Kelvin.
     */
    double getBoilingPoint();

    /**
     * @return If the coolant reacts with zirconium cladding at high temperatures. This is true for mostly water-based coolants.
     */
    boolean accumulatesHydrogen();


}
