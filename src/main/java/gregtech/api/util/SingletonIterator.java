package gregtech.api.util;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class SingletonIterator<T> implements Iterator<T> {

    private T value;

    public SingletonIterator(@NotNull T value) {
        this.value = value;
    }

    @Override
    public boolean hasNext() {
        return value != null;
    }

    @Override
    public T next() {
        if (value == null) {
            throw new NoSuchElementException();
        }

        T tmp = value;
        this.value = null;
        return tmp;
    }
}
