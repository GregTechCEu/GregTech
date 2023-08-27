package gregtech.api.recipes.chance.output;

import gregtech.api.recipes.chance.boost.ChanceBoostFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * A list of rollable chanced outputs
 */
public class ChancedOutputList<T> {

    private final ChancedOutputLogic chancedOutputLogic;
    private final List<ChancedOutput<T>> chancedEntries;

    public ChancedOutputList(@NotNull ChancedOutputLogic chancedOutputLogic, @NotNull List<@NotNull ChancedOutput<T>> chancedEntries) {
        this.chancedOutputLogic = chancedOutputLogic;
        this.chancedEntries = chancedEntries;
    }

    public @NotNull @Unmodifiable List<@NotNull ChancedOutput<T>> getChancedEntries() {
        return chancedEntries;
    }

    /**
     * Roll the chances for this output list
     *
     * @param boostFunction the function used to boost the outputs
     * @return a list of the rolled outputs
     */
    public @Nullable RolledOutputList roll(@NotNull ChanceBoostFunction boostFunction) {
        List<ChancedOutput<?>> list = chancedOutputLogic.roll(getChancedEntries(), boostFunction);
        return list == null ? null : new RolledOutputList(list);
    }
}
