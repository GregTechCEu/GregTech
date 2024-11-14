package gregtech.api.recipes.ingredients.match;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface MatchRollController<T extends Matcher<?>> extends List<T> {

    /**
     *
     * @return an array of longs, where position in array corresponds to position in this list,
     * and value corresponds to rolled consumption.
     */
    long @NotNull [] getConsumptionRollResults(int scale);

    MatchRollController<T> copy();
}
