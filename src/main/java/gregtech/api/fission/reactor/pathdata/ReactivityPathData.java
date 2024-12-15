package gregtech.api.fission.reactor.pathdata;

import gregtech.api.util.function.FloatSupplier;

import org.jetbrains.annotations.NotNull;

public interface ReactivityPathData {

    static @NotNull ReactivityPathData of(@NotNull FloatSupplier neutrons) {
        return new DynamicReactivityPathData(neutrons);
    }

    static @NotNull ReactivityPathData of(float neutrons) {
        return new StaticReactivityPathData(neutrons);
    }

    float getReactivity();
}
