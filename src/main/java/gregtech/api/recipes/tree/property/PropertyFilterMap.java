package gregtech.api.recipes.tree.property;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

import java.util.BitSet;

public final class PropertyFilterMap extends Object2ObjectOpenCustomHashMap<IPropertyFilter, IPropertyFilter.Filter> {

    private static final Hash.Strategy<IPropertyFilter> STRATEGY = new Strategy<>() {

        @Override
        public int hashCode(IPropertyFilter o) {
            return o.filterHash();
        }

        @Override
        public boolean equals(IPropertyFilter a, IPropertyFilter b) {
            if (a == null) {
                if (b == null) return true;
                else return b.filterEquals(null);
            } else return a.filterEquals(b);
        }
    };

    public PropertyFilterMap() {
        super(STRATEGY);
    }

    public void addFilter(int recipeID, IPropertyFilter filter) {
        addFilter((short) recipeID, filter);
    }

    public void addFilter(short recipeID, IPropertyFilter filter) {
        this.computeIfAbsent(filter, IPropertyFilter::getNewFilter).accumulate(recipeID, filter);
    }

    public BitSet filter(PropertySet properties) {
        BitSet bitSet = new BitSet();
        filter(bitSet, properties);
        return bitSet;
    }

    public void filter(BitSet bitSet, PropertySet properties) {
        for (IPropertyFilter.Filter filter : values()) {
            filter.filter(bitSet, properties);
        }
    }
}
