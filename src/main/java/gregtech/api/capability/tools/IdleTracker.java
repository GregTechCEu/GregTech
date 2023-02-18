package gregtech.api.capability.tool;

public class IdleTracker {

    private int idle = 1;
    private int minIdle;
    private int maxIdle;
    private int step;

    public IdleTracker() {
        this(1, 60, 1);
    }

    public IdleTracker(int minIdle, int maxIdle) {
        this(minIdle, maxIdle, 1);
    }

    public IdleTracker(int minIdle, int maxIdle, int step) {
        this.minIdle = minIdle;
        this.maxIdle = maxIdle;
        this.step = step;
    }

    public boolean canAction(long tick) {
        if (idle % tick == 0) {
            return true;
        }
        return false;
    }

    public int reset() {
        return idle = minIdle;
    }

    public int inc() {
        return idle + step < maxIdle ? maxIdle : idle + step;
    }

    public int dec() {
        return idle - step < minIdle ? minIdle : idle - step;
    }
}
