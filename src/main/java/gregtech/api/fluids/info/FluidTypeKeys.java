package gregtech.api.fluids.info;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public final class FluidTypeKeys {

    private static final Map<String, FluidTypeKey> registeredKeys = new Object2ObjectOpenHashMap<>();

    public static FluidTypeKey LIQUID = createKey("liquid");
    public static FluidTypeKey GAS = createKey("gas");
    public static FluidTypeKey PLASMA = createKey("plasma", "plasma");

    private FluidTypeKeys() {/**/}

    @Nonnull
    public static FluidTypeKey createKey(@Nonnull String name) {
        return createKey(name, null);
    }

    @Nonnull
    public static FluidTypeKey createKey(@Nonnull String name, @Nullable String prefix) {
        return createKey(name, prefix, null);
    }

    @Nonnull
    public static FluidTypeKey createKey(@Nonnull String name, @Nullable String prefix, @Nullable String suffix) {
        return registeredKeys.computeIfAbsent(name, n -> new FluidTypeKey(n, prefix, suffix));
    }
}
