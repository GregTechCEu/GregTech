package gregtech.api.recipes.properties.impl;

public class PowerPropertyData {

    public static final PowerPropertyData EMPTY = new PowerPropertyData(0, 0);

    private long voltage;
    private long amperage;

    public PowerPropertyData(long voltage) {
        this(voltage, 1);
    }

    public PowerPropertyData(long voltage, long amperage) {
        this.voltage = voltage;
        this.amperage = amperage;
    }

    public long[] toLongArray() {
        return new long[] { voltage, amperage };
    }

    public static PowerPropertyData fromLongArray(long[] array) {
        return new PowerPropertyData(array[0], array[1]);
    }

    public void setVoltage(long voltage) {
        this.voltage = voltage;
    }

    public long getVoltage() {
        return voltage;
    }

    public void setAmperage(long amperage) {
        this.amperage = amperage;
    }

    public long getAmperage() {
        return amperage;
    }
}
