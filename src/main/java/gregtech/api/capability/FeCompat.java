package gregtech.api.capability;

import gregtech.common.ConfigHolder;

public class FeCompat {

    /**
      * @return if native conversion is enabled
     */
    public static boolean nativeEUtoFE() {
        return ConfigHolder.U.energyOptions.nativeEUToFE;
    }

    /**
     * @return how many FE are equal to 1 EU when converting from EU to FE
     */
    public static double euToFeRatio() {
        return ConfigHolder.U.energyOptions.euToFeRatio;
    }

    /**
     * @return how many FE are equal to 1 EU when converting from FE to EU
     * With no loss it is equal to {@link #euToFeRatio()}
     */
    public static double feToEuRatio() {
        return ConfigHolder.U.energyOptions.feToEuRatio;
    }

    /**
     * Converts eu to fe
     * @return fe
     */
    public static int convertToFe(long eu) {
        return (int) (eu * (1 / ConfigHolder.U.energyOptions.euToFeRatio));
    }

    /**
     * Converts fe to eu
     * @return eu
     */
    public static long convertToEu(long fe) {
        return (long) (fe * ConfigHolder.U.energyOptions.feToEuRatio);
    }
}
