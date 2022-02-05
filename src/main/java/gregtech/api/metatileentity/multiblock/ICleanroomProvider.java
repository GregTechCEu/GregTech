package gregtech.api.metatileentity.multiblock;

import java.util.Set;

/**
 * Implement this interface in order to make a TileEntity into a block that provides a Cleanroom to other blocks
 */
public interface ICleanroomProvider {

    /**
     *
     * @return a {@link Set} of {@link CleanroomType} which the cleanroom provides
     */
    Set<CleanroomType> getTypes();

    /**
     * Sets the cleanroom's "clean" state
     *
     * @param isClean the state of cleanliness
     */
    void setClean(boolean isClean);

    /**
     *
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
     *
     * @return the amount of energy input per second
     */
    long getEnergyInputPerSecond();

    /**
     *
     * @return the tier {@link gregtech.api.GTValues.V} of energy the cleanroom uses at minimum
     */
    int getEnergyTier();
}
