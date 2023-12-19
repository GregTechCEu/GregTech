package gregtech.api.util.interpolate;

import net.minecraft.util.ITickable;

import java.util.function.Consumer;

public class Interpolator implements ITickable {

    private final float from;
    private final float to;
    private final int duration;
    private final IEase ease;
    private final Consumer<Number> interpolate;
    private final Consumer<Number> callback;

    private int tick = 0;

    public Interpolator(float from, float to, int duration, IEase ease, Consumer<Number> interpolate) {
        this(from, to, duration, ease, interpolate, null);
    }

    public Interpolator(float from, float to, int duration, IEase ease, Consumer<Number> interpolate,
                        Consumer<Number> callback) {
        this.from = from;
        this.to = to;
        this.duration = duration;
        this.ease = ease;
        this.interpolate = interpolate;
        this.callback = callback;
    }

    public void reset() {
        tick = 0;
    }

    public Interpolator start() {
        tick = 1;
        return this;
    }

    public boolean isFinish() {
        return tick == duration;
    }

    @Override
    public void update() {
        if (tick < 1 || tick > duration) return;
        if (tick == duration) {
            callback.accept(ease.getInterpolation(tick * 1.0f / duration) * (to - from) + from);
        }
        interpolate.accept(ease.getInterpolation(tick * 1.0f / duration) * (to - from) + from);
        tick++;
    }
}
