package gregtech.worldgen.generator;

import org.jetbrains.annotations.NotNull;

public interface GeneratorEntry {

    /**
     * @return the weight of this object
     */
    int getWeight();

    /**
     * @return the dimensions this object can function in
     */
    int @NotNull [] getDimensions();
}
