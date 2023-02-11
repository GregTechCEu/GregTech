package gregtech.api.util;

import java.util.Random;

public class IdleTracker {

    private static final Random random = new Random();

    private int idle = 1;
    private final int minIdle;
    private final int maxIdle;
    private final int step;
    private long timer = random.nextInt(10000);

    public IdleTracker() {
        this(1, 60, 1);
    }

    public IdleTracker(int minIdle, int maxIdle) {
        this(minIdle, maxIdle, 1);
    }

    public IdleTracker(int minIdle, int maxIdle, int step) {
        this.idle = minIdle;
        this.minIdle = minIdle;
        this.maxIdle = maxIdle;
        this.step = step;
    }

    /**
     * provide external timer
     * @param tick
     * @return
     */
    public boolean canAction(long tick) {
        return tick % idle == 0;
    }

    /**
     * use inner timer
     * @return
     */
    public boolean canAction() {
        return timer % idle == 0;
    }

    public int reset() {
        return idle = minIdle;
    }

    public long update() {
        return ++timer;
    }

    public long getTimer() {
        return timer;
    }

    public int inc() {
        return idle = (Math.min(idle + step, maxIdle));
    }

    public int dec() {
        return idle = (Math.max(idle - step, minIdle));
    }

    public int getIdle() {
        return idle;
    }
}
