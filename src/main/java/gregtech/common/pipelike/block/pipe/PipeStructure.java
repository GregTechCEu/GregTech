package gregtech.common.pipelike.block.pipe;

import gregtech.api.graphnet.pipenet.physical.IPipeChanneledStructure;
import gregtech.api.graphnet.pipenet.physical.IPipeMaterialStructure;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.client.renderer.pipe.AbstractPipeModel;
import gregtech.client.renderer.pipe.PipeModel;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Desugar
public record PipeStructure(String name, int material, int channelCount, boolean restrictive, OrePrefix ore,
                            float renderThickness, PipeModel model)
        implements IPipeMaterialStructure, IPipeChanneledStructure {

    public static final PipeStructure TINY = new PipeStructure("pipe_tiny", 1, 1, false, OrePrefix.pipeTiny, 0.25f,
            PipeModel.INSTANCES[0]);
    public static final PipeStructure SMALL = new PipeStructure("pipe_small", 2, 1, false, OrePrefix.pipeSmall, 0.375f,
            PipeModel.INSTANCES[1]);
    public static final PipeStructure NORMAL = new PipeStructure("pipe_normal", 6, 1, false, OrePrefix.pipeNormal, 0.5f,
            PipeModel.INSTANCES[2]);
    public static final PipeStructure LARGE = new PipeStructure("pipe_large", 12, 1, false, OrePrefix.pipeLarge, 0.75f,
            PipeModel.INSTANCES[3]);
    public static final PipeStructure HUGE = new PipeStructure("pipe_huge", 24, 1, false, OrePrefix.pipeHuge, 0.875f,
            PipeModel.INSTANCES[4]);

    public static final PipeStructure QUADRUPLE = new PipeStructure("pipe_quadruple", 8, 4, false, OrePrefix.pipeQuadruple,
            0.95f, PipeModel.INSTANCES[5]);
    public static final PipeStructure NONUPLE = new PipeStructure("pipe_nonuple", 18, 9, false, OrePrefix.pipeNonuple, 0.95f,
            PipeModel.INSTANCES[6]);

    public static final PipeStructure TINY_RESTRICTIVE = new PipeStructure("pipe_tiny_restrictive", 1, 1, true,
            OrePrefix.pipeTinyRestrictive, 0.25f, PipeModel.RESTRICTIVE_INSTANCES[0]);
    public static final PipeStructure SMALL_RESTRICTIVE = new PipeStructure("pipe_small_restrictive", 2, 1, true,
            OrePrefix.pipeSmallRestrictive, 0.375f, PipeModel.RESTRICTIVE_INSTANCES[1]);
    public static final PipeStructure NORMAL_RESTRICTIVE = new PipeStructure("pipe_normal_restrictive", 6, 1, true,
            OrePrefix.pipeNormalRestrictive, 0.5f, PipeModel.RESTRICTIVE_INSTANCES[2]);
    public static final PipeStructure LARGE_RESTRICTIVE = new PipeStructure("pipe_large_restrictive", 12, 1, true,
            OrePrefix.pipeLargeRestrictive, 0.75f, PipeModel.RESTRICTIVE_INSTANCES[3]);
    public static final PipeStructure HUGE_RESTRICTIVE = new PipeStructure("pipe_huge_restrictive", 24, 1, true,
            OrePrefix.pipeHugeRestrictive, 0.875f, PipeModel.RESTRICTIVE_INSTANCES[4]);

    public static final PipeStructure QUADRUPLE_RESTRICTIVE = new PipeStructure("pipe_quadruple_restrictive", 8, 4, true,
            OrePrefix.pipeQuadrupleRestrictive, 0.95f, PipeModel.RESTRICTIVE_INSTANCES[5]);
    public static final PipeStructure NONUPLE_RESTRICTIVE = new PipeStructure("pipe_nonuple_restrictive", 18, 9, true,
            OrePrefix.pipeNonupleRestrictive, 0.95f, PipeModel.RESTRICTIVE_INSTANCES[6]);

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
    public AbstractPipeModel<?> getModel() {
        return model;
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    public static void registerDefaultStructures(Consumer<PipeStructure> register) {
        register.accept(TINY);
        register.accept(SMALL);
        register.accept(NORMAL);
        register.accept(LARGE);
        register.accept(HUGE);
        register.accept(QUADRUPLE);
        register.accept(NONUPLE);
        register.accept(TINY_RESTRICTIVE);
        register.accept(SMALL_RESTRICTIVE);
        register.accept(NORMAL_RESTRICTIVE);
        register.accept(LARGE_RESTRICTIVE);
        register.accept(HUGE_RESTRICTIVE);
        register.accept(QUADRUPLE_RESTRICTIVE);
        register.accept(NONUPLE_RESTRICTIVE);
    }
}
