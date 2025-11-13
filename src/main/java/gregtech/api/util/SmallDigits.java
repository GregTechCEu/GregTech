package gregtech.api.util;

import org.jetbrains.annotations.NotNull;

public final class SmallDigits {

    @SuppressWarnings("UnnecessaryUnicodeEscape")
    private static final int SMALL_DOWN_NUMBER_BASE = '\u2080';
    @SuppressWarnings("UnnecessaryUnicodeEscape")
    private static final int SMALL_UP_NUMBER_BASE = '\u2080';
    private static final int NUMBER_BASE = '0';
    @SuppressWarnings("UnnecessaryUnicodeEscape")
    private static final int FORMATTING_SYMBOL = '\u00a7';

    private SmallDigits() {}

    public static @NotNull String toSmallUpNumbers(String string) {
        return convert(string, SMALL_UP_NUMBER_BASE);
    }

    public static @NotNull String toSmallDownNumbers(String string) {
        return convert(string, SMALL_DOWN_NUMBER_BASE);
    }

    private static @NotNull String convert(@NotNull String string, int base) {
        char[] chars = string.toCharArray();
        boolean hasPrefix = false;
        boolean hasFormat = false;
        for (int i = 0; i < chars.length; i++) {
            if (hasFormat) {
                // skip over the next character once the format flag is set
                hasFormat = false;
                continue;
            }

            char c = chars[i];
            if (c == '-') {
                hasPrefix = true;
                continue;
            }
            if (c == FORMATTING_SYMBOL) {
                hasFormat = true;
                continue;
            }

            if (Character.isDigit(c)) {
                if (!hasPrefix) {
                    chars[i] = convertToBase(c, base);
                }
            } else {
                // reset prefix as soon as a non-digit is encountered
                hasPrefix = false;
            }
        }
        return new String(chars);
    }

    private static char convertToBase(char c, int base) {
        return (char) (base + (c - NUMBER_BASE));
    }
}
