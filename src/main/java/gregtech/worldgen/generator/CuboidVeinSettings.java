package gregtech.worldgen.generator;

import org.jetbrains.annotations.NotNull;

public abstract class CuboidVeinSettings<T extends ChunkAlignedWorldGenerator> extends GeneratorSettingsBase<T>
        implements ChunkAlignedSettings<T> {

    protected CuboidVeinSettings(@NotNull String name, int minY, int maxY, int weight, int size,
                              int @NotNull [] allowedDimensions, @NotNull String @NotNull [] allowedBiomes) {
        super(name, minY, maxY, weight, size, allowedDimensions, allowedBiomes);
    }
}
