package gregtech.api.recipes.chance.boost;

import gregtech.api.recipes.chance.ChanceEntry;

/**
 * A chanced entry which can have its chance be boosted.
 *
 * @param <T> the type of ingredient contained by the chanced entry
 */
public interface BoostableChanceEntry<T> extends ChanceEntry<T> {

    /**
     * @return the chance boost amount
     */
    int getChanceBoost();
}
