package gregtech.api.capability;

import gregtech.common.ConfigHolder;

public class FeCompat {
    /**
     * Conversion ratio used by native conversion
     */
    public static double nativeRatio() {
        return ConfigHolder.U.energyOptions.euToFeRatio;
    }

    /**
     * Conversion ratio used by energy converters
     */
    public static double energyConverterRatio(boolean feToEu) {
        return feToEu ? ConfigHolder.U.energyOptions.feToEuRatio : ConfigHolder.U.energyOptions.euToFeRatio;
    }

    /**
     * Converts eu to fe, using specified ratio
     * @return fe
     */
    public static int convertToFe(long eu, double ratio){
        return (int) (eu / ratio);
    }

    /**
     * Converts fe to eu, using specified ratio
     * @return eu
     */
    public static long convertToEu(long fe, double ratio){
        return (int) (fe * ratio);
    }

    public static int nativeToFe(long eu){
        return convertToFe(eu, nativeRatio());
    }

    public static long nativeToEu(long fe){
        return convertToEu(fe, nativeRatio());
    }

    public static int converterToFe(long eu, boolean feToEu){
        return convertToFe(eu, energyConverterRatio(feToEu));
    }

    public static long converterToEu(long fe, boolean feToEu){
        return convertToEu(fe, energyConverterRatio(feToEu));
    }
}
