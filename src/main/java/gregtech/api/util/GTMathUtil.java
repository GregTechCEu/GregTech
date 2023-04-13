package gregtech.api.util;

import javax.annotation.Nonnull;

public class GTMathUtil {

    /**
     * @param values to find the mean of
     * @return the mean value
     */
    public static long mean(@Nonnull long[] values) {
        if (values.length == 0L)
            return 0L;

        long sum = 0L;
        for (long v : values)
            sum += v;
        return sum / values.length;
    }

    public static boolean isBetweenInclusive(long start, long end, long value) {
        return start <= value && value <= end;
    }

    /**
     * Checks if an (X,Y) point is within a defined box range
     *
     * @param initialX The initial X point of the box
     * @param initialY The initial Y point of the box
     * @param width    The width of the box
     * @param height   The height of the box
     * @param pointX   The X value of the point to check
     * @param pointY   The Y value of the point to check
     * @return True if the provided (X,Y) point is within the described box, else false
     */
    public static boolean isPointWithinRange(int initialX, int initialY, int width, int height, int pointX, int pointY) {
        return initialX <= pointX && pointX <= initialX + width && initialY <= pointY && pointY <= initialY + height;
    }

    /**
     * Tries to parse a string into an int, returning a default value if it fails.
     *
     * @param val          string to parse
     * @param defaultValue default value to return
     * @return returns an int from the parsed string, otherwise the default value
     */
    public static int tryParseInt(String val, int defaultValue) {
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            GTLog.logger.warn(e);
        }
        return defaultValue;
    }

    /**
     * Tries to parse a string into a long, returning a default value if it fails.
     *
     * @param val          string to parse
     * @param defaultValue default value to return
     * @return returns a long from the parsed string, otherwise the default value
     */
    public static long tryParseLong(String val, long defaultValue) {
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            GTLog.logger.warn(e);
        }
        return defaultValue;
    }

}
