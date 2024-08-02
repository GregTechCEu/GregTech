package gregtech.api.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public final class SingletonLazyIterator<T> implements Iterator<T> {

    private final Supplier<T> action;
    private boolean run;
    private T value;

    public SingletonLazyIterator(@NotNull Supplier<@Nullable T> action) {
        this.action = action;
    }

    @Override
    public boolean hasNext() {
        if (!run) {
            this.value = action.get();
            this.run = true;
        }
        return value != null;
    }

    @Override
    public T next() {
        if (!run) {
            this.value = action.get();
            this.run = true;
        }

        if (value == null) {
            throw new NoSuchElementException();
        }

        T tmp = value;
        this.value = null;
        return tmp;
    }
}
