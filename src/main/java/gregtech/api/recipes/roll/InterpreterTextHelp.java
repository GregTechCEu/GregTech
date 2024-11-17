package gregtech.api.recipes.roll;

import gregtech.client.utils.TooltipHelper;

import net.minecraft.client.resources.I18n;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class InterpreterTextHelp {

    static @Nullable String formatBoost(double rollBoost) {
        if (rollBoost > 0) {
            return "+" + rollBoost;
        } else if (rollBoost < 0) {
            return "-" + -rollBoost;
        } else {
            return null;
        }
    }

    static @NotNull String notConsumedSmallDisplay() {
        return I18n.format("gregtech.recipe.nc");
    }

    static @NotNull String notConsumedTooltip() {
        return TooltipHelper.BLINKING_CYAN + I18n.format("gregtech.recipe.not_consumed");
    }
}
