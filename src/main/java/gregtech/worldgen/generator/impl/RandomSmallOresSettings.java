package gregtech.worldgen.generator.impl;

import com.google.common.base.Preconditions;
import gregtech.worldgen.generator.SporadicSettings;
import gregtech.worldgen.placeable.WorldgenPlaceable;
import org.jetbrains.annotations.NotNull;

public class RandomSmallOresSettings extends GeneratorSettingsBase<RandomSmallOresGenerator> implements SporadicSettings<RandomSmallOresGenerator> {

    private final WorldgenPlaceable placeable;

    public RandomSmallOresSettings(@NotNull String name, int minY, int maxY, int size,
                                      int @NotNull [] allowedDimensions, @NotNull String @NotNull [] allowedBiomes,
                                      @NotNull WorldgenPlaceable placeable) {
        super(name, minY, maxY, 1, size, allowedDimensions, allowedBiomes);
        Preconditions.checkArgument(placeable.hasSmall(), "hasSmall");
        this.placeable = placeable;
    }

    public @NotNull WorldgenPlaceable placeable() {
        return placeable;
    }

    @Override
    public @NotNull RandomSmallOresGenerator createGenerator() {
        return new RandomSmallOresGenerator(this);
    }
}
