package gregtech.api.recipes.chance.output;

import com.google.common.collect.ImmutableList;
import gregtech.api.GTValues;
import gregtech.api.recipes.chance.boost.BoostableChanceEntry;
import gregtech.api.recipes.chance.boost.ChanceBoostFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;

/**
 * Logic for determining which chanced outputs should be produced from a list
 */
@FunctionalInterface
public interface ChancedOutputLogic {

    /**
     * The upper bound for rolling chances
     */
    int CHANCE_BOUND = 10_000;

    /**
     * Chanced Output Logic where any ingredients succeeding their roll will be produced
     */
    ChancedOutputLogic OR = new ChancedOutputLogic() {
        @Override
        public @Nullable @Unmodifiable <T> List<@NotNull ChancedOutput<?>> roll(@NotNull @Unmodifiable List<@NotNull ChancedOutput<T>> chancedEntries, @NotNull ChanceBoostFunction boostFunction) {
            ImmutableList.Builder<ChancedOutput<?>> builder = null;
            for (ChancedOutput<?> entry : chancedEntries) {
                if (passesChance(getChance(entry, boostFunction))) {
                    if (builder == null) builder = ImmutableList.builder();
                    builder.add(entry);
                }
            }
            return builder == null ? null : builder.build();
        }
    };

    /**
     * Chanced Output Logic where all ingredients must succeed their roll in order for any to be produced
     */
    ChancedOutputLogic AND = new ChancedOutputLogic() {
        @Override
        public @Nullable @Unmodifiable <T> List<@NotNull ChancedOutput<?>> roll(@NotNull @Unmodifiable List<@NotNull ChancedOutput<T>> chancedEntries, @NotNull ChanceBoostFunction boostFunction) {
            for (ChancedOutput<?> entry : chancedEntries) {
                if (!passesChance(getChance(entry, boostFunction))) {
                    return null;
                }
            }
            return ImmutableList.copyOf(chancedEntries);
        }
    };

    /**
     * Chanced Output Logic where only the first ingredient succeeding its roll will be produced
     */
    ChancedOutputLogic XOR = new ChancedOutputLogic() {
        @Override
        public @Nullable @Unmodifiable <T> List<@NotNull ChancedOutput<?>> roll(@NotNull @Unmodifiable List<@NotNull ChancedOutput<T>> chancedEntries, @NotNull ChanceBoostFunction boostFunction) {
            for (ChancedOutput<?> entry : chancedEntries) {
                if (passesChance(getChance(entry, boostFunction))) {
                    return Collections.singletonList(entry);
                }
            }
            return null;
        }
    };

    /**
     * @param entry         the entry to get the complete chance for
     * @param boostFunction the function boosting the entry's chance
     * @return the total chance for the entry
     */
    static int getChance(@NotNull ChancedOutput<?> entry, @NotNull ChanceBoostFunction boostFunction) {
        if (entry instanceof BoostableChanceEntry<?> boostableChanceEntry) {
            return boostFunction.getBoostedChance(boostableChanceEntry);
        }
        return entry.getChance();
    }

    /**
     * @param chance the chance to check
     * @return if the roll with the chance is successful
     */
    static boolean passesChance(int chance) {
        return GTValues.RNG.nextInt(CHANCE_BOUND) >= chance;
    }

    /**
     * Roll the chance and attempt to produce the output
     *
     * @param chancedEntries the list of entries to roll
     * @param boostFunction  the function to boost the entries' chances
     * @return a list of the produced outputs
     */
    <T> @Nullable @Unmodifiable List<@NotNull ChancedOutput<?>> roll(@NotNull @Unmodifiable List<@NotNull ChancedOutput<T>> chancedEntries,
                                                                 @NotNull ChanceBoostFunction boostFunction);
}
