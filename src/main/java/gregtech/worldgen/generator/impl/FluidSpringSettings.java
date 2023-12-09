package gregtech.worldgen.generator.impl;

import gregtech.worldgen.generator.SporadicSettings;
import gregtech.worldgen.placeable.WorldgenPlaceable;
import org.jetbrains.annotations.NotNull;

public class FluidSpringSettings extends GeneratorSettingsBase<FluidSpringGenerator> implements SporadicSettings<FluidSpringGenerator> {

    private final WorldgenPlaceable placeable;

    public FluidSpringSettings(@NotNull String name, int minY, int maxY, int weight, int size,
                               int @NotNull [] allowedDimensions, @NotNull String @NotNull [] allowedBiomes,
                               @NotNull WorldgenPlaceable placeable) {
        super(name, minY, maxY, weight, size, allowedDimensions, allowedBiomes);
        this.placeable = placeable;
    }

    public @NotNull WorldgenPlaceable placeable() {
        return this.placeable;
    }

    @Override
    public @NotNull FluidSpringGenerator createGenerator() {
        return new FluidSpringGenerator(this);
    }
}
