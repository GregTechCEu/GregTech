package gregtech.api.recipes.chance.boost;

import gregtech.api.GTValues;
import org.jetbrains.annotations.NotNull;

/**
 * Standard chance boost function based on the amount of overclocks performed.
 * <p>
 * Can be used with {@link #setContext(int, int)} and {@link #resetContext()}.
 * Use of these methods is <strong>not thread-safe</strong>.
 */
public final class OverclockedChanceBoostFunction implements ChanceBoostFunction {

    private int baseTier;
    private int overclockedTier;

    public OverclockedChanceBoostFunction(int baseTier, int overclockedTier) {
        this.baseTier = baseTier;
        this.overclockedTier = overclockedTier;
    }

    public void setContext(int baseTier, int overclockedTier) {
        this.baseTier = baseTier;
        this.overclockedTier = overclockedTier;
    }

    public void resetContext() {
        this.baseTier = 0;
        this.overclockedTier = 0;
    }

    @Override
    public int getBoostedChance(@NotNull BoostableChanceEntry<?> entry) {
        int tierDiff = overclockedTier - baseTier;
        if (tierDiff <= 0) return entry.getChance(); // equal or invalid tiers do not boost at all
        if (baseTier == GTValues.ULV) tierDiff--; // LV does not boost over ULV
        return entry.getChanceBoost() + (entry.getChanceBoost() * tierDiff);
    }
}
