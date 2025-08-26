package gregtech.api.util;

import net.minecraft.item.EnumDyeColor;

import org.jetbrains.annotations.NotNull;

public class DyeUtil {

    /**
     * Determines dye color nearest to specified RGB color
     */
    @NotNull
    public static EnumDyeColor determineDyeColor(int rgbColor) {
        int r1 = rgbColor >> 16 & 0xFF;
        int g1 = rgbColor >> 8 & 0xFF;
        int b1 = rgbColor & 0xFF;

        int minDistance = Integer.MAX_VALUE;
        EnumDyeColor dye = EnumDyeColor.WHITE;

        for (EnumDyeColor dyeColor : EnumDyeColor.values()) {
            int rd = r1 - (dyeColor.colorValue >> 16 & 0xFF);
            int gd = g1 - (dyeColor.colorValue >> 8 & 0xFF);
            int bd = b1 - (dyeColor.colorValue & 0xFF);

            int distance = rd * rd + gd * gd + bd * bd;
            if (distance < minDistance) {
                minDistance = distance;
                dye = dyeColor;
            }
        }

        return dye;
    }

    @NotNull
    public static String getOredictColorName(@NotNull EnumDyeColor dyeColor) {
        return switch (dyeColor) {
            case WHITE -> "dyeWhite";
            case ORANGE -> "dyeOrange";
            case MAGENTA -> "dyeMagenta";
            case LIGHT_BLUE -> "dyeLightBlue";
            case YELLOW -> "dyeYellow";
            case LIME -> "dyeLime";
            case PINK -> "dyePink";
            case GRAY -> "dyeGray";
            case SILVER -> "dyeLightGray";
            case CYAN -> "dyeCyan";
            case PURPLE -> "dyePurple";
            case BLUE -> "dyeBlue";
            case BROWN -> "dyeBrown";
            case GREEN -> "dyeGreen";
            case RED -> "dyeRed";
            case BLACK -> "dyeBlack";
            // noinspection UnnecessaryDefault
            default -> throw new IllegalStateException("Unreachable");
        };
    }
}
