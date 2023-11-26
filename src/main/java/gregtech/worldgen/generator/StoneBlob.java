package gregtech.worldgen.generator;

import gregtech.worldgen.placeable.WorldgenPlaceable;
import org.jetbrains.annotations.NotNull;

public class StoneBlob extends GeneratorSettingsBase<StoneBlobGenerator> {

    private final WorldgenPlaceable placeable;

    public StoneBlob(@NotNull String name, int minY, int maxY, int weight, int size,
                     int @NotNull [] allowedDimensions, @NotNull String @NotNull [] allowedBiomes,
                     @NotNull WorldgenPlaceable placeable) {
        super(name, minY, maxY, weight, size, allowedDimensions, allowedBiomes);
        this.placeable = placeable;
    }

    public @NotNull WorldgenPlaceable placeable() {
        return placeable;
    }

    @NotNull
    @Override
    public StoneBlobGenerator createGenerator() {
        return new StoneBlobGenerator(this);
    }
}
