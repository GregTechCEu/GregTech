package gregtech.api.fission.reactor.pathdata;

import gregtech.api.fission.component.ComponentDirection;
import gregtech.api.fission.component.FissionComponent;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.Nullable;

@Desugar
public record NeutronPathData(@Nullable FissionComponent component, ComponentDirection direction, int neutrons) {

}
