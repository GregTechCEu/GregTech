package gregtech.api.capability;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IRotorHolder {

    /**
     * @return the base efficiency of the rotor holder in 100 * %
     */
    static int getBaseEfficiency() {
        return 10000;
    }

    /**
     *
     * @return the total efficiency the rotor holder and rotor provide in 100 * %
     */
    default int getTotalEfficiency() {
        int rotorEfficiency = getRotorEfficiency();
        if (rotorEfficiency == -1)
            return -1;

        int holderEfficiency = getHolderEfficiency();
        if (holderEfficiency == -1)
            return -1;

        return Math.max(getBaseEfficiency(), rotorEfficiency * holderEfficiency);
    }

    /**
     * returns true on both the Client and Server
     *
     * @return whether there is a rotor in the holder
     */
    boolean hasRotor();

    /**
     * returns true on only the Server
     *
     * @return whether there is a rotor in the holder
     */
    @SideOnly(Side.SERVER)
    boolean hasRotorServer();

    /**
     *
     * @return true if the rotor is spinning
     */
    boolean isRotorSpinning();

    /**
     * @return the current speed of the holder
     */
    int getRotorSpeed();

    /**
     *
     * @return true if the rotor is at maximum speed
     */
    boolean isRotorMaxSpeed();

    /**
     * @return the rotor's efficiency in %
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
