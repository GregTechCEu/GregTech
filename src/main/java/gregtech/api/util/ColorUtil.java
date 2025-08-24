package gregtech.api.util;

import net.minecraft.item.EnumDyeColor;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public class ColorUtil {

    public static int combineRGB(@Range(from = 0, to = 255) int r, @Range(from = 0, to = 255) int g,
                                 @Range(from = 0, to = 255) int b) {
        return (r << 16) | (g << 8) | b;
    }

    public static int combineARGB(@Range(from = 0, to = 255) int a, @Range(from = 0, to = 255) int r,
                                  @Range(from = 0, to = 255) int g, @Range(from = 0, to = 255) int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static @Nullable EnumDyeColor getDyeColorFromRGB(int color) {
        if (color == -1) return null;

        for (EnumDyeColor dyeColor : EnumDyeColor.values()) {
            if (color == dyeColor.colorValue) {
                return dyeColor;
            }
        }

        return null;
    }

    public enum ARGBHelper {

        ALPHA(0xFF000000, 24),
        RED(0xFF0000, 16),
        GREEN(0xFF00, 8),
        BLUE(0xFF, 0);

        public final int overlay;
        public final int invertedOverlay;
        public final int shift;

        ARGBHelper(int overlay, int shift) {
            this.overlay = overlay;
            this.invertedOverlay = ~overlay;
            this.shift = shift;
        }

        public @Range(from = 0, to = 0xFF) int getFromInt(int sourceARGB) {
            return (sourceARGB >> shift) & 0xFF;
        }

        public int setInInt(int originalARGB, @Range(from = 0, to = 0xFF) int replacementColor) {
            return (originalARGB & invertedOverlay) | (replacementColor << shift);
        }

        public int setInInt(@Range(from = 0, to = 0xFF) int color) {
            return color << shift;
        }

        public int addInInt(int originalARGB, @Range(from = 0, to = 0xFF) int addingColor) {
            return setInInt(originalARGB, (getFromInt(originalARGB) + addingColor) & 0xFF);
        }

        public int subtractFromInt(int originalARGB, @Range(from = 0, to = 0xFF) int subtractingColor) {
            return setInInt(originalARGB, (getFromInt(originalARGB) - subtractingColor) & 0xFF);
        }
    }
}
