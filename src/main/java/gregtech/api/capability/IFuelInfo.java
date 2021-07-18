package gregtech.api.capability;

/**
 * Information about fuel
 */
public interface IFuelInfo {
    /**
     * @return the fuel
     */
    String getFuelName();

    /**
     * @return the amount of fuel remaining
     */
    int getFuelRemaining();

    /**
     * @return the fuel capacity
     */
    int getFuelCapacity();

    /**
     * @return the minimum fuel that can be consumed
     */
    int getFuelMinConsumed();

    /**
     * @return the estimated amount of time in ticks for burning the remaining fuel
     */
    long getFuelBurnTimeLong();
}
