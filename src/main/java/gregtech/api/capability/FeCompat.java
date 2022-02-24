package gregtech.api.capability;

import gregtech.common.ConfigHolder;

public class FeCompat {

    /**
     * Conversion ratio used by energy converters
     */
    public static int ratio(boolean feToEu) {
        return feToEu ? ConfigHolder.compat.energy.feToEuRatio : ConfigHolder.compat.energy.euToFeRatio;
    }

    /**
     * Converts eu to fe, using specified ratio
     * @return fe
     */
    public static int toFe(long eu, int ratio){
        return (int) (eu * ratio);
    }

    /**
     * Converts fe to eu, using specified ratio
     * @return eu
     */
    public static long toEu(long fe, int ratio){
        return fe / ratio;
    }

    public static int toFe(long eu) {
        return (int) (eu * ratio(false));
    }

    public static long toEu(long fe) {
        return fe / ratio(true);
    }
}
