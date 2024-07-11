package gregtech.api.graphnet.pipenet.block;

import org.jetbrains.annotations.NotNull;

public record CableStructure(String name, int material, int insulation, int lossMult) implements IPipeStructure {

    @Override
    public @NotNull String getName() {
        return this.name();
    }
}
