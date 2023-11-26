package gregtech.worldgen.generator;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

/**
 * Base class storing general info about world generators
 */
public abstract class GeneratorSettingsBase implements GeneratorSettings {

    protected final String name;

    protected final int minY;
    protected final int maxY;

    protected final int weight;

    protected final int size;

    protected final int[] allowedDimensions;
    protected final String[] allowedBiomes;

    protected GeneratorSettingsBase(@NotNull String name, int minY, int maxY, int weight, int size, int @NotNull [] allowedDimensions, @NotNull String @NotNull [] allowedBiomes) {
        this.name = name;
        Preconditions.checkArgument(minY < maxY, "minY < maxY");
        this.minY = minY;
        this.maxY = maxY;
        this.weight = weight;
        this.size = size;
        this.allowedDimensions = allowedDimensions;
        this.allowedBiomes = allowedBiomes;
    }

    @Override
    public int weight() {
        return this.weight;
    }

    public @NotNull String name() {
        return name;
    }

    public int minY() {
        return minY;
    }

    public int maxY() {
        return maxY;
    }

    public int size() {
        return size;
    }

    public int @NotNull [] allowedDimensions() {
        return allowedDimensions;
    }

    public @NotNull String @NotNull [] allowedBiomes() {
        return allowedBiomes;
    }
}
