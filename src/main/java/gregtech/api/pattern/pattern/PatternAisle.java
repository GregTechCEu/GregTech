package gregtech.api.pattern.pattern;

public class PatternAisle {

    // not final because of setRepeatable and i need to have compat
    // actualRepeats stores the information for multiblock checks, while minRepeats and maxRepeats are rules
    protected int minRepeats, maxRepeats, actualRepeats;
    protected final String[] pattern;

    public PatternAisle(int minRepeats, int maxRepeats, String[] pattern) {
        this.minRepeats = minRepeats;
        this.maxRepeats = maxRepeats;
        this.pattern = pattern;
    }

    public PatternAisle(int repeats, String[] pattern) {
        this.minRepeats = this.maxRepeats = repeats;
        this.pattern = pattern;
    }

    public void setRepeats(int minRepeats, int maxRepeats) {
        this.minRepeats = minRepeats;
        this.maxRepeats = maxRepeats;
    }

    public void setRepeats(int repeats) {
        this.minRepeats = this.maxRepeats = repeats;
    }

    public void setActualRepeats(int actualRepeats) {
        this.actualRepeats = actualRepeats;
    }

    public int getActualRepeats() {
        return this.actualRepeats;
    }

    /**
     * Gets the first instance of the char in the pattern
     * 
     * @param c The char to find
     * @return An int array in the form of [ index into String[], index into String#charAt ], or null if it was not
     *         found
     */
    public int[] firstInstanceOf(char c) {
        for (int strI = 0; strI < pattern.length; strI++) {
            int pos = pattern[strI].indexOf(c);
            if (pos != -1) return new int[] { strI, pos };
        }
        return null;
    }

    /**
     * Gets the char at the specified position.
     * 
     * @param stringI The string index to get from
     * @param charI   The char index to get
     * @return The char
     */
    public char charAt(int stringI, int charI) {
        return pattern[stringI].charAt(charI);
    }

    public int getStringCount() {
        return pattern.length;
    }

    public int getCharCount() {
        return pattern[0].length();
    }

    public PatternAisle copy() {
        PatternAisle clone = new PatternAisle(minRepeats, maxRepeats, pattern.clone());
        clone.actualRepeats = this.actualRepeats;
        return clone;
    }
}
