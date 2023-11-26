package gregtech.worldgen.generator;

import org.jetbrains.annotations.NotNull;

public interface GeneratorSettings {

    /**
     * @return the weight of this object
     */
    int weight();

    /**
     * @return the dimensions this object can function in
     */
    int @NotNull [] allowedDimensions();

    @NotNull String @NotNull [] allowedBiomes();
}
