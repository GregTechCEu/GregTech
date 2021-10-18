package gregtech.api.util.interpolate;

import gregtech.api.util.function.TriConsumer;
import net.minecraft.util.ITickable;

public class RGBInterpolator implements ITickable {
    private final int speed;
    private float r = 255;
    private float g = 0;
    private float b = 0;
    private final TriConsumer<Number, Number, Number> interpolate;
    private final TriConsumer<Number, Number, Number> callback;
    private boolean isOn = false;

    public RGBInterpolator(int speed, TriConsumer<Number, Number, Number> interpolate, TriConsumer<Number, Number, Number> callback) {
        this.speed = speed;
        this.interpolate = interpolate;
        this.callback = callback;
    }

    @Override
    public void update() {
        if (isOn) {
            if (r == 255 && g < 255) {
                b -= Math.min(speed, b);
                g += Math.min(speed, 255 - g);
            } else if (g == 255 && b < 255) {
                r -= Math.min(speed, r);
                b += Math.min(speed, 255 - b);
            } else if (b == 255 && r < 255) {
                g -= Math.min(speed, g);
                r += Math.min(speed, 255 - r);
            }
            interpolate.accept(r / 255, g / 255, b / 255);
        }
    }

    public void stop() {
        callback.accept(r, g, b);
        isOn = false;
    }

    public void start() {
        isOn = true;
    }

    public boolean isActivated() {
        return isOn;
    }
}
