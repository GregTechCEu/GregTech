package gregtech.api.fission.reactor.pathdata;

import com.github.bsideup.jabel.Desugar;

import gregtech.api.fission.component.FissionComponent;

import org.jetbrains.annotations.NotNull;

@Desugar
public record ReactivityPathData(@NotNull FissionComponent component, float reactivity) {
}
