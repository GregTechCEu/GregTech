package gregtech.api.recipes.chance.boost;

import org.jetbrains.annotations.NotNull;

/**
 * A function used to boost a {@link BoostableChanceEntry}
 */
@FunctionalInterface
public interface ChanceBoostFunction {

    /**
     * @param entry the amount to boost by
     * @return the boosted chance
     */
    int getBoostedChance(@NotNull BoostableChanceEntry<?> entry);
}
