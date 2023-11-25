package gregtech.worldgen.generator;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Base class storing general info about world generators
 */
public abstract class WorldGeneratorBase implements GeneratorEntry {

    protected final String name;

    protected final int minY;
    protected final int maxY;

    protected final int weight;

    protected final int size;

    protected final int[] allowedDimensions;
    protected final String[] allowedBiomes;

    protected WorldGeneratorBase(@NotNull String name, int minY, int maxY, int weight, int size,
                                 int @NotNull [] allowedDimensions, @NotNull String @NotNull [] allowedBiomes) {
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
    public int getWeight() {
        return this.weight;
    }

    @Override
    public int @NotNull [] getDimensions() {
        return this.allowedDimensions;
    }

    /**
     * @return the size of the world generator
     */
    public int getSize() {
        return this.size;
    }

    /**
     * @param dimension the dimension to check
     * @return if the vein can generate in the dimension
     */
    public boolean canGenerateInDimension(int dimension) {
        if (allowedDimensions.length == 0) return true;
        return ArrayUtils.contains(allowedDimensions, dimension);
    }

    /**
     * @param biome the biome to check
     * @return if the vein can generate in the biome
     */
    public boolean canGenerateInBiome(@NotNull String biome) {
        if (allowedBiomes.length == 0) return true;
        return ArrayUtils.contains(allowedBiomes, biome);
    }
}
