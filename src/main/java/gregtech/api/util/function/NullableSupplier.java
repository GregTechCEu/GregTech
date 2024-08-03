package gregtech.api.util.function;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@FunctionalInterface
public interface NullableSupplier<T> extends Supplier<T> {

    @Override
    @Nullable T get();
}
