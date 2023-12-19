package gregtech.api.recipes.chance.output;

import gregtech.api.recipes.chance.boost.ChanceBoostFunction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;

/**
 * A list of rollable chanced outputs
 */
public class ChancedOutputList<I, T extends ChancedOutput<I>> {

    private final ChancedOutputLogic chancedOutputLogic;
    private final List<T> chancedEntries;

    public static <I, T extends ChancedOutput<I>> @NotNull ChancedOutputList<I, T> empty() {
        return new ChancedOutputList<>(ChancedOutputLogic.NONE, Collections.emptyList());
    }

    public ChancedOutputList(@NotNull ChancedOutputLogic chancedOutputLogic, @NotNull List<@NotNull T> chancedEntries) {
        this.chancedOutputLogic = chancedOutputLogic;
        this.chancedEntries = chancedEntries;
    }

    public @NotNull @Unmodifiable List<@NotNull T> getChancedEntries() {
        return chancedEntries;
    }

    /**
     * Roll the chances for this output list
     *
     * @param boostFunction the function used to boost the outputs
     * @param baseTier      the base tier of the recipe
     * @param machineTier   the tier the recipe is run at
     * @return a list of the rolled outputs
     */
    public @Nullable @Unmodifiable List<T> roll(@NotNull ChanceBoostFunction boostFunction, int baseTier,
                                                int machineTier) {
        return chancedOutputLogic.roll(getChancedEntries(), boostFunction, baseTier, machineTier);
    }

    public @NotNull ChancedOutputLogic getChancedOutputLogic() {
        return chancedOutputLogic;
    }

    @Override
    public String toString() {
        return chancedEntries.toString();
    }
}
