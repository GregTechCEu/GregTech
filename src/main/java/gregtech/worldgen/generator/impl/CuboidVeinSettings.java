package gregtech.worldgen.generator.impl;

import gregtech.worldgen.generator.ChunkAlignedSettings;
import gregtech.worldgen.generator.ChunkAlignedWorldGenerator;
import gregtech.worldgen.placeable.WorldgenPlaceable;
import org.jetbrains.annotations.NotNull;

public abstract class CuboidVeinSettings<T extends ChunkAlignedWorldGenerator> extends GeneratorSettingsBase<T>
        implements ChunkAlignedSettings<T> {

    protected final WorldgenPlaceable indicator;

    protected CuboidVeinSettings(@NotNull String name, int minY, int maxY, int weight, int size,
                                 int @NotNull [] allowedDimensions, @NotNull String @NotNull [] allowedBiomes,
                                 @NotNull WorldgenPlaceable indicator) {
        super(name, minY, maxY, weight, size, allowedDimensions, allowedBiomes);
        this.indicator = indicator;
    }

    public @NotNull WorldgenPlaceable indicator() {
        return indicator;
    }
}
