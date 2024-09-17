package gregtech.api.recipes.lookup.property;

import gregtech.api.recipes.lookup.property.filter.IPropertyFilter;
import gregtech.api.recipes.lookup.property.filter.RecipePropertyWithFilter;
import gregtech.api.recipes.properties.RecipeProperty;
import gregtech.api.recipes.properties.RecipePropertyStorage;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;

public final class PropertyFilterMap extends
                                     Object2ObjectOpenCustomHashMap<IPropertyFilter<?>, IPropertyFilter.Filter<?>> {

    private static final Hash.Strategy<IPropertyFilter<?>> STRATEGY = new Strategy<>() {

        @Override
        public int hashCode(IPropertyFilter o) {
            return o.filterHash();
        }

        @Override
        public boolean equals(IPropertyFilter<?> a, IPropertyFilter<?> b) {
            if (a == null) {
                if (b == null) return true;
                else return b.filterEquals(null);
            } else return a.filterEquals(b);
        }
    };

    public PropertyFilterMap() {
        super(STRATEGY);
    }

    public void addFilters(int recipeID, RecipePropertyStorage storage) {
        for (var entry : storage.entrySet()) {
            if (entry.getKey() instanceof RecipePropertyWithFilter<?>filter) {
                handle(recipeID, filter, storage);
            }
        }
    }

    private <T> void handle(int recipeID, RecipePropertyWithFilter<T> property, RecipePropertyStorage storage) {
        addFilter(recipeID, property, storage.get(property, null));
    }

    public <H, T extends RecipeProperty<H> & IPropertyFilter<H>> void addFilter(int recipeID, T filter, H obj) {
        addFilter((short) recipeID, filter, obj);
    }

    @SuppressWarnings("unchecked")
    public <H, T extends RecipeProperty<H> & IPropertyFilter<H>> void addFilter(short recipeID, T filter, H obj) {
        if (obj == null) return;
        ((IPropertyFilter.Filter<H>) this.computeIfAbsent(filter, IPropertyFilter::getNewFilter)).accumulate(recipeID,
                obj);
    }

    public @NotNull BitSet filter(@NotNull PropertySet properties) {
        BitSet bitSet = new BitSet();
        filter(bitSet, properties);
        return bitSet;
    }

    public void filter(@NotNull BitSet bitSet, @NotNull PropertySet properties) {
        for (IPropertyFilter.Filter<?> filter : values()) {
            filter.filter(bitSet, properties);
        }
    }
}
