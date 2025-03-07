package gregtech.api.recipes.roll;

import gregtech.api.GTValues;
import gregtech.client.utils.TooltipHelper;

import net.minecraft.client.resources.I18n;

import org.jetbrains.annotations.NotNull;

public final class IndependentInterpreter implements RollInterpreter {

    public static final IndependentInterpreter INSTANCE = new IndependentInterpreter();

    private IndependentInterpreter() {}

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

    @Override
    public @NotNull String interpretSmallDisplay(int index, RollInterpreterApplication application, long maxYield,
                                                 long rollValue,
                                                 long rollBoost) {
        if (rollValue == Long.MIN_VALUE) return InterpreterTextHelp.notConsumedSmallDisplay();
        String s = (rollValue / 100d) + "%";
        if (rollBoost != 0) s += InterpreterTextHelp.boostSign(rollBoost);
        return s;
    }

    @Override
    public @NotNull String interpretTooltip(int index, RollInterpreterApplication application, long maxYield,
                                            long rollValue,
                                            long rollBoost) {
        if (rollValue == Long.MIN_VALUE) return InterpreterTextHelp.notConsumedTooltip();
        return TooltipHelper.BLINKING_CYAN + I18n.format("gregtech.recipe.chance", rollValue / 100d,
                InterpreterTextHelp.formatBoost(rollBoost / 100d));
    }
}
