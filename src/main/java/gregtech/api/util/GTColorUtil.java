package gregtech.api.util;

import com.google.common.base.CaseFormat;
import net.minecraft.block.material.MapColor;
import net.minecraft.item.EnumDyeColor;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Deals with both MC Dye Colors and other color conversions
 */
public class GTColorUtil {

    /**
     * Determines dye color nearest to specified RGB color
     */
    public static EnumDyeColor determineDyeColor(int rgbColor) {
        Color c = new Color(rgbColor);

        Map<Double, EnumDyeColor> distances = new HashMap<>();
        for (EnumDyeColor dyeColor : EnumDyeColor.values()) {
            Color c2 = new Color(dyeColor.colorValue);

            double distance = (c.getRed() - c2.getRed()) * (c.getRed() - c2.getRed())
                    + (c.getGreen() - c2.getGreen()) * (c.getGreen() - c2.getGreen())
                    + (c.getBlue() - c2.getBlue()) * (c.getBlue() - c2.getBlue());

            distances.put(distance, dyeColor);
        }

        double min = Collections.min(distances.keySet());
        return distances.get(min);
    }

    public static String getColorName(EnumDyeColor dyeColor) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, dyeColor.getName());
    }

    public static String getOredictColorName(EnumDyeColor dyeColor) {
        String colorName;

        if (dyeColor == EnumDyeColor.SILVER)
            colorName = "LightGray";
        else
            colorName = getColorName(dyeColor);

        return "dye" + colorName;
    }

    //just because CCL uses a different color format
    //0xRRGGBBAA
    public static int convertRGBtoOpaqueRGBA_CL(int colorValue) {
        return convertRGBtoRGBA_CL(colorValue, 255);
    }

    public static int convertRGBtoRGBA_CL(int colorValue, int opacity) {
        return colorValue << 8 | (opacity & 0xFF);
    }

    public static int convertOpaqueRGBA_CLtoRGB(int colorAlpha) {
        return colorAlpha >>> 8;
    }

    //0xAARRGGBB
    public static int convertRGBtoOpaqueRGBA_MC(int colorValue) {
        return convertRGBtoOpaqueRGBA_MC(colorValue, 255);
    }

    public static int convertRGBtoOpaqueRGBA_MC(int colorValue, int opacity) {
        return opacity << 24 | colorValue;
    }

    public static int convertOpaqueRGBA_MCtoRGB(int alphaColor) {
        return alphaColor & 0xFFFFFF;
    }

    public static MapColor getMapColor(int rgb) {
        MapColor color = MapColor.BLACK;
        int originalR = (rgb >> 16) & 0xFF;
        int originalG = (rgb >> 8) & 0xFF;
        int originalB = rgb & 0xFF;
        int distance = Integer.MAX_VALUE;

        for (MapColor mapColor : MapColor.COLORS) {
            // why is there a null in here mojang!?
            if (mapColor == null) continue;

            int colorValue = mapColor.colorValue;
            if (colorValue == 0) continue;

            int colorR = (colorValue >> 16) & 0xFF;
            int colorG = (colorValue >> 8) & 0xFF;
            int colorB = colorValue & 0xFF;

            int distR = Math.abs(originalR - colorR);
            int distG = Math.abs(originalG - colorG);
            int distB = Math.abs(originalB - colorB);
            int dist = distR * distR + distG * distG + distB * distB;

            if (dist < distance) {
                distance = dist;
                color = mapColor;
            }
        }
        return color;
    }
}
