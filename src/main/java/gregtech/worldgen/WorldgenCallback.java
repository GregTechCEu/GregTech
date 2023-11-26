package gregtech.worldgen;

import gregtech.api.util.math.ChunkPosDimension;
import gregtech.worldgen.generator.GeneratorSettings;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface WorldgenCallback<T extends GeneratorSettings<?>> {

    void receive(@NotNull ChunkPosDimension pos, @NotNull T value);
}
