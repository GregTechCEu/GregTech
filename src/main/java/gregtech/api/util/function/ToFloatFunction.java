package gregtech.api.util.function;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface ToFloatFunction<T> extends Function<T, Float> {

    float applyAsFloat(T t);

    @Deprecated
    @Override
    default Float apply(T t) {
        return applyAsFloat(t);
    }

    default <V extends Float> ToFloatFunction<V> composeAsFloat(@NotNull Function<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (V v) -> applyAsFloat(before.apply(v));
    }

    default <V extends Float> ToFloatFunction<T> andThenAsFloat(@NotNull Function<? super Float, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(applyAsFloat(t));
    }
}
