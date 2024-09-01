package gregtech.api.recipes.tree.property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;

public interface IPropertyFilter {

    /**
     * {@link IPropertyFilter}s should be limited in scope using this method.
     */
    boolean filterEquals(@Nullable IPropertyFilter other);

    /**
     * {@link IPropertyFilter}s should strive to avoid hash collisions with <i>all other instances</i> of
     * {@link IPropertyFilter} using this method.
     */
    int filterHash();
    // reserved hashes:
    // 0 circuit filter
    // 1 voltage filter

    @NotNull Filter getNewFilter();

    interface Filter {

        void accumulate(short recipeID, @NotNull IPropertyFilter filter);

        void filter(@NotNull BitSet recipeMask, @NotNull PropertySet properties);
    }
}
