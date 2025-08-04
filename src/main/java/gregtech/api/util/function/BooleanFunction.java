package gregtech.api.util.function;

import java.util.function.Function;

@FunctionalInterface
public interface BooleanFunction<T> extends Function<T, Boolean> {

    @Override
    default Boolean apply(T t) {
        return applyAsBoolean(t);
    }

    boolean applyAsBoolean(T t);
}
