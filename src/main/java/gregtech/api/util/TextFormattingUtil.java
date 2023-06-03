package gregtech.api.util;

import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class TextFormattingUtil {

    private static final long[] metricSuffixValues = {
            1_000L,
            1_000_000L,
            1_000_000_000L,
            1_000_000_000_000L,
            1_000_000_000_000_000L,
            1_000_000_000_000_000_000L
    };

    private static final char[] metricSuffixChars = {
            'k', 'M', 'G', 'T', 'P', 'E'
    };

    public static String formatLongToCompactString(long value, int precision) {
        if (value == 0 || Math.abs(value) < Math.pow(10, precision)) {
            return Long.toString(value); // deal with easy case
        }

        StringBuilder stb = new StringBuilder();
        if (value < 0) {
            stb.append('-');
            //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
            value = value == Long.MIN_VALUE ? Long.MAX_VALUE : -value;
        }

        int i = GTUtility.nearestLesserOrEqual(metricSuffixValues, value);
        if (i == -1) return stb.append(value).toString();

        long suffixValue = metricSuffixValues[i];
        stb.append(value / suffixValue);

        long truncatedDigit = value % suffixValue / (suffixValue / 10);
        if (truncatedDigit > 0) {
            stb.append('.').append(truncatedDigit);
        }
        return stb.append(metricSuffixChars[i]).toString();
    }

    public static String formatLongToCompactString(long value) {
        return formatLongToCompactString(value, 3);
    }

    public static String format(String format, Object toFormat, TextFormatting color) {
        return new TextComponentString(String.format(format, toFormat)).setStyle(new Style().setColor(color)).getFormattedText();
    }

    public static String format(String format, Object toFormat) {
        return new TextComponentString(String.format(format, toFormat)).getFormattedText();
    }

    public static String formatIntPretty(int number) {
        return formatLongPretty(number);
    }

    public static String formatIntPretty(int number, TextFormatting color) {
        return formatLongPretty(number, color);
    }

    public static String colorInt(int number, TextFormatting color) {
        return formatLongPretty(number, color);
    }

    public static String formatLongPretty(long number) {
        return new TextComponentString(String.format("%,d", number)).getFormattedText();
    }

    public static String formatLongPretty(long number, TextFormatting color) {
        return new TextComponentString(String.format("%,d", number)).setStyle(new Style().setColor(color)).getFormattedText();
    }

    public static String colorLong(long number, TextFormatting color) {
        return new TextComponentString(String.format("%d", number)).setStyle(new Style().setColor(color)).getFormattedText();
    }
}
