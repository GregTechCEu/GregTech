package gregtech.worldgen.generator;

import org.jetbrains.annotations.NotNull;

public interface GeneratorSettings<T extends WorldGenerator> {

    /**
     * @return the name of this object
     */
    @NotNull String name();

    /**
     * @return the weight of this object
     */
    int weight();

    /**
     * @return the dimensions this object can function in
     */
    int @NotNull [] allowedDimensions();

    @NotNull String @NotNull [] allowedBiomes();

    @NotNull T createGenerator();
}
