package gregtech.common.pipelike.block.warpduct;

import gregtech.api.graphnet.pipenet.physical.IPipeStructure;
import gregtech.api.graphnet.pipenet.physical.PipeStructureRegistrationEvent;
import gregtech.client.renderer.pipe.PipeModelRedirector;
import gregtech.client.renderer.pipe.PipeModelRegistry;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Desugar
public record WarpDuctStructure(String name, int weight, float renderThickness, PipeModelRedirector model) implements
        IPipeStructure {

    public static final WarpDuctStructure NORMAL = new WarpDuctStructure("warp_duct_normal", 1, 0.625f,
            PipeModelRegistry.getWarpDuctModel(false));

    public static final WarpDuctStructure RESTRICTIVE = new WarpDuctStructure("warp_duct_restrictive", 1000, 0.625f,
            PipeModelRegistry.getWarpDuctModel(true));

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public float getRenderThickness() {
        return renderThickness;
    }

    @Override
    public boolean isPaintable() {
        return false;
    }

    @Override
    public PipeModelRedirector getModel() {
        return model;
    }

    public static void register(@NotNull PipeStructureRegistrationEvent event) {
        event.register(NORMAL);
        event.register(RESTRICTIVE);
    }
}
