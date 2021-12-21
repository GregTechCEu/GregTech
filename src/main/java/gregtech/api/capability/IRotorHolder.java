package gregtech.api.capability;

public interface IRotorHolder {

    /**
     *
     * @return the tier of the rotor holder
     */
    int getTier();

    /**
     *
     * @return the base efficiency of the rotor holder
     */
    default int getBaseEfficiency() {
        return 10000;
    }

    /**
     *
     * @param turbineTier the tier of the attached turbine multiblock
     * @return the total efficiency provided by the rotor holder
     */
    default int getRotorHolderEfficiency(int turbineTier) {
        int tierDifference = getTier() - turbineTier;
        if (tierDifference < 0)
            return 0;
        // efficiency is 100% + 10% per tier over multiblock tier
        return getBaseEfficiency() + tierDifference * 1000;
    }

    /**
     *
     * @return the color of the contained rotor
     */
    int getRotorColor();

    /**
     *
     * @return the current speed of the holder
     */
    int getSpeed();
}
