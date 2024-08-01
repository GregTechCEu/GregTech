package gregtech.api.recipes.chance.boost;

import gregtech.api.GTValues;

import org.jetbrains.annotations.NotNull;

/**
 * A function used to boost a {@link BoostableChanceEntry}
 */
@FunctionalInterface
public interface ChanceBoostFunction {

    /**
     * Chance boosting function based on the number of performed overclocks
     */
    ChanceBoostFunction OVERCLOCK = (entry, recipeTier, machineTier) -> {
        int tierDiff = machineTier - recipeTier;
        if (tierDiff <= 0) return entry.getChance(); // equal or invalid tiers do not boost at all
        if (recipeTier == GTValues.ULV) tierDiff--; // LV does not boost over ULV
        return entry.getChance() + (entry.getChanceBoost() * tierDiff);
    };

    /**
     * Chance boosting function which performs no boosting
     */
    ChanceBoostFunction NONE = (entry, recipeTier, machineTier) -> entry.getChance();

    /**
     * @param entry       the amount to boost by
     * @param recipeTier  the base tier of the recipe
     * @param machineTier the tier the recipe is run at
     * @return the boosted chance
     */
    int getBoostedChance(@NotNull BoostableChanceEntry<?> entry, int recipeTier, int machineTier);
}
