package gregtech.api.unification.stack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    UnmodifiableSetViewVariantMap(@NotNull ItemVariantMap<? extends Set<E>> delegate) {
        this.delegate = delegate;
    }

    @Nullable
    @Override
    public Set<E> get(short meta) {
        Set<E> set = delegate.get(meta);
        return set != null ? Collections.unmodifiableSet(set) : null;
    }

    @Override
    public boolean hasNonWildcardEntry() {
        return delegate.hasNonWildcardEntry();
    }

    @Override
    public boolean has(short meta) {
        return delegate.has(meta);
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
