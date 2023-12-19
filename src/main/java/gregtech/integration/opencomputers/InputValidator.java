package gregtech.integration.opencomputers;

import li.cil.oc.api.machine.Arguments;

/**
 * Class used to validate OC raw inputs
 */
public class InputValidator {

    /**
     * Validate that the int at a given index is within the provided bounds.
     *
     * @param args  The OC arguments object
     * @param index The index to find the integer at
     * @param min   The minimum value of the integer, inclusive
     * @param max   The maximum value of the integer, inclusive
     *
     * @return The value, if valid
     * @throws IllegalArgumentException if the argument is not an integer, or if it is out of bounds
     */
    public static int getInteger(Arguments args, int index, int min, int max) {
        int value = args.checkInteger(index);
        if (value > max || value < min) {
            throw new IllegalArgumentException("Passed value must be within " + min + " and " + max);
        }
        return value;
    }

    public static <T extends Enum<?>> T getEnumArrayIndex(Arguments args, int argsIndex, T[] array) {
        int value = args.checkInteger(argsIndex);
        if (value < 0 || value >= array.length) {
            throw new IllegalArgumentException("Passed value must be within 0 and " + (array.length - 1));
        }
        return array[value];
    }

    /**
     * Validate that the String at a given index is a valid hex-color code.
     *
     * Can be in the formats:
     * - 0xFFFFFFFF
     * - FFFFFFFF
     *
     * MUST be 8 characters, 0-9 or A-F. May optionally include "0x" at the beginning.
     */
    public static String getColorString(Arguments args, int index) {
        String colorString = args.checkString(index);
        if (colorString == null) {
            throw new IllegalArgumentException("Must pass a color string.");
        }
        if (colorString.startsWith("0x")) {
            colorString = colorString.substring(2);
        }
        if (colorString.length() != 8) {
            throw new IllegalArgumentException(
                    "String " + colorString + " is not valid, must be 8 characters long beyond \"0x\".");
        }
        try {
            Long.parseLong(colorString, 16);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "String " + colorString + " is not a valid code, must be only numbers (0-9) and letters (A-F).");
        }
        return colorString;
    }
}
