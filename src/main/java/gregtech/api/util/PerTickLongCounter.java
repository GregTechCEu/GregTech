package gregtech.api.util;

import java.util.function.LongSupplier;

public class PerTickLongCounter {

    private final LongSupplier timeSupplier;
    private final long defaultValue;
    private long lastUpdatedWorldTime;
    private long lastValue;
    private long currentValue;

    public PerTickLongCounter(LongSupplier timeSupplier) {
        this(timeSupplier, 0);
    }

    public PerTickLongCounter(LongSupplier timeSupplier, long defaultValue) {
        this.timeSupplier = timeSupplier;
        this.defaultValue = defaultValue;
        this.currentValue = defaultValue;
        this.lastValue = defaultValue;
    }

    private void checkValueState() {
        long currentWorldTime = this.timeSupplier.getAsLong();
        if (currentWorldTime != lastUpdatedWorldTime) {
            if (currentWorldTime == lastUpdatedWorldTime + 1) {
                //last updated time is 1 tick ago, so we can move current value to last
                //before resetting it to default value
                this.lastValue = currentValue;
            } else {
                //otherwise, set last value as default value
                this.lastValue = defaultValue;
            }
            this.lastUpdatedWorldTime = currentWorldTime;
            this.currentValue = defaultValue;
        }
    }

    public long get() {
        checkValueState();
        return currentValue;
    }

    public long getLast() {
        checkValueState();
        return lastValue;
    }

    public void increment(long value) {
        checkValueState();
        this.currentValue += value;
    }

    public void set(long value) {
        checkValueState();
        this.currentValue = value;
    }
}
