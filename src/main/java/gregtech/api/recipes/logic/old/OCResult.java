package gregtech.api.recipes.logic.old;

import org.jetbrains.annotations.NotNull;

public final class OCResult {

    private long eut;
    private long parallelEUt;
    private int duration;
    private int parallel;

    public void init(long eut, int duration) {
        init(eut, duration, 0);
    }

    public void init(long eut, int duration, int parallel) {
        init(eut, duration, parallel, parallel == 0 ? eut : eut * parallel);
    }

    public void init(long eut, int duration, int parallel, long parallelEUt) {
        this.eut = eut;
        this.duration = duration;
        this.parallel = parallel;
        this.parallelEUt = parallelEUt;
    }

    public void reset() {
        this.eut = 0L;
        this.parallelEUt = 0L;
        this.duration = 0;
        this.parallel = 0;
    }

    public long eut() {
        return eut;
    }

    public void setEut(long eut) {
        this.eut = eut;
    }

    public long parallelEUt() {
        return parallelEUt;
    }

    public void setParallelEUt(long parallelEUt) {
        this.parallelEUt = parallelEUt;
    }

    public int duration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int parallel() {
        return parallel;
    }

    public void setParallel(int parallel) {
        this.parallel = parallel;
    }

    @Override
    public @NotNull String toString() {
        return "OCResult[" +
                "EUt=" + eut + ", " +
                "duration=" + duration + ", " +
                "parallel=" + parallel + ", " +
                "parallelEUt=" + parallelEUt + ']';
    }
}
