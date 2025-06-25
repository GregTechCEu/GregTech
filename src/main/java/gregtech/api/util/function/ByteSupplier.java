package gregtech.api.util.function;

import java.util.function.Supplier;

@FunctionalInterface
public interface ByteSupplier extends Supplier<Byte> {

    @Override
    default Byte get() {
        return getByte();
    }

    byte getByte();
}
