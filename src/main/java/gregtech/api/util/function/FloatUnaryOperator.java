package gregtech.api.util.function;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.UnaryOperator;

@SuppressWarnings("unused")
@FunctionalInterface
public interface FloatUnaryOperator extends UnaryOperator<Float> {

    FloatUnaryOperator NOOP = v -> v;

    float applyAsFloat(float t);

    @Override
    default Float apply(Float v) {
        return applyAsFloat(v);
    }

    default @NotNull FloatUnaryOperator composeAsFloat(@NotNull FloatUnaryOperator before) {
        Objects.requireNonNull(before, "Passed null \"before\" to FloatUnaryOperator#composeAsFloat");
        return v -> applyAsFloat(before.applyAsFloat(v));
    }

    default @NotNull FloatUnaryOperator andThenAsFloat(@NotNull FloatUnaryOperator after) {
        Objects.requireNonNull(after, "Passed null \"after\" to FloatUnaryOperator#andThenAsFloat");
        return v -> after.applyAsFloat(applyAsFloat(v));
    }
}
