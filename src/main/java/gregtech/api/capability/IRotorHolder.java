package gregtech.api.capability;

public interface IRotorHolder {

    /**
     * @return the tier of the rotor holder
     */
    int getTier();

    /**
     * @return the base efficiency of the rotor holder
     */
    default int getBaseEfficiency() {
        return 10000;
    }

    /**
     * @return the current speed of the holder
     */
    int getSpeed();

    /**
     * @return the rotor's efficiency
     */
    int getRotorEfficiency();


    /**
     * @return the rotor's power
     */
    int getRotorPower();

    /**
     * damages the rotor
     *
     * @param amount   to damage
     * @param simulate whether to actually apply the damage
     * @return true if damage can be applied
     */
    boolean damageRotor(int amount, boolean simulate);

    /**
     * @return the power multiplier provided by the rotor holder
     */
    int getHolderPowerMultiplier();

    /**
     * @return the efficiency provided by the rotor holder
     */
    int getHolderEfficiency();
}
