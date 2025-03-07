package gregtech.api.recipes.roll;

import gregtech.client.utils.TooltipHelper;

import net.minecraft.client.resources.I18n;

import org.jetbrains.annotations.NotNull;

abstract class InterpreterTextHelp {

    static @NotNull String formatBoost(double rollBoost) {
        if (rollBoost > 0) {
            return "+" + rollBoost;
        } else if (rollBoost < 0) {
            return "-" + -rollBoost;
        } else {
            return "0";
        }
    }

    static @NotNull String boostSign(long rollBoost) {
        if (rollBoost > 0) {
            return "+";
        } else if (rollBoost < 0) {
            return "-";
        } else {
            return "0";
        }
    }

    static @NotNull String notConsumedSmallDisplay() {
        return I18n.format("gregtech.recipe.nc");
    }

    static @NotNull String notConsumedTooltip() {
        return TooltipHelper.BLINKING_CYAN + I18n.format("gregtech.recipe.not_consumed");
    }
}
