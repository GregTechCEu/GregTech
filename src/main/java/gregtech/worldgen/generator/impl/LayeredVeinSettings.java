package gregtech.worldgen.generator.impl;

import gregtech.worldgen.placeable.WorldgenPlaceable;
import org.jetbrains.annotations.NotNull;

public class LayeredVeinSettings extends CuboidVeinSettings<LayeredVeinGenerator> {

    private final int density;

    private final WorldgenPlaceable top;
    private final WorldgenPlaceable middle;
    private final WorldgenPlaceable bottom;
    private final WorldgenPlaceable spread;

    public LayeredVeinSettings(@NotNull String name, int minY, int maxY, int weight, int size,
                               int @NotNull [] allowedDimensions, @NotNull String @NotNull [] allowedBiomes,
                               @NotNull WorldgenPlaceable indicator, int density, @NotNull WorldgenPlaceable top,
                               @NotNull WorldgenPlaceable middle, @NotNull WorldgenPlaceable bottom,
                               @NotNull WorldgenPlaceable spread) {
        super(name, minY, maxY, weight, size, allowedDimensions, allowedBiomes, indicator);
        this.density = density;
        this.top = top;
        this.middle = middle;
        this.bottom = bottom;
        this.spread = spread;
    }

    public int density() {
        return density;
    }

    public @NotNull WorldgenPlaceable top() {
        return top;
    }

    public @NotNull WorldgenPlaceable middle() {
        return middle;
    }

    public @NotNull WorldgenPlaceable bottom() {
        return bottom;
    }

    public @NotNull WorldgenPlaceable spread() {
        return spread;
    }

    @Override
    public @NotNull LayeredVeinGenerator createGenerator() {
        return new LayeredVeinGenerator(this);
    }
}
