package gregtech.api.util;

import org.jetbrains.annotations.Range;

public class ColorUtil {

    /**
     * Combine three R, G, and B values into an (A)RGB encoded integer. <br/>
     * Alpha channel will be {@code 0}
     */
    public static int combineRGBNoAlpha(@Range(from = 0, to = 255) int r, @Range(from = 0, to = 255) int g,
                                        @Range(from = 0, to = 255) int b) {
        return (r << 16) | (g << 8) | b;
    }

    /**
     * Combine three R, G, and B values into an (A)RGB encoded integer. <br/>
     * Alpha channel will be {@code 0xFF}/{@code 255}
     */
    public static int combineRGBFullAlpha(@Range(from = 0, to = 255) int r, @Range(from = 0, to = 255) int g,
                                          @Range(from = 0, to = 255) int b) {
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /**
     * Combine A, R, G, and B values into an ARGB encoded integer.
     */
    public static int combineARGB(@Range(from = 0, to = 255) int a, @Range(from = 0, to = 255) int r,
                                  @Range(from = 0, to = 255) int g, @Range(from = 0, to = 255) int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Pack two ARGB encoded integers into a single {@code long} with the left integer taking the 32 most significant
     * bits and the right the 32 least significant bits.
     */
    public static long packTwoARGB(int left, int right) {
        return ((long) left << 32) | right;
    }

    /**
     * Get the left ARGB encoded integer from a long produced from {@link #packTwoARGB(int, int)}.
     */
    public static int getLeftARGB(long doublePackedARGB) {
        return (int) (doublePackedARGB >>> 32);
    }

    /**
     * Get the right ARGB encoded integer from a long produced from {@link #packTwoARGB(int, int)}.
     */
    public static int getRightARGB(long doublePackedARGB) {
        return (int) doublePackedARGB;
    }

    /**
     * Get an array of the red, blue, and green channels as floats from 0 to 1. <br/>
     * Index {@code 0} = {@code Red} <br/>
     * Index {@code 1} = {@code Green} <br/>
     * Index {@code 2} = {@code Blue} <br/>
     * See {@link #getARGBFloats(int)} for getting the alpha channel.
     */
    public static float[] getRGBFloats(int rgb) {
        return new float[] {
                ARGBHelper.RED.isolateAndShiftAsFloat(rgb),
                ARGBHelper.GREEN.isolateAndShiftAsFloat(rgb),
                ARGBHelper.BLUE.isolateAndShiftAsFloat(rgb)
        };
    }

    /**
     * Get an array of the alpha, red, blue, and green channels as floats from 0 to 1. <br/>
     * Index {@code 0} = {@code Alpha} <br/>
     * Index {@code 1} = {@code Red} <br/>
     * Index {@code 2} = {@code Green} <br/>
     * Index {@code 3} = {@code Blue} <br/>
     * See {@link #getRGBFloats(int)} for ignoring the alpha channel.
     */
    public static float[] getARGBFloats(int argb) {
        return new float[] {
                ARGBHelper.ALPHA.isolateAndShiftAsFloat(argb),
                ARGBHelper.RED.isolateAndShiftAsFloat(argb),
                ARGBHelper.GREEN.isolateAndShiftAsFloat(argb),
                ARGBHelper.BLUE.isolateAndShiftAsFloat(argb)
        };
    }

    /**
     * A helper enum designed to help with changing or getting specific color channels from an ARGB encoded integer.
     */
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
        public final @Range(from = 0, to = 0xFF) int isolateAndShift(int value) {
            return (value >> shift) & 0xFF;
        }

        /**
         * Isolate this channel as a float from 0 to 1. <br/>
         * Example: {@code GREEN.isolateAndShift(0xDEADBEEF)} will return {@code 0xBE / 255} or {@code 0.74509805}.
         */
        public final float isolateAndShiftAsFloat(int value) {
            return ((value >> shift) & 0xFF) / 255.0F;
        }

        /**
         * Remove the other two colors from the integer encoded ARGB and set the alpha to 255. <br/>
         * Will always return {@code 0xFF000000} if called on {@link #ALPHA}. <br/>
         * Unlike {@link #isolateAndShift(int)}, this will not be between 0 and 255. <br/>
         * Example: {@code GREEN.isolateWithFullAlpha(0xDEADBEEF)} will return {@code 0xFF00BE00} or {@code -16728576}.
         */
        public final int isolateWithFullAlpha(int value) {
            return (value & overlay) | ALPHA.overlay;
        }

        /**
         * Set the value of this channel in an integer encoded ARGB value.
         */
        public final int replace(int originalARGB, @Range(from = 0, to = 0xFF) int value) {
            return (originalARGB & invertedOverlay) | (value << shift);
        }

        /**
         * The same as {@link #replace(int, int)} but will behave as if {@code originalARGB} was {@code 0}.
         */
        public final int get(@Range(from = 0, to = 0xFF) int value) {
            return value << shift;
        }

        /**
         * Add a value to this channel's value. Can overflow in this channel, but will not affect the other channels.
         */
        public final int add(int originalARGB, @Range(from = 0, to = 0xFF) int value) {
            return replace(originalARGB, (isolateAndShift(originalARGB) + value) & 0xFF);
        }

        /**
         * Subtract a value from this channel's value. Can underflow in this channel, but will not affect the other
         * channels.
         */
        public final int subtract(int originalARGB, @Range(from = 0, to = 0xFF) int value) {
            return replace(originalARGB, (isolateAndShift(originalARGB) - value) & 0xFF);
        }
    }
}
