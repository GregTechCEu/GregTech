package gregtech.api.fission.reactor.pathdata;

import gregtech.api.fission.component.ComponentDirection;
import gregtech.api.fission.component.FissionComponent;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Desugar
public record StaticNeutronPathData(@Nullable FissionComponent component, @NotNull ComponentDirection direction,
                                    float neutrons)
        implements NeutronPathData {

    @Override
    public float getNeutrons() {
        return neutrons;
    }
}
