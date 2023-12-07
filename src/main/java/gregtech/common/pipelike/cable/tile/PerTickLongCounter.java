package gregtech.common.pipelike.cable.tile;

import net.minecraft.world.World;

public class PerTickLongCounter {

    private final long defaultValue;
    private long lastUpdatedWorldTime;
    private long lastValue;
    private long currentValue;

    public PerTickLongCounter() {
        this(0);
    }

    public PerTickLongCounter(long defaultValue) {
        this.defaultValue = defaultValue;
        this.currentValue = defaultValue;
        this.lastValue = defaultValue;
    }

    private void checkValueState(World world) {
        if (world == null) return;
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

    public long get(World world) {
        checkValueState(world);
        return currentValue;
    }

    public long getLast(World world) {
        checkValueState(world);
        return lastValue;
    }

    public void increment(World world, long value) {
        checkValueState(world);
        this.currentValue += value;
    }

    public void set(World world, long value) {
        checkValueState(world);
        this.currentValue = value;
    }
}
