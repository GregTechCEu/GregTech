package gregtech.api.util;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayIterator<T> implements Iterator<T> {

    @NotNull
    private final T[] backingArray;
    private int index = 0;

    public ArrayIterator(@NotNull T[] backingArray) {
        this.backingArray = backingArray;
    }

    @Override
    public boolean hasNext() {
        return backingArray.length > index;
    }

    @Override
    public T next() {
        if (!hasNext()) throw new NoSuchElementException();
        return backingArray[index++];
    }
}
