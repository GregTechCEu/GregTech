package gregtech.api.fission.reactor.pathdata;

import gregtech.api.util.function.FloatSupplier;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.NotNull;

@Desugar
public record DynamicReactivityPathData(@NotNull FloatSupplier reactivity)
        implements ReactivityPathData {

    @Override
    public float getReactivity() {
        return reactivity.getAsFloat();
    }
}
