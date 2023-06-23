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
     * This method accepts energy from a side, and stores it in the container
     *
     * @param amount amount of energy to add/remove to the container
     * @return amount of energy actually accepted
     */
    long changeEnergy(long amount);

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
