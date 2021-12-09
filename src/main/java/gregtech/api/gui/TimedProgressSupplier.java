package gregtech.api.gui;

import java.util.function.DoubleSupplier;

public class TimedProgressSupplier implements DoubleSupplier {

    private final int msPerCycle;
    private final int maxValue;
    private final boolean countDown;
    private final long startTime;

    public TimedProgressSupplier(int ticksPerCycle, int maxValue, boolean countDown) {
        this.msPerCycle = ticksPerCycle * 50;
        this.maxValue = maxValue;
        this.countDown = countDown;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public double getAsDouble() {
        long currentTime = System.currentTimeMillis();
        long msPassed = (currentTime - startTime) % msPerCycle;
        int currentValue = (int) Math.floorDiv(msPassed * (maxValue + 1), msPerCycle);
        if (countDown) {
            return (maxValue - currentValue) / (maxValue * 1.0);
        }
        return currentValue / (maxValue * 1.0);
    }
}
