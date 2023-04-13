package gregtech.api.util;

import gregtech.api.GTValues;

import java.util.NavigableMap;
import java.util.TreeMap;

import static gregtech.api.GTValues.V;

public class GTVoltageUtil {

    private static final NavigableMap<Long, Byte> tierByVoltage = new TreeMap<>();

    static {
        for (int i = 0; i < V.length; i++) {
            tierByVoltage.put(V[i], (byte) i);
        }
    }

    /**
     * @return lowest tier that can handle passed voltage
     */
    public static byte getTierByVoltage(long voltage) {
        if (voltage > V[GTValues.MAX]) return GTValues.MAX;
        return tierByVoltage.ceilingEntry(voltage).getValue();
    }

    /**
     * Ex: This method turns both 1024 and 512 into HV.
     *
     * @return the highest tier below or equal to the voltage value given
     */
    public static byte getFloorTierByVoltage(long voltage) {
        if (voltage < V[GTValues.ULV]) return GTValues.ULV;
        return tierByVoltage.floorEntry(voltage).getValue();
    }

    public static int getExplosionPower(long voltage) {
        return getTierByVoltage(voltage) + 1;
    }
}
