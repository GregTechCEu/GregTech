package gregtech.api.util;

import com.google.common.base.CaseFormat;
import net.minecraft.item.EnumDyeColor;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DyeUtil {

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
}
