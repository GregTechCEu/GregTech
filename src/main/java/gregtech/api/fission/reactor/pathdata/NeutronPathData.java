package gregtech.api.fission.reactor.pathdata;

import gregtech.api.fission.component.ComponentDirection;
import gregtech.api.fission.component.FissionComponent;

import gregtech.api.util.function.FloatSupplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface NeutronPathData {

    static @NotNull NeutronPathData of(@Nullable FissionComponent component, @NotNull ComponentDirection direction,
                                       @NotNull FloatSupplier neutrons) {
        return new DynamicNeutronPathData(component, direction, neutrons);
    }

    static @NotNull NeutronPathData of(@Nullable FissionComponent component, @NotNull ComponentDirection direction,
                                       float neutrons) {
        return new StaticNeutronPathData(component, direction, neutrons);
    }

    @Nullable FissionComponent component();

    @NotNull ComponentDirection direction();

    float getNeutrons();
}
