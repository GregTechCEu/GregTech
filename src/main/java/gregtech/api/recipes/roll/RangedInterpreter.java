package gregtech.api.recipes.roll;

import gregtech.api.GTValues;
import gregtech.client.utils.TooltipHelper;

import net.minecraft.client.resources.I18n;

import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleSupplier;

import static gregtech.api.recipes.roll.InterpreterTextHelp.formatBoost;

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

    @Override
    public @NotNull String interpretSmallDisplay(int index, RollInterpreterApplication application, long maxYield,
                                                 long rollValue,
                                                 long rollBoost) {
        if (rollValue == Long.MIN_VALUE) return InterpreterTextHelp.notConsumedSmallDisplay();
        String s = (rollValue) + "%";
        if (rollBoost != 0) s += InterpreterTextHelp.boostSign(rollBoost);
        return s;
    }

    @Override
    public @NotNull String interpretTooltip(int index, RollInterpreterApplication application, long maxYield,
                                            long rollValue,
                                            long rollBoost) {
        if (rollValue == Long.MIN_VALUE) return InterpreterTextHelp.notConsumedTooltip();
        return TooltipHelper.BLINKING_CYAN +
                I18n.format("gregtech.recipe.range", maxYield, rollValue, formatBoost(rollBoost));
    }

    @Override
    public @NotNull String addJEILine(RollInterpreterApplication application, int count) {
        return I18n.format("gregtech.recipe.chance.range", application.getTranslated(),
                application.flowDirectionTranslated());
    }

    @Override
    public @NotNull String addJEITooltip(RollInterpreterApplication application, int count) {
        return I18n.format("gregtech.recipe.chance.range.tooltip");
    }
}
