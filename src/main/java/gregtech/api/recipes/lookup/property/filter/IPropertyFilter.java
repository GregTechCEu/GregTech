package gregtech.api.recipes.lookup.property.filter;

import gregtech.api.recipes.lookup.property.PropertySet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;

public interface IPropertyFilter<T> {

    /**
     * {@link IPropertyFilter}s should be limited in scope using this method.
     */
    boolean filterEquals(@Nullable IPropertyFilter<?> other);

    /**
     * {@link IPropertyFilter}s should strive to avoid hash collisions with <i>all other instances</i> of
     * {@link IPropertyFilter} using this method.
     */
    int filterHash();
    // reserved hashes:
    // -1 circuit filter
    // 0 power usage filter
    // 1 power generation filter
    // 2 cleanroom filter
    // 3 eu to start filter
    // 4 temperature filter
    // 5 dimension filter
    // 6 CWUt filter
    // 7 biome filter

    /**
     * @return a new filter instance for filtering during recipe search.
     */
    @NotNull
    Filter<T> getNewFilter();

    interface Filter<T> {

        void accumulate(short recipeID, @NotNull T filterInformation);

        void filter(@NotNull BitSet recipeMask, @NotNull PropertySet properties);
    }
}
