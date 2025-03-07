package gregtech.api.recipes.ingredients.match;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;

public abstract class AbstractMatchCalculation<T> implements MatchCalculation<T> {

    protected Int2ObjectAVLTreeMap<long[]> cache = new Int2ObjectAVLTreeMap<>();
    protected Long2ObjectOpenHashMap<long[]> cacheRolled = new Long2ObjectOpenHashMap<>();
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
     * 
     * @param oldScale the old scale, also equivalent to {@link #scaling}
     * @param newScale the new scale, {@link #scaling} will be subsequently set to this.
     */
    protected abstract void rescale(int oldScale, int newScale);

    /**
     * Used to get the match results for a given scaling. Caching and automatic invalidation is handled by
     * {@link AbstractMatchCalculation}. The desired scale can be found through the {@link #scaling} field.
     * 
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

    @Override
    public long @Nullable [] getConsumeResultsForScaleAndBoost(int scale, int rollBoost) {
        if (scaling == 0) return null;
        if (!cache.containsKey(scale)) attemptScale(scale);
        long[] arr = cache.get(scale);
        if (arr == null) return null;
        long key = (((long) scale) << 32) | (rollBoost & 0xFFFFFFFFL);
        if (!cacheRolled.containsKey(key)) cacheRolled.put(key, convertToConsumeResults(arr, scale, rollBoost));
        return cacheRolled.get(scale);
    }

    protected abstract long @NotNull [] convertToConsumeResults(long @NotNull @Unmodifiable [] matchResults, int scale,
                                                                int rollBoost);

    protected abstract List<T> mapResults(long @NotNull [] results);

    @Override
    public @NotNull List<T> getMatched(int scale) {
        // fail if we could not match at this scale
        long[] results = getMatchResultsForScale(scale);
        if (results == null) return Collections.emptyList();
        else return mapResults(results);
    }

    @Override
    public @NotNull List<T> getConsumed(int scale, int rollBoost) {
        // fail if we could not match at this scale
        long[] results = getConsumeResultsForScaleAndBoost(scale, rollBoost);
        if (results == null) return Collections.emptyList();
        else return mapResults(results);
    }
}
