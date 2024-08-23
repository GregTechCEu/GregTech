package gregtech.common.pipelike.block.pipe;

import gregtech.api.graphnet.pipenet.physical.IPipeChanneledStructure;
import gregtech.api.graphnet.pipenet.physical.IPipeMaterialStructure;
import gregtech.api.graphnet.pipenet.physical.PipeStructureRegistry;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.client.renderer.pipe.PipeModelRedirector;
import gregtech.client.renderer.pipe.PipeModelRegistry;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Desugar
public record MaterialPipeStructure(String name, int material, int channelCount, boolean restrictive, OrePrefix ore,
                                    float renderThickness, PipeModelRedirector model)
        implements IPipeMaterialStructure, IPipeChanneledStructure {

    public static final MaterialPipeStructure TINY = new MaterialPipeStructure("pipe_tiny", 1, 1, false,
            OrePrefix.pipeTiny, 0.25f, PipeModelRegistry.getPipeModel(0));
    public static final MaterialPipeStructure SMALL = new MaterialPipeStructure("pipe_small", 2, 1, false,
            OrePrefix.pipeSmall, 0.375f, PipeModelRegistry.getPipeModel(1));
    public static final MaterialPipeStructure NORMAL = new MaterialPipeStructure("pipe_normal", 6, 1, false,
            OrePrefix.pipeNormal, 0.5f, PipeModelRegistry.getPipeModel(2));
    public static final MaterialPipeStructure LARGE = new MaterialPipeStructure("pipe_large", 12, 1, false,
            OrePrefix.pipeLarge, 0.75f, PipeModelRegistry.getPipeModel(3));
    public static final MaterialPipeStructure HUGE = new MaterialPipeStructure("pipe_huge", 24, 1, false,
            OrePrefix.pipeHuge, 0.875f, PipeModelRegistry.getPipeModel(4));

    public static final MaterialPipeStructure QUADRUPLE = new MaterialPipeStructure("pipe_quadruple", 8, 4, false,
            OrePrefix.pipeQuadruple, 0.95f, PipeModelRegistry.getPipeModel(5));
    public static final MaterialPipeStructure NONUPLE = new MaterialPipeStructure("pipe_nonuple", 18, 9, false,
            OrePrefix.pipeNonuple, 0.95f, PipeModelRegistry.getPipeModel(6));

    public static final MaterialPipeStructure TINY_RESTRICTIVE = new MaterialPipeStructure("pipe_tiny_restrictive", 1,
            1, true, OrePrefix.pipeTinyRestrictive, 0.25f, PipeModelRegistry.getPipeRestrictiveModel(0));
    public static final MaterialPipeStructure SMALL_RESTRICTIVE = new MaterialPipeStructure("pipe_small_restrictive", 2,
            1, true, OrePrefix.pipeSmallRestrictive, 0.375f, PipeModelRegistry.getPipeRestrictiveModel(1));
    public static final MaterialPipeStructure NORMAL_RESTRICTIVE = new MaterialPipeStructure("pipe_normal_restrictive",
            6, 1, true, OrePrefix.pipeNormalRestrictive, 0.5f, PipeModelRegistry.getPipeRestrictiveModel(2));
    public static final MaterialPipeStructure LARGE_RESTRICTIVE = new MaterialPipeStructure("pipe_large_restrictive",
            12, 1, true, OrePrefix.pipeLargeRestrictive, 0.75f, PipeModelRegistry.getPipeRestrictiveModel(3));
    public static final MaterialPipeStructure HUGE_RESTRICTIVE = new MaterialPipeStructure("pipe_huge_restrictive", 24,
            1, true, OrePrefix.pipeHugeRestrictive, 0.875f, PipeModelRegistry.getPipeRestrictiveModel(4));

    public static final MaterialPipeStructure QUADRUPLE_RESTRICTIVE = new MaterialPipeStructure(
            "pipe_quadruple_restrictive", 8, 4, true, OrePrefix.pipeQuadrupleRestrictive, 0.95f,
            PipeModelRegistry.getPipeRestrictiveModel(5));
    public static final MaterialPipeStructure NONUPLE_RESTRICTIVE = new MaterialPipeStructure(
            "pipe_nonuple_restrictive", 18, 9, true, OrePrefix.pipeNonupleRestrictive, 0.95f,
            PipeModelRegistry.getPipeRestrictiveModel(6));

    public MaterialPipeStructure(String name, int material, int channelCount, boolean restrictive, OrePrefix ore,
                                 float renderThickness, PipeModelRedirector model) {
        this.name = name;
        this.material = material;
        this.channelCount = channelCount;
        this.restrictive = restrictive;
        this.ore = ore;
        this.renderThickness = renderThickness;
        this.model = model;
        PipeStructureRegistry.register(this);
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public OrePrefix getOrePrefix() {
        return ore;
    }

    @Override
    public float getRenderThickness() {
        return renderThickness;
    }

    @Override
    public int getChannelCount() {
        return channelCount;
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @Override
    public PipeModelRedirector getModel() {
        return model;
    }
}
