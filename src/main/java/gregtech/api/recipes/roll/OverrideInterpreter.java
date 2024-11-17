package gregtech.api.recipes.roll;

import gregtech.api.GTValues;
import gregtech.client.utils.TooltipHelper;

import net.minecraft.client.resources.I18n;

import org.jetbrains.annotations.NotNull;

import static gregtech.api.recipes.roll.InterpreterTextHelp.formatBoost;

public final class OverrideInterpreter implements RollInterpreter {

    private final int chance;
    private final int chanceBoost;

    public OverrideInterpreter(int chance, int chanceBoost) {
        this.chance = chance;
        this.chanceBoost = chanceBoost;
    }

    @Override
    public long @NotNull [] interpretAndRoll(long @NotNull [] maxYield, long @NotNull [] rollValue,
                                             long @NotNull [] rollBoost, int boostStrength, int parallel) {
        long[] roll = new long[maxYield.length];
        if (maxYield.length == 0) return roll;
        int chance = this.chance + this.chanceBoost * boostStrength;
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

    @Override
    public @NotNull String interpretSmallDisplay(int index, RollInterpreterApplication application, long maxYield,
                                                 long rollValue,
                                                 long rollBoost) {
        if (rollValue == Long.MIN_VALUE) return InterpreterTextHelp.notConsumedSmallDisplay();
        String s = (chance / 100d) + "%";
        String b = formatBoost(rollBoost / 100d);
        if (b != null) s += " " + b + "%/t";
        return s;
    }

    @Override
    public @NotNull String interpretTooltip(int index, RollInterpreterApplication application, long maxYield,
                                            long rollValue,
                                            long rollBoost) {
        if (rollValue == Long.MIN_VALUE) return InterpreterTextHelp.notConsumedTooltip();
        return TooltipHelper.BLINKING_CYAN + addJEITooltip(application, 0);
    }

    @Override
    public @NotNull String addJEILine(RollInterpreterApplication application, int count) {
        return I18n.format("gregtech.recipe.chance.override", application.getTranslated(),
                application.flowDirectionTranslated());
    }

    @Override
    public @NotNull String addJEITooltip(RollInterpreterApplication application, int count) {
        return I18n.format("gregtech.recipe.chance", chance / 100d, formatBoost(chanceBoost / 100d));
    }
}
