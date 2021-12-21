package gregtech.api.capability;

public interface IRotorHolder {

    /**
     * @return the base efficiency of the rotor holder
     */
    static int getBaseEfficiency() {
        return 10000;
    }

    /**
     *
     * @return the total efficiency the rotor holder and rotor provide
     */
    default int getTotalEfficiency() {
        int rotorEfficiency = getRotorEfficiency();
        if (rotorEfficiency == -1)
            return -1;

        int holderEfficiency = getHolderEfficiency();
        if (holderEfficiency == -1)
            return -1;

        return Math.max(getBaseEfficiency(), rotorEfficiency + holderEfficiency);
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
