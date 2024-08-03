package gregtech.api.recipes.chance;

import gregtech.api.recipes.chance.output.ChancedOutputLogic;

import org.jetbrains.annotations.NotNull;

/**
 * Basic implementation for a chance entry.
 *
 * @param <T> the type of ingredient contained by the chanced entry
 */
public abstract class BaseChanceEntry<T> implements ChanceEntry<T> {

    private final T ingredient;
    private final int chance;
    private final int maxChance;

    public BaseChanceEntry(@NotNull T ingredient, int chance) {
        this.ingredient = ingredient;
        this.chance = chance;
        this.maxChance = ChancedOutputLogic.getMaxChancedValue();
    }

    public BaseChanceEntry(@NotNull T ingredient, int chance, int maxChance) {
        this.ingredient = ingredient;
        this.chance = chance;
        this.maxChance = maxChance;
    }

    @Override
    public @NotNull T getIngredient() {
        return ingredient;
    }

    @Override
    public int getChance() {
        return chance;
    }

    @Override
    public int getMaxChance() {
        return maxChance;
    }
}
