package gregtech.api.recipes.chance.output;

import gregtech.api.GTValues;
import gregtech.api.recipes.chance.boost.BoostableChanceEntry;
import gregtech.api.recipes.chance.boost.ChanceBoostFunction;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Logic for determining which chanced outputs should be produced from a list
 */
public interface ChancedOutputLogic {

    /**
     * Chanced Output Logic where any ingredients succeeding their roll will be produced
     */
    ChancedOutputLogic OR = new ChancedOutputLogic() {

        @Override
        public @Nullable @Unmodifiable <I,
                T extends ChancedOutput<I>> List<@NotNull T> roll(@NotNull @Unmodifiable List<@NotNull T> chancedEntries,
                                                                  @NotNull ChanceBoostFunction boostFunction,
                                                                  int baseTier, int machineTier,
                                                                  @Nullable Map<I, Integer> cache) {
            ImmutableList.Builder<T> builder = ImmutableList.builder();

            for (T entry : chancedEntries) {
                int chance = getChance(entry, boostFunction, baseTier, machineTier);
                if (passesChance(chance, entry, cache)) {
                    builder.add(entry);
                }
            }

            List<T> list = builder.build();
            return list.size() == 0 ? null : list;
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
        public @Nullable @Unmodifiable <I,
                T extends ChancedOutput<I>> List<@NotNull T> roll(@NotNull @Unmodifiable List<@NotNull T> chancedEntries,
                                                                  @NotNull ChanceBoostFunction boostFunction,
                                                                  int baseTier, int machineTier,
                                                                  @Nullable Map<I, Integer> cache) {
            boolean failed = false;
            for (T entry : chancedEntries) {
                int chance = getChance(entry, boostFunction, baseTier, machineTier);
                if (!passesChance(chance, entry, cache)) {
                    failed = true;
                }
            }
            return failed ? null : ImmutableList.copyOf(chancedEntries);
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

        @Override
        public @Nullable @Unmodifiable <I,
                T extends ChancedOutput<I>> List<@NotNull T> roll(@NotNull @Unmodifiable List<@NotNull T> chancedEntries,
                                                                  @NotNull ChanceBoostFunction boostFunction,
                                                                  int baseTier, int machineTier,
                                                                  @Nullable Map<I, Integer> cache) {
            T selected = null;
            for (T entry : chancedEntries) {
                int chance = getChance(entry, boostFunction, baseTier, machineTier);
                if (passesChance(chance, entry, cache) && selected == null) {
                    selected = entry;
                }
            }
            return selected == null ? null : Collections.singletonList(selected);
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
        public @Nullable @Unmodifiable <I,
                T extends ChancedOutput<I>> List<@NotNull T> roll(@NotNull @Unmodifiable List<@NotNull T> chancedEntries,
                                                                  @NotNull ChanceBoostFunction boostFunction,
                                                                  int baseTier, int machineTier,
                                                                  @Nullable Map<I, Integer> cache) {
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
     * @param entry         the entry to get the complete chance for
     * @param boostFunction the function boosting the entry's chance
     * @param baseTier      the base tier of the recipe
     * @param machineTier   the tier the recipe is run at
     * @return the total chance for the entry
     */
    static int getChance(@NotNull ChancedOutput<?> entry, @NotNull ChanceBoostFunction boostFunction, int baseTier,
                         int machineTier) {
        if (entry instanceof BoostableChanceEntry<?>boostableChanceEntry) {
            return boostFunction.getBoostedChance(boostableChanceEntry, baseTier, machineTier);
        }
        return entry.getChance();
    }

    /**
     * @param chance the boosted chance to be checked
     * @param entry  the entry to get the max chance and ingredient of for comparison and cache
     * @param cache  the cache of previously rolled chances, can be null
     * @return if the roll with the chance is successful
     */
    static <I, T extends ChancedOutput<I>> boolean passesChance(int chance, T entry, @Nullable Map<I, Integer> cache) {
        if (cache == null || !cache.containsKey(entry.getIngredient())) {
            int initial = GTValues.RNG.nextInt(entry.getMaxChance() + 1);
            updateCachedChance(entry.getIngredient(), cache, initial);
            return GTValues.RNG.nextInt(entry.getMaxChance()) <= entry.getChance();
        }

        int fullChance = getCachedChance(entry, cache) + chance;
        if (fullChance >= entry.getMaxChance()) {
            fullChance %= entry.getMaxChance();
            updateCachedChance(entry.getIngredient(), cache, fullChance);
            return true;
        }

        updateCachedChance(entry.getIngredient(), cache, fullChance);
        return false;
    }

    /**
     * @return the upper bound for rolling chances
     */
    static int getMaxChancedValue() {
        return 10_000;
    }

    /**
     * @param entry the current entry
     * @param cache the cache of previously rolled chances, can be null
     * @return the cached chance, otherwise 0 if
     *         the cache is null or does not contain the key
     */
    static <I, T extends ChancedOutput<I>> int getCachedChance(T entry, @Nullable Map<I, Integer> cache) {
        if (cache == null || !cache.containsKey(entry.getIngredient()))
            return 0;

        return cache.get(entry.getIngredient());
    }

    /**
     * @param ingredient the key used for the cache
     * @param cache      the cache of previously rolled chances, can be null
     * @param chance     the chance to update the cache with
     */
    static <I> void updateCachedChance(I ingredient, @Nullable Map<I, Integer> cache, int chance) {
        if (cache == null) return;
        cache.put(ingredient, chance);
    }

    /**
     * Roll the chance and attempt to produce the output
     *
     * @param chancedEntries the list of entries to roll
     * @param boostFunction  the function to boost the entries' chances
     * @param baseTier       the base tier of the recipe
     * @param machineTier    the tier the recipe is run at
     * @param cache          the cache of previously rolled chances, can be null
     * @return a list of the produced outputs, or null if failed
     */
    <I, T extends ChancedOutput<I>> @Nullable @Unmodifiable List<@NotNull T> roll(
                                                                                  @NotNull @Unmodifiable List<@NotNull T> chancedEntries,
                                                                                  @NotNull ChanceBoostFunction boostFunction,
                                                                                  int baseTier, int machineTier,
                                                                                  @Nullable Map<I, Integer> cache);

    /**
     * Roll the chance and attempt to produce the output
     *
     * @param chancedEntries the list of entries to roll
     * @param boostFunction  the function to boost the entries' chances
     * @param baseTier       the base tier of the recipe
     * @param machineTier    the tier the recipe is run at
     * @return a list of the produced outputs
     */
    default <I, T extends ChancedOutput<I>> @Nullable @Unmodifiable List<@NotNull T> roll(
                                                                                          @NotNull @Unmodifiable List<@NotNull T> chancedEntries,
                                                                                          @NotNull ChanceBoostFunction boostFunction,
                                                                                          int baseTier,
                                                                                          int machineTier) {
        return roll(chancedEntries, boostFunction, baseTier, machineTier, null);
    }

    @NotNull
    String getTranslationKey();
}
