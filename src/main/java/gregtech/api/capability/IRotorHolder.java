package gregtech.api.capability;

public interface IRotorHolder {

    /**
     * @return true if the front face is unobstructed
     */
    boolean isFrontFaceFree();

    /**
     * @return the base efficiency of the rotor holder in %
     */
    static int getBaseEfficiency() {
        return 100;
    }

    /**
     * @return the total efficiency the rotor holder and rotor provide in %
     */
    default int getTotalEfficiency() {
        int rotorEfficiency = getRotorEfficiency();
        if (rotorEfficiency == -1)
            return -1;

        int holderEfficiency = getHolderEfficiency();
        if (holderEfficiency == -1)
            return -1;

        return Math.max(getBaseEfficiency(), rotorEfficiency * holderEfficiency / 100);
    }

    /**
     *
     * @return the total power boost to output and consumption the rotor holder and rotor provide in %
     */
    default int getTotalPower() {
        return getHolderPowerMultiplier() * getRotorPower();
    }

    /**
     * returns true on both the Client and Server
     *
     * @return whether there is a rotor in the holder
     */
    boolean hasRotor();

    /**
     * @return the current speed of the holder
     */
    int getRotorSpeed();

    /**
     * @return the rotor's efficiency in %
     */
    int getRotorEfficiency();

    /**
     * @return the rotor's power in %
     */
    int getRotorPower();

    /**
     * @return the rotor's durability as %
     */
    int getRotorDurabilityPercent();

    /**
     * damages the rotor
     *
     * @param amount to damage
     */
    void damageRotor(int amount);

    /**
     *
     * @return the maximum speed the holder can have
     */
    int getMaxRotorHolderSpeed();

    /**
     * @return the power multiplier provided by the rotor holder
     */
    int getHolderPowerMultiplier();

    /**
     * @return the efficiency provided by the rotor holder in %
     */
    int getHolderEfficiency();
}
