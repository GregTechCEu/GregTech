package gregtech.api.recipes.ingredients.match;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.List;

public interface MatchCalculation<T> {

    /**
     * Attempts to match at the given scaling. Results & success status should be saved to this object.
     * 
     * @param scale the scale to attempt
     * @return whether the match was successful at this scale.
     */
    boolean attemptScale(@Range(from = 1, to = Integer.MAX_VALUE) int scale);

    /**
     * Get the largest succeeding scale lower than or equal to maximum.
     * 
     * @param maximum the maximum scaling
     * @return the largest succeeding scale
     */
    int largestSucceedingScale(int maximum);

    /**
     * Match results are an indexed map of match quantity of matchables, pre rolling.
     * 
     * @return the match results for this scale, or null if could not match at this scale.
     */
    long @Nullable [] getMatchResultsForScale(int scale);

    /**
     * Consume results are an indexed map of consume quantity of matchables, post rolling.
     *
     * @return the consume results for this scale, or null if could not match at this scale.
     */
    long @Nullable [] getConsumeResultsForScaleAndBoost(int scale, int rollBoost);

    /**
     * Get the list of matched, before rolling, based on matchables and match results.
     * Should be consistent with the results of {@link #getMatchResultsForScale(int)}; caching is recommended.
     *
     * @return the list of consumed.
     */
    @NotNull
    List<T> getMatched(int scale);

    /**
     * Get the list of consumed, after rolling, based on matchables and match results.
     * Should be consistent with the results of {@link #getConsumeResultsForScaleAndBoost(int, int)}; caching is
     * recommended.
     * 
     * @return the list of consumed.
     */
    @NotNull
    List<T> getConsumed(int scale, int rollBoost);
}
