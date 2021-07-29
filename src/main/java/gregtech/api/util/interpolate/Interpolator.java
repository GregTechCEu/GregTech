package gregtech.api.util.interpolate;

import net.minecraft.util.ITickable;

import java.util.function.Consumer;

public class Interpolator implements ITickable {
    float from;
    float to;
    int duration;
    IEase ease;
    Consumer<Number> callback;

    int tick = -1;

    public Interpolator(float from, float to, int duration, IEase ease, Consumer<Number> callback) {
        this.from = from;
        this.to = to;
        this.duration = duration;
        this.ease = ease;
        this.callback = callback;
    }

    public void reset() {
        tick = -1;
    }

    public void start() {
        tick = 0;
    }

    public boolean isFinish(){
        return tick == duration;
    }

    @Override
    public void update() {
        if (tick < 0 || tick >= duration) return;
        callback.accept(ease.getInterpolation(tick * 1.0f / duration) * (to - from) + from);
        tick++;
    }
}
