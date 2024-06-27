package gregtech.api.pipenet.alg.iter;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SimpleCacheableIterator<T> implements ICacheableIterator<T> {

    private final Iterable<T> prototype;
    private final Iterator<T> backingIterator;

    public SimpleCacheableIterator(Iterable<T> prototype) {
        this.prototype = prototype;
        this.backingIterator = prototype.iterator();
    }

    @Override
    public SimpleCacheableIterator<T> newCacheableIterator() {
        return new SimpleCacheableIterator<>(prototype);
    }

    @Override
    public Iterator<T> newIterator() {
        return prototype.iterator();
    }

    @Override
    public boolean hasNext() {
        return backingIterator.hasNext();
    }

    @Override
    public T next() {
        return backingIterator.next();
    }

    @Override
    public void remove() {
        backingIterator.remove();
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        backingIterator.forEachRemaining(action);
    }
}
