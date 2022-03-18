package gregtech.api.capability;

import gregtech.common.ConfigHolder;
import net.minecraftforge.energy.IEnergyStorage;

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

    /**
     * @deprecated Specify ratio
     */
    public static int toFe(long eu) {
        return (int) (eu * ratio(false));
    }

    /**
     * @deprecated Specify ratio
     */
    public static long toEu(long fe) {
        return fe / ratio(true);
    }

    /**
     * Inserts energy to the storage. EU -> FE conversion is performed.
     * @return amount of EU inserted
     */
    public static long insertEu(IEnergyStorage storage, long amountEU){
        int euToFeRatio = ratio(false);
        int feSent = storage.receiveEnergy(toFe(amountEU, euToFeRatio), true);
        return toEu(storage.receiveEnergy(feSent - (feSent % euToFeRatio), false), euToFeRatio);
    }

    /**
     * Extracts energy from the storage. EU -> FE conversion is performed.
     * @return amount of EU extracted
     */
    public static long extractEu(IEnergyStorage storage, long amountEU){
        int euToFeRatio = ratio(false);
        int extract = storage.extractEnergy(toFe(amountEU, euToFeRatio), true);
        return toEu(storage.extractEnergy(extract - (extract % euToFeRatio), false), euToFeRatio);
    }
}
