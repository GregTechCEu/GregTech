package gregtech.common.pipelike.cable.tile;

import net.minecraft.world.World;

import java.util.Arrays;

public class AveragingPerTickCounter {

    private final long defaultValue;
    private final long[] values;
    private long lastUpdatedWorldTime = 0;
    private int currentIndex = 0;
    private boolean dirty = true;
    private double lastAverage = 0;

    /**
     * Averages a value over a certain amount of ticks
     *
     * @param defaultValue self explanatory
     * @param length       amount of ticks to average (20 for 1 second)
     */
    public AveragingPerTickCounter(long defaultValue, int length) {
        this.defaultValue = defaultValue;
        this.values = new long[length];
        Arrays.fill(values, defaultValue);
    }

    private void checkValueState(World world) {
        long currentWorldTime = world.getTotalWorldTime();
        if (currentWorldTime != lastUpdatedWorldTime) {
            long dif = currentWorldTime - lastUpdatedWorldTime;
            if (dif >= values.length) {
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
    public long getLast(World world) {
        checkValueState(world);
        return values[currentIndex];
    }

    /**
     * @return the average of all values
     */
    public double getAverage(World world) {
        checkValueState(world);
        if (!dirty)
            return lastAverage;
        dirty = false;
        return lastAverage = Arrays.stream(values).sum() / (double) (values.length);
    }

    /**
     * @param value the value to increment the current value by
     */
    public void increment(World world, long value) {
        checkValueState(world);
        values[currentIndex] += value;
    }

    /**
     * @param value the value to set current value to
     */
    public void set(World world, long value) {
        checkValueState(world);
        values[currentIndex] = value;
    }
}
