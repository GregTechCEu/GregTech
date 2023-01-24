package gregtech.api.fluids.info;

import gregtech.api.unification.material.Material;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;

public final class FluidTypes {

    private static final Map<String, FluidType> registeredKeys = new Object2ObjectOpenHashMap<>();

    public static FluidType LIQUID = createType("liquid");
    public static FluidType GAS = createType("gas");
    public static FluidType PLASMA = createType("plasma", "plasma");

    private FluidTypes() {/**/}

    @Nonnull
    public static FluidType createType(@Nonnull String name) {
        return createType(name, FluidType.createDefaultFunction());
    }

    @Nonnull
    public static FluidType createType(@Nonnull String name, @Nonnull String prefix) {
        return createType(name, FluidType.createDefaultFunction(prefix));
    }

    @Nonnull
    public static FluidType createType(@Nonnull String name, @Nullable String prefix, @Nonnull String suffix) {
        return createType(name, FluidType.createDefaultFunction(prefix, suffix));
    }

    @Nonnull
    public static FluidType createType(@Nonnull String name, @Nonnull Function<Material, String> materialNameFunction) {
        return registeredKeys.computeIfAbsent(name, n -> new FluidType(n, materialNameFunction));
    }


    @Nullable
    public static FluidType getType(@Nonnull String name) {
        return registeredKeys.get(name);
    }
}
