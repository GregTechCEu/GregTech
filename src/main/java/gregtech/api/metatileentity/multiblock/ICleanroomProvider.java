package gregtech.api.metatileentity.multiblock;

import org.jetbrains.annotations.NotNull;

/**
 * Implement this interface in order to make a TileEntity into a block that provides a Cleanroom to other blocks
 */
public interface ICleanroomProvider {

    /**
     * @param type the type to check
     * @return if the type is fulfilled
     */
    boolean checkCleanroomType(@NotNull CleanroomType type);

    /**
     * Sets the cleanroom's clean amount
     *
     * @param amount the amount of cleanliness
     */
    void setCleanAmount(int amount);

    /**
     * Adjust the cleanroom's clean amount
     *
     * @param amount the amount of cleanliness to increase/decrease by
     */
    void adjustCleanAmount(int amount);

    /**
     * @return whether the cleanroom is currently clean
     */
    boolean isClean();

    /**
     * Consumes energy from the cleanroom
     *
     * @param simulate whether to actually apply change values or not
     * @return whether the draining succeeded
     */
    boolean drainEnergy(boolean simulate);

    /**
     * @return the amount of energy input per second
     */
    long getEnergyInputPerSecond();

    /**
     * @return the tier {@link gregtech.api.GTValues#V} of energy the cleanroom uses at minimum
     */
    int getEnergyTier();
}
