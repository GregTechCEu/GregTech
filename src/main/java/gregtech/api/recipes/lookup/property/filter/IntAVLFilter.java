package gregtech.api.recipes.lookup.property.filter;

import gregtech.api.util.GTLog;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.BitSet;

public final class IntAVLFilter extends Int2ObjectAVLTreeMap<BitSet> {

    private static final BitSet EMPTY = new BitSet();
    private final BitSet zeroReference = new BitSet();
    private final FilterEqualityBehavior equalityBehavior;

    @Contract("_, false, false -> fail")
    public IntAVLFilter(FilterEqualityBehavior equalityBehavior, boolean handlesPositive, boolean handlesNegative) {
        if (!handlesNegative && !handlesPositive)
            throw new IllegalArgumentException(
                    "Cannot have a filter which handles neither positive nor negative numbers!");
        if (equalityBehavior == FilterEqualityBehavior.EQUAL)
            GTLog.logger.warn("AVL filters should not be used with EQUAL behavior!", new Throwable());
        this.put(0, zeroReference);
        if (handlesPositive && !equalityBehavior.matchesAbove()) {
            this.put(Integer.MAX_VALUE, new BitSet());
        }
        if (handlesNegative && !equalityBehavior.matchesBelow()) {
            this.put(Integer.MIN_VALUE, new BitSet());
        }
        this.equalityBehavior = equalityBehavior;
    }

    public void filter(@NotNull BitSet recipeMask, int key) {
        if (key == 0) {
            recipeMask.or(zeroReference);
            return;
        }
        boolean optimize = this.equalityBehavior == FilterEqualityBehavior.GREATER_THAN_OR_EQUAL ||
                this.equalityBehavior == FilterEqualityBehavior.LESS_THAN_OR_EQUAL;
        if (!equalityBehavior.matchesAbove()) {
            recipeMask.or(this.getFirstAbove(optimize ? key - 1 : key));
        }
        if (!equalityBehavior.matchesBelow()) {
            recipeMask.or(this.getFirstBelow(optimize ? key + 1 : key));
        }
        if (!this.equalityBehavior.matchesEqual()) {
            recipeMask.or(this.getOrDefault(key, EMPTY));
        }
    }

    public void accumulate(short recipeID, int address) {
        boolean eq = equalityBehavior.matchesEqual();
        if (eq) this.computeIfAbsent(address, this::newBitSet);

        if (!equalityBehavior.matchesAbove()) {
            this.computeIfAbsent(eq ? address + 1 : address, k -> (BitSet) this.getFirstBelow(k).clone()).set(recipeID);
            // mark all bitsets referring to greater counts that this recipeID is invalid.
            for (BitSet bitset : this.tailMap(address).values()) {
                bitset.set(recipeID);
            }
        }
        if (!equalityBehavior.matchesBelow()) {
            this.computeIfAbsent(eq ? address - 1 : address, k -> (BitSet) this.getFirstAbove(k).clone()).set(recipeID);
            // mark all bitsets referring to smaller counts that this recipeID is invalid.
            for (BitSet bitset : this.headMap(address).values()) {
                bitset.set(recipeID);
            }
        }
    }

    private BitSet newBitSet(int key) {
        BitSet set = new BitSet();
        if (equalityBehavior.matchesAbove()) {
            set.or(this.getFirstAbove(key));
        }
        if (equalityBehavior.matchesBelow()) {
            set.or(this.getFirstBelow(key));
        }
        return set;
    }

    /**
     * Returns the first entry strictly below the given key in sorting order.
     */
    public @NotNull @UnmodifiableView BitSet getFirstBelow(int key) {
        var view = this.headMap(key).int2ObjectEntrySet();
        return view.isEmpty() ? EMPTY : view.last().getValue();
    }

    /**
     * Returns the first entry strictly above the given key in sorting order.
     */
    public @NotNull @UnmodifiableView BitSet getFirstAbove(int key) {
        var view = this.tailMap(key).int2ObjectEntrySet();
        return view.isEmpty() ? EMPTY : view.first().getValue();
    }

    /**
     * Returns a view of the portion of this sorted map whose keys are strictly greater
     * than <code>from</code>.
     */
    @Override
    public Int2ObjectSortedMap<BitSet> tailMap(int from) {
        // make tail map strictly greater than, similar to head map.
        return super.tailMap(from + 1);
    }
}
