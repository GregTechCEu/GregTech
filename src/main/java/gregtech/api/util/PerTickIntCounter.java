package gregtech.api.util;

import net.minecraft.world.World;

public class PerTickIntCounter {

    private final int defaultValue;

    private long lastUpdatedWorldTime;

    private int lastValue;
    private int currentValue;

    public PerTickIntCounter(int defaultValue) {
        this.defaultValue = defaultValue;
        this.currentValue = defaultValue;
        this.lastValue = defaultValue;
    }

    private void checkValueState(World world) {
        long currentWorldTime = world.getTotalWorldTime();
        if (currentWorldTime != lastUpdatedWorldTime) {
            if (currentWorldTime == lastUpdatedWorldTime + 1) {
                // last updated time is 1 tick ago, so we can move current value to last
                // before resetting it to default value
                this.lastValue = currentValue;
            } else {
                // otherwise, set last value as default value
                this.lastValue = defaultValue;
            }
            this.lastUpdatedWorldTime = currentWorldTime;
            this.currentValue = defaultValue;
        }
    }

    public int get(World world) {
        checkValueState(world);
        return currentValue;
    }

    public int getLast(World world) {
        checkValueState(world);
        return lastValue;
    }

    public void increment(World world, int value) {
        checkValueState(world);
        this.currentValue += value;
    }

    public void set(World world, int value) {
        checkValueState(world);
        this.currentValue = value;
    }
}
