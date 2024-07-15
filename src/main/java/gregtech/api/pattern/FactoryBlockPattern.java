package gregtech.api.pattern;

import gregtech.api.util.RelativeDirection;

import com.google.common.base.Joiner;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import static gregtech.api.util.RelativeDirection.*;

/**
 * A builder class for {@link BlockPattern}<br />
 * When the multiblock is placed, its facings are concrete. Then, the {@link RelativeDirection}s passed into
 * {@link FactoryBlockPattern#start(RelativeDirection, RelativeDirection, RelativeDirection)} are ways in which the
 * pattern progresses. It can be thought like this, where startPos() is either defined via
 * {@link FactoryBlockPattern#setStartOffset(int, int, int)}
 * , or automatically detected(for legacy compat only, you should use
 * {@link FactoryBlockPattern#setStartOffset(int, int, int)} always for new code):
 * 
 * <pre>
 * {@code
 * for(int aisleI in 0..aisles):
 *     for(int stringI in 0..strings):
 *         for(int charI in 0..chars):
 *             pos = startPos()
 *             pos.move(aisleI in aisleDir)
 *             pos.move(stringI in stringDir)
 *             pos.move(charI in charDir)
 *             predicate = aisles[aisleI].stringAt(stringI).charAt(charI)
 * }
 * </pre>
 */
public class FactoryBlockPattern {

    private static final Joiner COMMA_JOIN = Joiner.on(",");

    /**
     * In the form of [ num aisles, num string per aisle, num char per string ]
     */
    private final int[] dimensions = { -1, -1, -1 };
    /**
     * In the form of [ aisleOffset, stringOffset, charOffset ] where the offsets are the opposite
     * {@link RelativeDirection} of structure directions
     */
    private int[] startOffset;
    private char centerChar;

    private final List<PatternAisle> aisles = new ArrayList<>();

    /**
     * Map going from chars to the predicates
     */
    private final Char2ObjectMap<TraceabilityPredicate> symbolMap = new Char2ObjectOpenHashMap<>();

    /**
     * In the form of [ charDir, stringDir, aisleDir ]
     */
    private final RelativeDirection[] structureDir = new RelativeDirection[3];

    /**
     * @see FactoryBlockPattern#start(RelativeDirection, RelativeDirection, RelativeDirection)
     */
    private FactoryBlockPattern(RelativeDirection charDir, RelativeDirection stringDir, RelativeDirection aisleDir) {
        structureDir[0] = charDir;
        structureDir[1] = stringDir;
        structureDir[2] = aisleDir;
        int flags = 0;
        for (int i = 0; i < this.structureDir.length; i++) {
            switch (structureDir[i]) {
                case UP:
                case DOWN:
                    flags |= 0x1;
                    break;
                case LEFT:
                case RIGHT:
                    flags |= 0x2;
                    break;
                case FRONT:
                case BACK:
                    flags |= 0x4;
                    break;
            }
        }
        if (flags != 0x7) throw new IllegalArgumentException("Must have 3 different axes!");
        this.symbolMap.put(' ', TraceabilityPredicate.ANY);
    }

    /**
     * Adds a repeatable aisle to this pattern.
     * 
     * @param aisle The aisle to add
     * @see FactoryBlockPattern#setRepeatable(int, int)
     */
    public FactoryBlockPattern aisleRepeatable(int minRepeat, int maxRepeat, @NotNull String... aisle) {
        if (ArrayUtils.isEmpty(aisle) || StringUtils.isEmpty(aisle[0]))
            throw new IllegalArgumentException("Empty pattern for aisle");

        // set the dimensions if the user hasn't already
        if (dimensions[2] == -1) {
            dimensions[2] = aisle[0].length();
        }
        if (dimensions[1] == -1) {
            dimensions[1] = aisle.length;
        }

        if (aisle.length != dimensions[1]) {
            throw new IllegalArgumentException("Expected aisle with height of " + dimensions[1] +
                    ", but was given one with a height of " + aisle.length + ")");
        } else {
            for (String s : aisle) {
                if (s.length() != dimensions[2]) {
                    throw new IllegalArgumentException(
                            "Not all rows in the given aisle are the correct width (expected " + dimensions[2] +
                                    ", found one with " + s.length() + ")");
                }

                for (char c : s.toCharArray()) {
                    if (!this.symbolMap.containsKey(c)) {
                        this.symbolMap.put(c, null);
                    }
                }
            }

            aisles.add(new PatternAisle(minRepeat, maxRepeat, aisle));
            if (minRepeat > maxRepeat)
                throw new IllegalArgumentException("Lower bound of repeat counting must smaller than upper bound!");
            return this;
        }
    }

