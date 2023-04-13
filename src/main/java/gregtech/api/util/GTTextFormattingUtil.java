package gregtech.api.util;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class GTTextFormattingUtil {

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    private static final TreeMap<Integer, String> romanNumeralConversions = new TreeMap<>();


    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static String formatLongToCompactString(long value, int precision) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return formatLongToCompactString(Long.MIN_VALUE + 1, precision);
        if (value < 0) return "-" + formatLongToCompactString(-value, precision);
        if (value < Math.pow(10, precision)) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10F);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public static String formatLongToCompactString(long value) {
        return formatLongToCompactString(value, 3);
    }

    public static String romanNumeralString(int num) {

        if (romanNumeralConversions.isEmpty()) { // Initialize on first run-through.
            romanNumeralConversions.put(1000, "M");
            romanNumeralConversions.put(900, "CM");
            romanNumeralConversions.put(500, "D");
            romanNumeralConversions.put(400, "CD");
            romanNumeralConversions.put(100, "C");
            romanNumeralConversions.put(90, "XC");
            romanNumeralConversions.put(50, "L");
            romanNumeralConversions.put(40, "XL");
            romanNumeralConversions.put(10, "X");
            romanNumeralConversions.put(9, "IX");
            romanNumeralConversions.put(5, "V");
            romanNumeralConversions.put(4, "IV");
            romanNumeralConversions.put(1, "I");
        }

        int conversion = romanNumeralConversions.floorKey(num);
        if (num == conversion) {
            return romanNumeralConversions.get(num);
        }
        return romanNumeralConversions.get(conversion) + romanNumeralString(num - conversion);
    }

    /**
     * Does almost the same thing as .to(LOWER_UNDERSCORE, string), but it also inserts underscores between words and numbers.
     *
     * @param string Any string with ASCII characters.
     * @return A string that is all lowercase, with underscores inserted before word/number boundaries: "maragingSteel300" -> "maraging_steel_300"
     */
    public static String toLowerCaseUnderscore(String string) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            if (i != 0 && (Character.isUpperCase(string.charAt(i)) || (
                    Character.isDigit(string.charAt(i - 1)) ^ Character.isDigit(string.charAt(i)))))
                result.append("_");
            result.append(Character.toLowerCase(string.charAt(i)));
        }
        return result.toString();
    }

    /**
     * Does almost the same thing as LOWER_UNDERSCORE.to(UPPER_CAMEL, string), but it also removes underscores before numbers.
     *
     * @param string Any string with ASCII characters.
     * @return A string that is all lowercase, with underscores inserted before word/number boundaries: "maraging_steel_300" -> "maragingSteel300"
     */
    public static String lowerUnderscoreToUpperCamel(String string) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == '_')
                continue;
            if (i == 0 || string.charAt(i - 1) == '_') {
                result.append(Character.toUpperCase(string.charAt(i)));
            } else {
                result.append(string.charAt(i));
            }
        }
        return result.toString();
    }

}
