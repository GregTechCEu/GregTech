package gregtech.api.capability.impl.fecompat;

import gregtech.common.ConfigHolder;

public class ForgeEnergyCompat {

    /**
     * @return If native conversion is enabled.
     */
    public static boolean nativeEUtoFE() {
        return ConfigHolder.compat.energy.nativeEUToFE;
    }

    /**
     * @return how many FE are equal to 1 EU when converting from EU to FE.
     */
    public static double ratioEUToFE() {
        return ConfigHolder.compat.energy.ratioEUToFE;
    }

    /**
     * @return How many FE are equal to 1 EU when converting from FE to EU.
     *
     * Currently is equal to {@link ForgeEnergyCompat#ratioEUToFE()}.
     */
    public static double ratioFEToEU() {
        return ConfigHolder.compat.energy.ratioEUToFE;
    }

    /**
     * Converts EU to FE.
     * @return FE
     */
    public static int convertToFE(long EU) {
        return (int) (EU * ConfigHolder.compat.energy.ratioEUToFE);
    }

    /**
     * Converts FE to EU.
     *
     * @return EU
     */
    public static long convertToEU(long FE) {
        return (long) (FE * ConfigHolder.compat.energy.ratioEUToFE);
    }
}
