package gregtech.common.pipelike.block.pipe;

import gregtech.api.graphnet.pipenet.physical.IPipeMaterialStructure;
import gregtech.api.unification.ore.OrePrefix;

import gregtech.common.pipelike.block.cable.CableStructure;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public record PipeStructure(String name, int material, int channelCount, boolean restrictive, OrePrefix ore, float renderThickness)
        implements IPipeMaterialStructure {

    public static final PipeStructure TINY = new PipeStructure("tiny", 1, 1, false, OrePrefix.pipeTiny, 0.25f);
    public static final PipeStructure SMALL = new PipeStructure("small", 2, 1, false, OrePrefix.pipeSmall, 0.375f);
    public static final PipeStructure NORMAL = new PipeStructure("normal", 6, 1, false, OrePrefix.pipeNormal, 0.5f);
    public static final PipeStructure LARGE = new PipeStructure("large", 12, 1, false, OrePrefix.pipeLarge, 0.75f);
    public static final PipeStructure HUGE = new PipeStructure("huge", 24, 1, false, OrePrefix.pipeHuge, 0.875f);

    // TODO adjust quadruple and nonuple thicknesses to 0.5f and 0.75f respectively, and update textures accordingly
    public static final PipeStructure QUADRUPLE = new PipeStructure("quadruple", 8, 4, false, OrePrefix.pipeQuadruple, 0.95f);
    public static final PipeStructure NONUPLE = new PipeStructure("nonuple", 18, 9, false, OrePrefix.pipeNonuple, 0.95f);


    public static final PipeStructure TINY_RESTRICTIVE = new PipeStructure("tiny_restrictive", 1, 1, true, OrePrefix.pipeTinyRestrictive, 0.25f);
    public static final PipeStructure SMALL_RESTRICTIVE = new PipeStructure("small_restrictive", 2, 1, true, OrePrefix.pipeSmallRestrictive, 0.375f);
    public static final PipeStructure NORMAL_RESTRICTIVE = new PipeStructure("normal_restrictive", 6, 1, true, OrePrefix.pipeNormalRestrictive, 0.5f);
    public static final PipeStructure LARGE_RESTRICTIVE = new PipeStructure("large_restrictive", 12, 1, true, OrePrefix.pipeLargeRestrictive, 0.75f);
    public static final PipeStructure HUGE_RESTRICTIVE = new PipeStructure("huge_restrictive", 24, 1, true, OrePrefix.pipeHugeRestrictive, 0.875f);

    public static final PipeStructure QUADRUPLE_RESTRICTIVE = new PipeStructure("quadruple_restrictive", 8, 4, true, OrePrefix.pipeQuadrupleRestrictive, 0.95f);
    public static final PipeStructure NONUPLE_RESTRICTIVE = new PipeStructure("nonuple_restrictive", 18, 9, true, OrePrefix.pipeNonupleRestrictive, 0.95f);

    @Override
    public @NotNull String getName() {
        return this.name();
    }

    @Override
    public OrePrefix getOrePrefix() {
        return this.ore();
    }

    @Override
    public float getRenderThickness() {
        return this.renderThickness();
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
