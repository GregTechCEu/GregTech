package gregtech.api.capability;

import gregtech.common.ConfigHolder;

public class FeCompat {
    /**
     * Conversion ratio used by native conversion
     */
    public static double nativeRatio() {
        return ConfigHolder.compat.energy.euToFeRatio;
    }

    /**
     * Conversion ratio used by energy converters
     */
    public static double ratio(boolean feToEu) {
        return feToEu ? ConfigHolder.compat.energy.feToEuRatio : ConfigHolder.compat.energy.euToFeRatio;
    }

    /**
     * Converts eu to fe, using specified ratio
     * @return fe
     */
    public static int toFe(long eu, double ratio){
        return (int) (eu * ratio);
    }

    /**
     * Converts fe to eu, using specified ratio
     * @return eu
     */
    public static long toEu(long fe, double ratio){
        return (int) (fe / ratio);
    }

    public static int nativeToFe(long eu){
        return toFe(eu, nativeRatio());
    }

    public static long nativeToEu(long fe){
        return toEu(fe, nativeRatio());
    }

    public static int toFe(long eu, boolean feToEu){
        return toFe(eu, ratio(feToEu));
    }

    public static long toEu(long fe, boolean feToEu){
        return toEu(fe, ratio(feToEu));
    }
}