    /**
     * Adds a single aisle to this pattern. (so multiple calls to this will increase the aisleDir by 1)
     */
    public FactoryBlockPattern aisle(String... aisle) {
        return aisleRepeatable(1, 1, aisle);
    }

    /**
     * Set last aisle repeatable
     * 
     * @param minRepeat Minimum amount of repeats, inclusive
     * @param maxRepeat Maximum amount of repeats, inclusive
     */
    public FactoryBlockPattern setRepeatable(int minRepeat, int maxRepeat) {
        if (minRepeat > maxRepeat)
            throw new IllegalArgumentException("Lower bound of repeat counting must smaller than upper bound!");
        aisles.get(aisles.size() - 1).setRepeats(minRepeat, maxRepeat);
        return this;
    }

    /**
     * Set last aisle repeatable
     * 
     * @param repeatCount The amount to repeat
     */
    public FactoryBlockPattern setRepeatable(int repeatCount) {
        return setRepeatable(repeatCount, repeatCount);
    }

    public FactoryBlockPattern setStartOffset(int aisleOffset, int stringOffset, int charOffset) {
        this.startOffset[0] = aisleOffset;
        this.startOffset[1] = stringOffset;
        this.startOffset[2] = charOffset;
        return this;
    }

    /**
     * Starts the builder, this is equivlent to calling
     * {@link FactoryBlockPattern#start(RelativeDirection, RelativeDirection, RelativeDirection)} with RIGHT, UP, BACK
     * 
     * @see FactoryBlockPattern#start(RelativeDirection, RelativeDirection, RelativeDirection)
     */
    public static FactoryBlockPattern start() {
        return new FactoryBlockPattern(RIGHT, UP, BACK);
    }

    /**
     * Starts the builder, each pair of {@link RelativeDirection} must be used at exactly once!
     * 
     * @param charDir   The direction chars progress in, each successive char in a string progresses by this direction
     * @param stringDir The direction strings progress in, each successive string in an aisle progresses by this
     *                  direction
     * @param aisleDir  The direction aisles progress in, each successive {@link FactoryBlockPattern#aisle(String...)}
     *                  progresses in this direction
     */
    public static FactoryBlockPattern start(RelativeDirection charDir, RelativeDirection stringDir,
                                            RelativeDirection aisleDir) {
        return new FactoryBlockPattern(charDir, stringDir, aisleDir);
    }

    /**
     * Puts a symbol onto the predicate map
     * 
     * @param symbol       The symbol, will override previous identical ones
     * @param blockMatcher The predicate to put
     */
    public FactoryBlockPattern where(char symbol, TraceabilityPredicate blockMatcher) {
        this.symbolMap.put(symbol, new TraceabilityPredicate(blockMatcher).sort());
        if (blockMatcher.isCenter) centerChar = symbol;
        return this;
    }

    public FactoryBlockPattern where(String symbol, TraceabilityPredicate blockMatcher) {
        if (symbol.length() == 1) {
            return where(symbol.charAt(0), blockMatcher);
        }
        throw new IllegalArgumentException(
                String.format("Symbol \"%s\" is invalid! It must be exactly one character!", symbol));
    }

    public BlockPattern build() {
        checkMissingPredicates();
        this.dimensions[0] = aisles.size();
        RelativeDirection temp = structureDir[0];
        structureDir[0] = structureDir[2];
        structureDir[2] = temp;
        return new BlockPattern(aisles.toArray(new PatternAisle[0]), dimensions, structureDir, startOffset, symbolMap,
                centerChar);
    }

    private void checkMissingPredicates() {
        List<Character> list = new ArrayList<>();

        for (Entry<Character, TraceabilityPredicate> entry : this.symbolMap.entrySet()) {
            if (entry.getValue() == null) {
                list.add(entry.getKey());
            }
        }

        if (!list.isEmpty()) {
            throw new IllegalStateException("Predicates for character(s) " + COMMA_JOIN.join(list) + " are missing");
        }
    }
}
