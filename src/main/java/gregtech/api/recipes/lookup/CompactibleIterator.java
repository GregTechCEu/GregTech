package gregtech.api.recipes.lookup;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

public interface CompactibleIterator<T> extends Iterator<T> {

    /**
     * Returns an equivalent iterator that has been compacted to reduce its memory size.
     * This may be an expensive operation so only call it if necessary!
     * 
     * @return an equivalent iterator that takes up less memory
     */
    @NotNull Iterator<T> compact();

    CompactibleIterator<Object> EMPTY = new CompactibleIterator<>() {

        @Override
        public @NotNull Iterator<Object> compact() {
            return this;
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new NoSuchElementException();
        }
    };

    @SuppressWarnings("unchecked")
    @NotNull
    static <T> CompactibleIterator<T> empty() {
        return (CompactibleIterator<T>) EMPTY;
    }
}
