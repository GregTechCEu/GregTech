package gregtech.api.recipes.chance.output;

import gregtech.api.recipes.chance.BaseChanceEntry;

import org.jetbrains.annotations.NotNull;

/**
 * And output which has a chance to be produced
 *
 * @param <T> the type of ingredient contained by the output
 */
public abstract class ChancedOutput<T> extends BaseChanceEntry<T> {

    public ChancedOutput(@NotNull T ingredient, int chance) {
        super(ingredient, chance);
    }
}
