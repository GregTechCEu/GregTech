package gregtech.api.util;

import org.jetbrains.annotations.NotNull;

public final class SmallDigits {

    @SuppressWarnings("UnnecessaryUnicodeEscape")
    private static final int SMALL_DOWN_NUMBER_BASE = '\u2080';
    @SuppressWarnings("UnnecessaryUnicodeEscape")
    private static final int SMALL_UP_NUMBER_BASE = '\u2080';
    private static final int NUMBER_BASE = '0';

    private SmallDigits() {}

    @NotNull
    public static String toSmallUpNumbers(String string) {
        return convert(string, SMALL_UP_NUMBER_BASE);
    }

    @NotNull
    public static String toSmallDownNumbers(String string) {
        return convert(string, SMALL_DOWN_NUMBER_BASE);
    }

    @NotNull
    private static String convert(@NotNull String string, int base) {
        boolean hasPrecedingDash = false;
        char[] charArray = string.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            boolean isDash = c == '-';
            if (isDash) hasPrecedingDash = true;

            int relativeIndex = c - NUMBER_BASE;
            if (relativeIndex >= 0 && relativeIndex <= 9) {
                if (!hasPrecedingDash) {
                    // no preceding dash, so convert the char
                    charArray[i] = (char) (base + relativeIndex);
                }
            } else if (!isDash && hasPrecedingDash) {
                // was a non-number, so invalidate the previously seen dash
                hasPrecedingDash = false;
            }
        }
        return new String(charArray);
    }
}
