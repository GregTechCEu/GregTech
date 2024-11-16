package gregtech.api.recipes.chance.output;

import gregtech.api.GTValues;
import gregtech.api.recipes.RecipeContext;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;

/**
 * Logic for determining which chanced outputs should be produced from a list
 */
public interface ChancedOutputLogic {

    /**
     * Chanced Output Logic where any ingredients succeeding their roll will be produced
     */
    ChancedOutputLogic OR = new ChancedOutputLogic() {

        @Override
        public @Nullable @Unmodifiable <I, T extends ChancedOutput<I>> List<@NotNull CalculatedOutput<I>> roll(
                                                                                                               @NotNull @Unmodifiable List<@NotNull T> chancedEntries,
                                                                                                               @NotNull RecipeContext<I> context) {
            ImmutableList.Builder<CalculatedOutput<I>> builder = ImmutableList.builder();
            boolean success = false;

            for (T entry : chancedEntries) {
                int chance = context.getChance(entry);
                if (passesChance(chance, entry, context)) {
                    success = true;
                    int amount = chance / entry.getMaxChance();
                    builder.add(new CalculatedOutput<>(entry, amount));
                }
            }

            return success ? builder.build() : null;
        }

        @Override
        public @NotNull String getTranslationKey() {
            return "gregtech.chance_logic.or";
        }

        @Override
        public String toString() {
            return "ChancedOutputLogic{OR}";
        }
    };

    /**
     * Chanced Output Logic where all ingredients must succeed their roll in order for any to be produced
     */
    ChancedOutputLogic AND = new ChancedOutputLogic() {

        @Override
        public @Nullable @Unmodifiable <I, T extends ChancedOutput<I>> List<@NotNull CalculatedOutput<I>> roll(
                                                                                                               @NotNull @Unmodifiable List<@NotNull T> chancedEntries,
                                                                                                               @NotNull RecipeContext<I> context) {
            ImmutableList.Builder<CalculatedOutput<I>> builder = ImmutableList.builder();
            boolean failed = false;
            for (T entry : chancedEntries) {
                int chance = context.getChance(entry);
                int amount = chance / entry.getMaxChance();
                builder.add(new CalculatedOutput<>(entry, amount));
                if (!passesChance(chance, entry, context)) {
                    failed = true;
                }
            }
            return failed ? null : builder.build();
        }

        @Override
        public @NotNull String getTranslationKey() {
            return "gregtech.chance_logic.and";
        }

        @Override
        public String toString() {
            return "ChancedOutputLogic{AND}";
        }
    };

    /**
     * Chanced Output Logic where only the first ingredient succeeding its roll will be produced
     */
    ChancedOutputLogic XOR = new ChancedOutputLogic() {

        private int roll;

        @Override
        public @Nullable @Unmodifiable <I, T extends ChancedOutput<I>> List<@NotNull CalculatedOutput<I>> roll(
                                                                                                               @NotNull @Unmodifiable List<@NotNull T> chancedEntries,
                                                                                                               @NotNull RecipeContext<I> context) {
            CalculatedOutput<I> selected = null;
            int total = 1;
            for (T entry : chancedEntries) {
                total += entry.getMaxChance();
            }

            roll = GTValues.RNG.nextInt(total);
            for (T entry : chancedEntries) {
                int chance = context.getChance(entry);
                if (passesChance(chance, entry, context) && selected == null) {
                    selected = new CalculatedOutput<>(entry);
                }
            }
            return selected == null ? null : Collections.singletonList(selected);
        }

        @Override
        public <I, T extends ChancedOutput<I>> boolean passesChance(int chance, T entry,
                                                                    @NotNull RecipeContext<I> context) {
            boolean b = chance >= roll;
            if (!b) roll -= chance;
            context.updateCachedChance(entry, chance);
            return b;
        }

        @Override
        public @NotNull String getTranslationKey() {
            return "gregtech.chance_logic.xor";
        }

        @Override
        public String toString() {
            return "ChancedOutputLogic{XOR}";
        }
    };

    /**
     * Chanced Output Logic where nothing is produced
     */
    ChancedOutputLogic NONE = new ChancedOutputLogic() {

        @Override
        public @Nullable @Unmodifiable <I, T extends ChancedOutput<I>> List<@NotNull CalculatedOutput<I>> roll(
                                                                                                               @NotNull @Unmodifiable List<@NotNull T> chancedEntries,
                                                                                                               @NotNull RecipeContext<I> context) {
            return null;
        }

        @Override
        public @NotNull String getTranslationKey() {
            return "gregtech.chance_logic.none";
        }

        @Override
        public String toString() {
            return "ChancedOutputLogic{NONE}";
        }
    };

    /**
     * @param chance  the boosted chance to be checked
     * @param entry   the entry to get the max chance and ingredient of for comparison and cache
     * @param context Context containing machine and recipe tier, the boost function, and the chance cache
     * @return if the roll with the chance is successful
     */
    default <I, T extends ChancedOutput<I>> boolean passesChance(int chance, T entry,
                                                                 @NotNull RecipeContext<I> context) {
        if (context.getCachedChance(entry) == -1) {
            int initial = GTValues.RNG.nextInt(entry.getMaxChance());
            context.updateCachedChance(entry, chance);
            return initial <= entry.getChance();
        }

        boolean roll = false;
        int fullChance = context.getChance(entry);
        if (fullChance >= entry.getMaxChance()) {
            roll = true;
        }

        context.updateCachedChance(entry, fullChance);
        return roll;
    }

    /**
     * @return the upper bound for rolling chances
     */
    static int getMaxChancedValue() {
        return 10_000;
    }

    // /**
    // * Roll the chance and attempt to produce the output
    // *
    // * @param chancedEntries the list of entries to roll
    // * @param boostFunction the function to boost the entries' chances
    // * @param baseTier the base tier of the recipe
    // * @param machineTier the tier the recipe is run at
    // * @param cache the cache of previously rolled chances, can be null
    // * @return a list of the produced outputs, or null if failed
    // */
    // <I, T extends ChancedOutput<I>> @Nullable @Unmodifiable List<@NotNull T> roll(
    // @NotNull @Unmodifiable List<@NotNull T> chancedEntries,
    // @NotNull ChanceBoostFunction boostFunction,
    // int baseTier, int machineTier,
    // @Nullable Map<I, Integer> cache);

    // /**
    // * Roll the chance and attempt to produce the output
    // *
    // * @param chancedEntries the list of entries to roll
    // * @param boostFunction the function to boost the entries' chances
    // * @param baseTier the base tier of the recipe
    // * @param machineTier the tier the recipe is run at
    // * @return a list of the produced outputs
    // */
    // default <I, T extends ChancedOutput<I>> @Nullable @Unmodifiable List<@NotNull T> roll(
    // @NotNull @Unmodifiable List<@NotNull T> chancedEntries,
    // @NotNull ChanceBoostFunction boostFunction,
    // int baseTier,
    // int machineTier) {
    // return roll(chancedEntries, boostFunction, baseTier, machineTier, null);
    // }

    <I, T extends ChancedOutput<I>> @Nullable @Unmodifiable List<@NotNull CalculatedOutput<I>> roll(
                                                                                                    @NotNull @Unmodifiable List<@NotNull T> chancedEntries,
                                                                                                    @NotNull RecipeContext<I> context);

    @NotNull
    String getTranslationKey();
}
