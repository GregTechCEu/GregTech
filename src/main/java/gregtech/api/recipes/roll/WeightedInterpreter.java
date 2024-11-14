package gregtech.api.recipes.roll;

import gregtech.api.GTValues;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Arrays;

public final class WeightedInterpreter implements RollInterpreter {

    public static final WeightedInterpreter INSTANCE = new WeightedInterpreter(1, 10_000);
    private final @Range(from = 1, to = Integer.MAX_VALUE) int maximumRollAttempts;
    private final @Range(from = 1, to = 10_000) int chancePerRoll;

    public WeightedInterpreter(@Range(from = 1, to = Integer.MAX_VALUE) int maximumRollAttempts,
                               @Range(from = 1, to = 10_000) int chancePerRoll) {
        this.maximumRollAttempts = maximumRollAttempts;
        this.chancePerRoll = chancePerRoll;
    }

    @Override
    public long @NotNull [] interpretAndRoll(long @NotNull [] maxYield, long @NotNull [] rollValue,
                                             long @NotNull [] rollBoost, int boostStrength, int parallel) {
        long[] adj = new long[maxYield.length];
        if (maxYield.length == 0) return adj;
        long sum = 0;
        boolean[] skipped = new boolean[maxYield.length];
        for (int i = 0; i < maxYield.length; i++) {
            if (rollValue[i] == Long.MIN_VALUE) {
                skipped[i] = true;
                continue;
            }
            sum += (adj[i] = rollValue[i] + rollBoost[i] * boostStrength);
        }
        int attempts = Math.min(maxYield.length, maximumRollAttempts);
        long[] roll = new long[maxYield.length];
        for (int p = 0; p < parallel; p++) {
            boolean[] picked = Arrays.copyOf(skipped, skipped.length);
            mainloop:
            for (int i = 0; i < attempts; i++) {
                if (GTValues.RNG.nextInt(10_000) < chancePerRoll) {
                    long pick = GTValues.RNG.nextLong(sum);
                    int pointer = 0;
                    for (; pointer < maxYield.length; pointer++) {
                        while (picked[pointer]) {
                            pointer++;
                            if (pointer == adj.length) continue mainloop; // shouldn't happen
                        }
                        pick -= adj[pointer];
                        if (pick < 0) break;
                    }
                    pick = adj[pointer];
                    sum -= pick;
                    roll[pointer] += pick;
                    picked[pointer] = true;
                }
            }
        }
        return roll;
    }
}
