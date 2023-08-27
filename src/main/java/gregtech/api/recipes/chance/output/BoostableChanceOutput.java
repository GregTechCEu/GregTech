package gregtech.api.recipes.chance.output;

import gregtech.api.recipes.chance.boost.BoostableChanceEntry;
import org.jetbrains.annotations.NotNull;

/**
 * A chanced output which can be boosted
 *
 * @param <T> the type of ingredient contained by the output
 */
public abstract class BoostableChanceOutput<T> extends ChancedOutput<T> implements BoostableChanceEntry<T> {

    private final int chanceBoost;

    public BoostableChanceOutput(@NotNull T ingredient, int chance, int chanceBoost) {
        super(ingredient, chance);
        this.chanceBoost = chanceBoost;
    }

    @Override
    public int getChanceBoost() {
        return this.chanceBoost;
    }
}
