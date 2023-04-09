package gregtech.api.unification.stack;

import javax.annotation.Nullable;

/**
 * {@link ItemVariantMap} implementation which holds one shared value for all
 * variants of an item. All operations, including set accesses like
 * {@link #setEntry(short, Object)}, regardless of the metadata value provided,
 * will retrieve or modify the same and only entry of this collection.
 *
 * @param <E> type of the elements
 */
public final class SingleItemVariantMap<E> implements ItemVariantMap.Mutable<E> {

    @Nullable
    private E entry;

    public boolean hasEntry() {
        return entry != null;
    }

    /**
     * @return the value contained
     */
    @Nullable
    public E getEntry() {
        return entry;
    }

    /**
     * @param e new value, or {@code null} for entry removal
     * @return previous valued contained, or {@code null} if there was no such value.
     */
    @Nullable
    public E setEntry(@Nullable E e) {
        E cache = this.entry;
        this.entry = e;
        return cache;
    }

    @Override
    public boolean hasNonWildcardEntry() {
        return entry != null;
    }

    @Override
    public boolean hasEntry(short meta) {
        return entry != null;
    }

    @Nullable
    @Override
    public E getEntry(short meta) {
        return entry;
    }

    @Nullable
    @Override
    public E setEntry(short meta, @Nullable E e) {
        return setEntry(e);
    }

    @Override
    public void clear() {
        this.entry = null;
    }

    @Override
    public String toString() {
        return "SingleItemVariantMap{" +
                "entry=" + this.entry +
                '}';
    }
}
