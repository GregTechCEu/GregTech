package gregtech.api.capability;

public interface ILaserContainer {

    /**
     * Removes specified amount of energy from this container
     *
     * @param amount amount of energy to remove
     * @return amount of energy removed
     */
    default long removeEnergy(long amount) {
        return changeEnergy(-amount);
    }

    /**
     * This method accepts energy, and stores it in the container
     *
     * @param amount amount of energy to add/remove to the container
     * @return amount of energy actually accepted
     */
    long changeEnergy(long amount);


    /**
     * @return amount of currently stored energy in this
     */
    long getEnergyStored();

    /**
     * @return maximum amount of energy that can be stored in this
     */
    long getEnergyCapacity();

    /**
     * @return maximum amount of energy that can be added or removed per tick
     */
    long getMaxThroughput();
}
