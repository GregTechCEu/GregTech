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

        /**
         * Isolate this channel as an integer from 0 to 255. <br/>
         * Example: {@code GREEN.isolateAndShift(0xDEADBEEF)} will return {@code 0xBE} or {@code 190}.
         */
        public @Range(from = 0, to = 0xFF) int isolateAndShift(int value) {
            return (value >> shift) & 0xFF;
        }

        /**
         * Remove the other two colors from the integer encoded ARGB and set the alpha to 255. <br/>
         * Will always return {@code 0xFF000000} if called on {@link #ALPHA}. <br/>
         * Unlike {@link #isolateAndShift(int)}, this will not be between 0 and 255. <br/>
         * Example: {@code GREEN.isolateWithFullAlpha(0xDEADBEEF)} will return {@code 0xFF00BE00} or {@code 4278238720}.
         */
        public int isolateWithFullAlpha(int value) {
            return (value & overlay) | 0xFF000000;
        }

        /**
         * Set the value of this channel in an integer encoded ARGB value.
         */
        public int replace(int originalARGB, @Range(from = 0, to = 0xFF) int value) {
            return (originalARGB & invertedOverlay) | (value << shift);
        }

        /**
         * The same as {@link #replace(int, int)} but will just return the value shifted to this channel.
         */
        public int replace(@Range(from = 0, to = 0xFF) int value) {
            return value << shift;
        }

        /**
         * Add a value to this channel's value. Can overflow in this channel, but will not affect the other channels.
         */
        public int add(int originalARGB, @Range(from = 0, to = 0xFF) int value) {
            return replace(originalARGB, (isolateAndShift(originalARGB) + value) & 0xFF);
        }

        /**
         * Subtract a value from this channel's value. Can underflow in this channel, but will not affect the other
         * channels.
         */
        public int subtract(int originalARGB, @Range(from = 0, to = 0xFF) int value) {
            return replace(originalARGB, (isolateAndShift(originalARGB) - value) & 0xFF);
        }
    }
}
