package gregtech.api.util.reference;

import it.unimi.dsi.fastutil.objects.AbstractObjectIterator;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.Nullable;

public abstract class ExpiringReferenceHashSet<T> extends AbstractObjectSet<T> {

    private final ObjectOpenHashSet<ExpiringReference<T>> backer = new ObjectOpenHashSet<>();

    @Override
    public int size() {
        return backer.size();
    }

    @Override
    public boolean isEmpty() {
        return backer.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        // noinspection unchecked
        return backer.contains(wrapObject((T) o));
    }

    @Override
    public ObjectIterator<T> iterator() {
        return new SetIterator();
    }

    @Override
    public boolean add(T t) {
        return backer.add(wrapObject(t));
    }

    public void clearStaleEntries() {
        // iteration automatically removes stale entries.
        // noinspection StatementWithEmptyBody
        for (T ignored : this);
    }

    /**
     * Clears stale entries and then performs {@link ObjectOpenHashSet#trim()}
     */
    public void trim() {
        clearStaleEntries();
        backer.trim();
    }

    /**
     * Clears stale entries and then performs {@link ObjectOpenHashSet#trim(int)}
     */
    public void trim(final int n) {
        clearStaleEntries();
        backer.trim(n);
    }

    protected abstract ExpiringReference<T> wrapObject(T obj);

    /**
     * Implementations must override equals() and hashCode() with calls to their referenced object.
     * If expired, return false or 0.
     */
    protected interface ExpiringReference<T> {

        @Nullable
        T get();

        default boolean expired() {
            return get() == null;
        }
    }

    protected class SetIterator extends AbstractObjectIterator<T> {

        protected final ObjectIterator<ExpiringReference<T>> backer = ExpiringReferenceHashSet.this.backer.iterator();

        protected T next;

        @Override
        public boolean hasNext() {
            if (!backer.hasNext()) return false;
            if (this.next == null) computeNext();
            return this.next != null;
        }

        @Override
        public T next() {
            if (this.next == null) computeNext();
            T next = this.next;
            this.next = null;
            return next;
        }

        protected void computeNext() {
            while (true) {
                ExpiringReference<T> ref = backer.next();
                this.next = ref.get();
                if (ref.expired()) {
                    backer.remove();
                } else break;
                if (!backer.hasNext()) break;
            }
        }

        @Override
        public void remove() {
            backer.remove();
        }
    }
}
