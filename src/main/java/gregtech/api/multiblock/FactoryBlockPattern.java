package gregtech.api.multiblock;

import com.google.common.base.Joiner;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gregtech.api.util.RelativeDirection;
import gregtech.api.util.IntRange;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import static gregtech.api.util.RelativeDirection.*;

public class FactoryBlockPattern {

    private static final Joiner COMMA_JOIN = Joiner.on(",");
    private final List<String[]> depth = new ArrayList<>();
    private final List<int[]> aisleRepetitions = new ArrayList<>();
    private final Map<Character, IntRange> countLimits = new HashMap<>();
    private final Map<Character, Predicate<BlockWorldState>> symbolMap = new HashMap<>();
    private final TIntObjectMap<Predicate<PatternMatchContext>> layerValidators = new TIntObjectHashMap<>();
    private final List<Predicate<PatternMatchContext>> contextValidators = new ArrayList<>();
    private int aisleHeight;
    private int rowWidth;
    private final RelativeDirection[] structureDir = new RelativeDirection[3];

    private FactoryBlockPattern(RelativeDirection charDir, RelativeDirection stringDir, RelativeDirection aisleDir) {
        structureDir[0] = charDir;
        structureDir[1] = stringDir;
        structureDir[2] = aisleDir;
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
        this.symbolMap.put(' ', k -> true);
    }

    /**
     * Adds a repeatable aisle to this pattern.
     */
    public FactoryBlockPattern aisleRepeatable(int minRepeat, int maxRepeat, String... aisle) {
        if (!ArrayUtils.isEmpty(aisle) && !StringUtils.isEmpty(aisle[0])) {
            if (this.depth.isEmpty()) {
                this.aisleHeight = aisle.length;
                this.rowWidth = aisle[0].length();
            }

            if (aisle.length != this.aisleHeight) {
                throw new IllegalArgumentException("Expected aisle with height of " + this.aisleHeight + ", but was given one with a height of " + aisle.length + ")");
            } else {
                for (String s : aisle) {
                    if (s.length() != this.rowWidth) {
                        throw new IllegalArgumentException("Not all rows in the given aisle are the correct width (expected " + this.rowWidth + ", found one with " + s.length() + ")");
                    }

                    for (char c0 : s.toCharArray()) {
                        if (!this.symbolMap.containsKey(c0)) {
                            this.symbolMap.put(c0, null);
                        }
                    }
                }

                this.depth.add(aisle);
                if (minRepeat > maxRepeat)
                    throw new IllegalArgumentException("Lower bound of repeat counting must smaller than upper bound!");
                aisleRepetitions.add(new int[]{minRepeat, maxRepeat});
                return this;
            }
        } else {
            throw new IllegalArgumentException("Empty pattern for aisle");
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
     */
    public FactoryBlockPattern setRepeatable(int minRepeat, int maxRepeat) {
        if (minRepeat > maxRepeat)
            throw new IllegalArgumentException("Lower bound of repeat counting must smaller than upper bound!");
        aisleRepetitions.set(aisleRepetitions.size() - 1, new int[]{minRepeat, maxRepeat});
        return this;
    }

    /**
     * Set last aisle repeatable
     */
    public FactoryBlockPattern setRepeatable(int repeatCount) {
        return setRepeatable(repeatCount, repeatCount);
    }

    public FactoryBlockPattern setAmountLimit(char symbol, int minAmount, int maxLimit) {
        this.symbolMap.put(symbol, null);
        this.countLimits.put(symbol, new IntRange(minAmount, maxLimit));
        return this;
    }

    public FactoryBlockPattern setAmountAtLeast(char symbol, int minValue) {
        return setAmountLimit(symbol, minValue, Integer.MAX_VALUE);
    }

    public FactoryBlockPattern setAmountAtMost(char symbol, int maxValue) {
        return setAmountLimit(symbol, 0, maxValue);
    }

    public static FactoryBlockPattern start() {
        return new FactoryBlockPattern(RIGHT, UP, BACK);
    }

    public static FactoryBlockPattern start(RelativeDirection charDir, RelativeDirection stringDir, RelativeDirection aisleDir) {
        return new FactoryBlockPattern(charDir, stringDir, aisleDir);
    }

    public FactoryBlockPattern where(char symbol, Predicate<BlockWorldState> blockMatcher) {
        this.symbolMap.put(symbol, blockMatcher);
        return this;
    }

    /**
     * Adds predicate to be run after multiblock checking to validate
     * pattern matching context before succeeding match sequence
     */
    public FactoryBlockPattern validateContext(Predicate<PatternMatchContext> validator) {
        this.contextValidators.add(validator);
        return this;
    }

    /**
     * Adds predicate to validate given layer using given validator
     * Given context is layer-local and can be accessed via {@link BlockWorldState#getLayerContext()}
     */
    public FactoryBlockPattern validateLayer(int layerIndex, Predicate<PatternMatchContext> layerValidator) {
        this.layerValidators.put(layerIndex, layerValidator);
        return this;
    }

    public BlockPattern build() {
        return new BlockPattern(makePredicateArray(), makeCountLimitsList(),
                layerValidators, contextValidators,
                structureDir, aisleRepetitions.toArray(new int[aisleRepetitions.size()][]));
    }

    @SuppressWarnings("unchecked")
    private Predicate<BlockWorldState>[][][] makePredicateArray() {
        this.checkMissingPredicates();
        Predicate<BlockWorldState>[][][] predicate = (Predicate<BlockWorldState>[][][]) Array.newInstance(Predicate.class, this.depth.size(), this.aisleHeight, this.rowWidth);

        for (int i = 0; i < this.depth.size(); ++i) {
            for (int j = 0; j < this.aisleHeight; ++j) {
                for (int k = 0; k < this.rowWidth; ++k) {
                    predicate[i][j][k] = this.symbolMap.get(this.depth.get(i)[j].charAt(k));
                }
            }
        }

        return predicate;
    }

    private List<Pair<Predicate<BlockWorldState>, IntRange>> makeCountLimitsList() {
        List<Pair<Predicate<BlockWorldState>, IntRange>> array = new ArrayList<>(countLimits.size());
        for (Entry<Character, IntRange> entry : this.countLimits.entrySet()) {
            Predicate<BlockWorldState> predicate = this.symbolMap.get(entry.getKey());
            array.add(Pair.of(predicate, entry.getValue()));
        }
        return array;
    }

    private void checkMissingPredicates() {
        List<Character> list = new ArrayList<>();

        for (Entry<Character, Predicate<BlockWorldState>> entry : this.symbolMap.entrySet()) {
            if (entry.getValue() == null) {
                list.add(entry.getKey());
            }
        }

        if (!list.isEmpty()) {
            throw new IllegalStateException("Predicates for character(s) " + COMMA_JOIN.join(list) + " are missing");
        }
    }
}
