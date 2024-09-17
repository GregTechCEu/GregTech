package gregtech.api.recipes.ingredients.match;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jgrapht.graph.DefaultWeightedEdge;

public abstract class AbstractMatchCalculation<T> implements MatchCalculation<T> {

    protected Int2ObjectAVLTreeMap<long[]> cache;
    protected int scaling = 1;

    /**
     * Attempts to match at the given scaling. Results & success status will be saved to this object.
     *
     * @param scale the scale to attempt
     * @return whether the match was successful at this scale.
     */
    @Override
    public boolean attemptScale(@Range(from = 1, to = Integer.MAX_VALUE) int scale) {
        if (scaling == 0) return false;
        if (cache.containsKey(scale)) {
            return cache.get(scale) != null;
        }
        if (scale != scaling) {
            rescale(scaling, scale);
            scaling = scale;
        }
        long[] attempt = attemptScaleInternal();
        if (scaling == 0) return false;
        if (scaling == 1 && attempt == null) {
            reportNoValidScales();
            return false;
        }
        cache.put(scaling, attempt);
        return attempt != null;
    }

    /**
     * Used to notify descendants of a rescale event, if they need to rescale.
     * @param oldScale the old scale, also equivalent to {@link #scaling}
     * @param newScale the new scale, {@link #scaling} will be subsequently set to this.
     */
    protected abstract void rescale(int oldScale, int newScale);

    /**
     * Used to get the match results for a given scaling. Caching and automatic invalidation is handled by
     * {@link AbstractMatchCalculation}. The desired scale can be found through the {@link #scaling} field.
     * @return the match results, or null if no match was found.
     */
    protected abstract long @Nullable [] attemptScaleInternal();

    @MustBeInvokedByOverriders
    protected void reportNoValidScales() {
        scaling = 0;
        cache = null;
    }

    @Override
    public int largestSucceedingScale(int maximum) {
        if (scaling == 0) return 0;
        int minValue = 1;
        maximum = getSearchCeiling(maximum);
        while (maximum - minValue > 1) {
            if (scaling == 0) return 0;
            int middle = (minValue + maximum) / 2;
            if (!attemptScale(middle)) {
                maximum = middle;
            } else {
                minValue = middle;
            }
        }
        if (maximum != minValue) {
            if (attemptScale(maximum)) minValue = maximum;
        }
        if (!attemptScale(minValue)) return 0;
        return minValue;
    }

    protected int getSearchCeiling(int maximum) {
        for (var entry : cache.int2ObjectEntrySet()) {
            if (entry.getIntKey() >= maximum) return maximum;
            if (entry.getValue() == null) return entry.getIntKey();
        }
        return maximum;
    }

    @Override
    public long @Nullable [] getMatchResultsForScale(int scale) {
        if (scaling == 0) return null;
        if (!cache.containsKey(scale)) attemptScale(scale);
        return cache.get(scale);
    }
}
