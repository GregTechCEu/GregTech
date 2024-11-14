package gregtech.api.recipes.roll;

import gregtech.api.GTValues;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.function.DoubleSupplier;

/**
 * Interprets what roll data means, and performs rolling.
 */
@SuppressWarnings("Convert2Lambda")
public interface RollInterpreter {

    /**
     * Interprets roll data as chance, ranging from 0 to 10_000, of getting max yield.
     * Each roll is independently calculated of the others.
     */
    static @NotNull RollInterpreter chanceIndependent() {
        return DEFAULT;
    }

    /**
     * Interprets roll data as chance, ranging from 0 to 10_000, of getting max yield.
     * The first roll is calculated and the result is applied to all other rolls, regardless of their roll data.
     */
    static @NotNull RollInterpreter chanceOverride() {
        return OVERRIDE;
    }

    /**
     * Interprets roll data as weights, determining their relative likelihood of being picked.
     * Every roll attempt, if it succeeds based on chance per roll, a random roll will be selected and yielded based on
     * weights, and then removed from consideration for future selections.
     * Maximum roll attempts will be bounded to be less than or equal to the number of rolls passed in before any rolls
     * are performed.
     */
    static @NotNull RollInterpreter chanceWeighted(@Range(from = 1, to = Integer.MAX_VALUE) int maximumRollAttempts,
                                                   @Range(from = 1, to = 10_000) int chancePerRoll) {
        if (maximumRollAttempts == 1 && chancePerRoll == 10_000) return WeightedInterpreter.INSTANCE;
        else return new WeightedInterpreter(maximumRollAttempts, chancePerRoll);
    }

    /**
     * Interprets roll data as min yield.
     * The boosted min yield is found and clamped between 0 and max yield, then a value is uniformly selected between
     * min and max yield.
     */
    static @NotNull RollInterpreter ranged() {
        return RangedInterpreter.INSTANCE;
    }

    /**
     * Interprets roll data as min yield.
     * The boosted min yield is found and clamped between 0 and max yield, then a value is selected by using the
     * provided distribution.
     * 
     * @param distribution must be a double supplier from 0 (inclusive) to 1 (exclusive), preferably from a random
     *                     source. See {@link GTValues#RNG}.
     */
    static @NotNull RollInterpreter ranged(@NotNull DoubleSupplier distribution) {
        return new RangedInterpreter(distribution);
    }

    RollInterpreter DEFAULT = new RollInterpreter() {

        @Override
        public long @NotNull [] interpretAndRoll(long @NotNull [] maxYield, long @NotNull [] rollValue,
                                                 long @NotNull [] rollBoost, int boostStrength, int parallel) {
            long[] roll = new long[maxYield.length];
            for (int i = 0; i < maxYield.length; i++) {
                if (rollValue[i] == Long.MIN_VALUE) continue;
                long chance = rollValue[i] + rollBoost[i] * boostStrength;
                for (int j = 0; j < parallel; j++) {
                    roll[i] += GTValues.RNG.nextInt(10_000) < chance ? maxYield[i] : 0;
                }
            }
            return roll;
        }
    };

    RollInterpreter OVERRIDE = new RollInterpreter() {

        @Override
        public long @NotNull [] interpretAndRoll(long @NotNull [] maxYield, long @NotNull [] rollValue,
                                                 long @NotNull [] rollBoost, int boostStrength, int parallel) {
            long[] roll = new long[maxYield.length];
            if (maxYield.length == 0) return roll;
            long chance = rollValue[0] + rollBoost[0] * boostStrength;
            for (int p = 0; p < parallel; p++) {
                if (GTValues.RNG.nextInt(10_000) < chance) {
                    for (int i = 0; i < maxYield.length; i++) {
                        if (rollValue[i] == Long.MIN_VALUE) continue;
                        roll[i] += maxYield[i];
                    }
                }
            }
            return roll;
        }
    };

    /**
     * Should interpret the roll data arrays and return the results of rolling. All arrays should be the same size.
     * 
     * @param maxYield      the maximum yield array. Values in the return array should not exceed their respective value
     *                      in the maxYield array times parallel.
     * @param rollValue     the roll value array. Values of {@link Long#MIN_VALUE} should be interpreted as not
     *                      consumable, and thus should always return 0 after rolling.
     * @param rollBoost     the roll boost array. Should be multiplied or otherwise scale by boost strength.
     * @param boostStrength the boost strength. Should affect roll boost.
     * @param parallel      the parallel of the roll.
     * @return the rolled values. Values in the return array should not exceed their respective value in the maxYield
     *         array multiplied by parallel.
     */
    long @NotNull [] interpretAndRoll(long @NotNull [] maxYield, long @NotNull [] rollValue, long @NotNull [] rollBoost,
                                      int boostStrength, int parallel);
}
