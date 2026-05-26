package gregtech.api.util.function;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@FunctionalInterface
public interface FloatConsumer extends Consumer<Float> {

    @Override
    default void accept(@NotNull Float value) {
        apply(value);
    }

    void apply(float value);
}
