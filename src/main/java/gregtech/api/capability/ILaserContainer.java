package gregtech.api.capability;

import net.minecraft.util.EnumFacing;

public interface ILaserContainer {

    /**
     * This method accepts energy from a side, and stores it in the container
     *
     * @param side   side the energy is coming from
     * @param amount amount of energy to add to the container
     * @return amount of energy actually accepted
     */
    long acceptEnergy(EnumFacing side, long amount);

    /**
     * This method accepts energy, and stores it in the container
     *
     * @param amount amount of energy to add/remove to the container
     * @return amount of energy actually accepted
     */
    long changeEnergy(long amount);

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
     * Adds specified amount of energy from this container
     *
     * @param amount amount of energy to add
     * @return amount of energy added
     */
    default long addEnergy(long amount) {
        return changeEnergy(amount);
    }


    /**
     * @return if this container accepts energy from the given side
     */
    boolean inputsEnergy(EnumFacing side);

    /**
     * @return if this container can output energy to the given side
     */
    default boolean outputsEnergy(EnumFacing side) {
        return false;
    }

    /**
     * @return amount of currently stored energy
     */
    long getEnergyStored();

    /**
     * @return maximum amount of energy that can be stored
     */
    long getEnergyCapacity();
}
