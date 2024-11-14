package gregtech.api.recipes.roll;

import gregtech.api.GTValues;

import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleSupplier;

public final class RangedInterpreter implements RollInterpreter {

    public static final RangedInterpreter INSTANCE = new RangedInterpreter(GTValues.RNG::nextDouble);

    private final @NotNull DoubleSupplier distribution;

    public RangedInterpreter(@NotNull DoubleSupplier distribution) {
        this.distribution = distribution;
    }

    @Override
    public long @NotNull [] interpretAndRoll(long @NotNull [] maxYield, long @NotNull [] rollValue,
                                             long @NotNull [] rollBoost, int boostStrength, int parallel) {
        long[] roll = new long[maxYield.length];
        if (maxYield.length == 0) return roll;
        for (int i = 0; i < maxYield.length; i++) {
            if (rollValue[i] == Long.MIN_VALUE) continue;
            long minYield = rollValue[i] + rollBoost[i] * boostStrength;
            if (minYield >= maxYield[i]) roll[i] = maxYield[i];
            else {
                minYield = Math.max(minYield, 0);
                roll[i] = Math.round(parallel * (maxYield[i] - minYield) * distribution.getAsDouble()) +
                        minYield * parallel;
            }
        }
        return roll;
    }
}
