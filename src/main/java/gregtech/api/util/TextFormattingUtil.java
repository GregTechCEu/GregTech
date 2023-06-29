package gregtech.api.util;

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
}
