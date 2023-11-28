package gregtech.worldgen.generator.impl;

import com.google.common.base.Preconditions;
import gregtech.worldgen.placeable.WorldgenPlaceable;
import org.jetbrains.annotations.NotNull;

public class MixedVeinSettings extends CuboidVeinSettings<MixedVeinGenerator> {

    private final int[] densities;
    private final WorldgenPlaceable[] placeables;
    private final int height;

    public MixedVeinSettings(@NotNull String name, int minY, int maxY, int weight, int size,
                             int @NotNull [] allowedDimensions, @NotNull String @NotNull [] allowedBiomes,
                             int @NotNull [] densities, @NotNull WorldgenPlaceable @NotNull [] placeables, int height) {
        super(name, minY, maxY, weight, size, allowedDimensions, allowedBiomes);
        Preconditions.checkArgument(densities.length == placeables.length, "densities.length != placeables.length");
        this.densities = densities;
        this.placeables = placeables;
        this.height = height;
    }

    public int @NotNull [] densities() {
        return densities;
    }

    public @NotNull WorldgenPlaceable @NotNull [] placeables() {
        return placeables;
    }

    public int height() {
        return height;
    }

    @Override
    public @NotNull MixedVeinGenerator createGenerator() {
        return new MixedVeinGenerator(this);
    }
}
