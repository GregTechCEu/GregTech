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
        switch (dyeColor) {
            case WHITE:
                return "dyeWhite";
            case ORANGE:
                return "dyeOrange";
            case MAGENTA:
                return "dyeMagenta";
            case LIGHT_BLUE:
                return "dyeLightBlue";
            case YELLOW:
                return "dyeYellow";
            case LIME:
                return "dyeLime";
            case PINK:
                return "dyePink";
            case GRAY:
                return "dyeGray";
            case SILVER:
                return "dyeLightGray";
            case CYAN:
                return "dyeCyan";
            case PURPLE:
                return "dyePurple";
            case BLUE:
                return "dyeBlue";
            case BROWN:
                return "dyeBrown";
            case GREEN:
                return "dyeGreen";
            case RED:
                return "dyeRed";
            case BLACK:
                return "dyeBlack";
            default:
                throw new IllegalStateException("Unreachable");
        }
    }
}
