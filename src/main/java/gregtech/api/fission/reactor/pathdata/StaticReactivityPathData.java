package gregtech.api.fission.reactor.pathdata;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record StaticReactivityPathData(float reactivity) implements ReactivityPathData {

    @Override
    public float getReactivity() {
        return reactivity;
    }
}
