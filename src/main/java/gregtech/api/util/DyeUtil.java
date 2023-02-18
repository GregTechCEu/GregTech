package gregtech.api.util;

import com.google.common.base.CaseFormat;
import net.minecraft.item.EnumDyeColor;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class DyeUtil {

    public static final EnumDyeColor[] VALUES = EnumDyeColor.values();
    public static final int[] DYE_COLOR_VALUES = new int[VALUES.length];

    static {
        for (int i = 0; i < VALUES.length; i++) {
            EnumDyeColor color = VALUES[i];
            int colorValue;
            try {
                colorValue = ObfuscationReflectionHelper.getPrivateValue(EnumDyeColor.class, color, "field_193351_w");
            } catch (Exception e) {
                GTLog.logger.error("Could not find EnumDyeColor colorValue", e);
                colorValue = 0xFFFFFF;
            }
            DYE_COLOR_VALUES[i] = colorValue;
        }
    }

    private DyeUtil() {/**/}

    /**
     * Determines dye color nearest to specified RGB color
     */
    public static EnumDyeColor determineDyeColor(int rgbColor) {
        Color c = new Color(rgbColor);

        Map<Double, EnumDyeColor> distances = new HashMap<>();
        for (int i = 0; i < VALUES.length; i++) {
            Color c2 = new Color(DYE_COLOR_VALUES[i]);

            double distance = (c.getRed() - c2.getRed()) * (c.getRed() - c2.getRed())
                    + (c.getGreen() - c2.getGreen()) * (c.getGreen() - c2.getGreen())
                    + (c.getBlue() - c2.getBlue()) * (c.getBlue() - c2.getBlue());

            distances.put(distance, VALUES[i]);
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
