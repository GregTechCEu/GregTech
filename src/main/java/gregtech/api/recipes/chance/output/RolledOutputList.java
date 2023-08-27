package gregtech.api.recipes.chance.output;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A list of chanced outputs whose chances have already been rolled
 */
public final class RolledOutputList<I, T extends ChancedOutput<I>> {

    private final List<T> producedOutputs;

    public RolledOutputList(@NotNull List<@NotNull T> producedOutputs) {
        this.producedOutputs = producedOutputs;
    }

    public @NotNull List<T> getProducedOutputs() {
        return this.producedOutputs;
    }
}
