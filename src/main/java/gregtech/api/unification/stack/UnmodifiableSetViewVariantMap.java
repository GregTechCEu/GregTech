package gregtech.api.unification.stack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

/**
 * Unmodifiable view of another {@link ItemVariantMap} with {@link Set} of elements as values.
 *
 * @param <E> type of the elements
 * @see ItemVariantMap#unmodifiableSetView(ItemVariantMap)
 */
final class UnmodifiableSetViewVariantMap<E> implements ItemVariantMap<Set<E>> {

    private final ItemVariantMap<? extends Set<E>> delegate;

    UnmodifiableSetViewVariantMap(@Nonnull ItemVariantMap<? extends Set<E>> delegate) {
        this.delegate = delegate;
    }

    @Nullable
    @Override
    public Set<E> getEntry(short meta) {
        Set<E> set = delegate.getEntry(meta);
        return set != null ? Collections.unmodifiableSet(set) : null;
    }

    @Override
    public boolean hasNonWildcardEntry() {
        return delegate.hasNonWildcardEntry();
    }

    @Override
    public boolean hasEntry(short meta) {
        return delegate.hasEntry(meta);
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean hasWildcardEntry() {
        return delegate.hasWildcardEntry();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
