package gregtech.api.recipes.ingredients.match;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Collections;
import java.util.List;

public class EmptyMatchCalculation<T> implements MatchCalculation<T> {

    private static final EmptyMatchCalculation<Object> INSTANCE = new EmptyMatchCalculation<>();
    private static final long[] empty = new long[0];

    public static <T> EmptyMatchCalculation<T> get() {
        return (EmptyMatchCalculation<T>) INSTANCE;
    }

    private EmptyMatchCalculation() {}

    @Override
    public boolean attemptScale(@Range(from = 1, to = Integer.MAX_VALUE) int scale) {
        return true;
    }

    @Override
    public int largestSucceedingScale(int maximum) {
        return maximum;
    }

    @Override
    public long @Nullable [] getMatchResultsForScale(int scale) {
        return empty;
    }

    @Override
    public long @Nullable [] getConsumeResultsForScaleAndBoost(int scale, int rollBoost) {
        return empty;
    }

    @Override
    public @NotNull List<T> getMatched(int scale) {
        return Collections.emptyList();
    }

    @Override
    public @NotNull List<T> getConsumed(int scale, int rollBoost) {
        return Collections.emptyList();
    }
}
