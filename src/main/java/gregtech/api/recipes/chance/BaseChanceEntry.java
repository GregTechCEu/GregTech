package gregtech.api.recipes.chance;

import org.jetbrains.annotations.NotNull;

/**
 * Basic implementation for a chance entry.
 *
 * @param <T> the type of ingredient contained by the chanced entry
 */
public abstract class BaseChanceEntry<T> implements ChanceEntry<T> {

    private final T ingredient;
    private final int chance;

    public BaseChanceEntry(@NotNull T ingredient, int chance) {
        this.ingredient = ingredient;
        this.chance = chance;
    }

    @Override
    public @NotNull T getIngredient() {
        return ingredient;
    }

    @Override
    public int getChance() {
        return chance;
    }
}
