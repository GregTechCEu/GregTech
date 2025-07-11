package gregtech.api.util.function;

import java.util.function.Supplier;

@FunctionalInterface
public interface FloatSupplier extends Supplier<Float> {

    @Override
    default Float get() {
        return getAsFloat();
    }

    float getAsFloat();
}
