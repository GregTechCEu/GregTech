package gregtech.common.pipelike.cable.tile;

import net.minecraft.world.World;

import java.util.Arrays;
import java.util.function.LongSupplier;

public class AveragingPerTickCounter {

    private final LongSupplier timeSupplier;
    private final long defaultValue;
    private final long[] values;
    private long lastUpdatedWorldTime = 0;
    private int currentIndex = 0;
    private boolean dirty = true;
    private double lastAverage = 0;

    public AveragingPerTickCounter(LongSupplier timeSupplier) {
        this(timeSupplier, 0, 20);
    }

    /**
     * Averages a value over a certain amount of ticks
     *
     * @param timeSupplier current time in ticks supplier. Usually {@link World#getTotalWorldTime()}
     * @param defaultValue self-explanatory
     * @param length       amount of ticks to average (20 for 1 second)
     */
    public AveragingPerTickCounter(LongSupplier timeSupplier, long defaultValue, int length) {
        this.timeSupplier = timeSupplier;
        this.defaultValue = defaultValue;
        this.values = new long[length];
        Arrays.fill(values, defaultValue);
    }

    private void checkValueState() {
        long currentWorldTime = this.timeSupplier.getAsLong();
        if (currentWorldTime != lastUpdatedWorldTime) {
            long dif = currentWorldTime - lastUpdatedWorldTime;
            if (dif >= values.length || dif < 0) {
                Arrays.fill(values, defaultValue);
                currentIndex = 0;
            } else {
                currentIndex += dif;
                if (currentIndex > values.length - 1)
                    currentIndex -= values.length;
                int index;
                for (int i = 0, n = values.length; i < dif; i++) {
                    index = i + currentIndex;
                    if (index >= n)
                        index -= n;
                    values[index] = defaultValue;
                }
            }
            this.lastUpdatedWorldTime = currentWorldTime;
            dirty = true;
        }
    }

    /**
     * @return the value from the current tick
     */
    public long getLast() {
        checkValueState();
        return values[currentIndex];
    }

    /**
     * @return the average of all values
     */
    public double getAverage() {
        checkValueState();
        if (!dirty)
            return lastAverage;
        dirty = false;
        return lastAverage = Arrays.stream(values).sum() / (double) (values.length);
    }

    /**
     * @param value the value to increment the current value by
     */
    public void increment(long value) {
        checkValueState();
        values[currentIndex] += value;
    }

    /**
     * @param value the value to set current value to
     */
    public void set(long value) {
        checkValueState();
        values[currentIndex] = value;
    }
}
