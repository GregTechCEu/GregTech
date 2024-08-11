package gregtech.api.pattern.pattern;

import gregtech.api.pattern.OriginOffset;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.GTLog;
import gregtech.api.util.RelativeDirection;

import com.google.common.base.Joiner;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.util.RelativeDirection.*;

/**
 * A builder class for {@link BlockPattern}<br />
 * When the multiblock is placed, its facings are concrete. Then, the {@link RelativeDirection}s passed into
 * {@link FactoryBlockPattern#start(RelativeDirection, RelativeDirection, RelativeDirection)} are ways in which the
 * pattern progresses. It can be thought like this, where startPos() is either defined via
 * {@link FactoryBlockPattern#startOffset(OriginOffset)}
 * , or automatically detected(for legacy compat only, you should use
 * {@link FactoryBlockPattern#startOffset(OriginOffset)} always for new code):
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

    protected static final Joiner COMMA_JOIN = Joiner.on(",");

    /**
     * In the form of [ num aisles, num string per aisle, num char per string ]
     */
    private final int[] dimensions = { -1, -1, -1 };
    /**
     * Look at the field with the same name in {@link BlockPattern} for docs
     */
    private OriginOffset offset;
    private char centerChar;

    private final List<PatternAisle> aisles = new ArrayList<>();

    /**
     * Map going from chars to the predicates
     */
    private final Char2ObjectMap<TraceabilityPredicate> symbolMap = new Char2ObjectOpenHashMap<>();

    /**
     * In the form of [ aisleDir, stringDir, charDir ]
     */
    private final RelativeDirection[] structureDir = new RelativeDirection[3];

    /**
     * @see FactoryBlockPattern#start(RelativeDirection, RelativeDirection, RelativeDirection)
     */
    private FactoryBlockPattern(RelativeDirection aisleDir, RelativeDirection stringDir, RelativeDirection charDir) {
        structureDir[0] = aisleDir;
        structureDir[1] = stringDir;
        structureDir[2] = charDir;
        int flags = 0;
        for (int i = 0; i < 3; i++) {
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

    /**
     * Sets a part of the start offset in the given direction.
     */
    public FactoryBlockPattern startOffset(OriginOffset offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Starts the builder, this is equivlent to calling
     * {@link FactoryBlockPattern#start(RelativeDirection, RelativeDirection, RelativeDirection)} with RIGHT, UP, BACK
     * 
     * @see FactoryBlockPattern#start(RelativeDirection, RelativeDirection, RelativeDirection)
     */
    public static FactoryBlockPattern start() {
        return new FactoryBlockPattern(BACK, UP, RIGHT);
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
    public static FactoryBlockPattern start(RelativeDirection aisleDir, RelativeDirection stringDir,
                                            RelativeDirection charDir) {
        return new FactoryBlockPattern(aisleDir, stringDir, charDir);
    }

    /**
     * Puts a symbol onto the predicate map
     * 
     * @param symbol       The symbol, will override previous identical ones
     * @param blockMatcher The predicate to put
     */
    public FactoryBlockPattern where(char symbol, TraceabilityPredicate blockMatcher) {
        this.symbolMap.put(symbol, new TraceabilityPredicate(blockMatcher).sort());
        if (blockMatcher.isCenter()) centerChar = symbol;
        return this;
    }

    public BlockPattern build() {
        checkMissingPredicates();
        this.dimensions[0] = aisles.size();
        // the reason this exists is before this rewrite, MultiblockControllerBase would
        // pass the opposite front facing into the pattern check, but now this is fixed.
        //
        if (offset == null) GTLog.logger.warn(
                "You didn't use .startOffset() on the builder! Start offset will now be auto detected, which may product unintended results!");
        return new BlockPattern(aisles.toArray(new PatternAisle[0]), dimensions, structureDir, offset, symbolMap,
                centerChar);
    }

    private void checkMissingPredicates() {
        List<Character> list = new ArrayList<>();

        for (Char2ObjectMap.Entry<TraceabilityPredicate> entry : this.symbolMap.char2ObjectEntrySet()) {
            if (entry.getValue() == null) {
                list.add(entry.getCharKey());
            }
        }

        if (!list.isEmpty()) {
            throw new IllegalStateException("Predicates for character(s) " + COMMA_JOIN.join(list) + " are missing");
        }
    }
}
