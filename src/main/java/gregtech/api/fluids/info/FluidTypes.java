package gregtech.api.fluids.info;

import gregtech.api.unification.material.Material;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;

/**
 * Stores the default fluid types, and creates all of them.
 */
public final class FluidTypes {

    private static final Map<String, FluidType> registeredKeys = new Object2ObjectOpenHashMap<>();

    public static FluidType LIQUID = createType("liquid");
    public static FluidType GAS = createType("gas");
    public static FluidType PLASMA = createType("plasma", "plasma");

    private FluidTypes() {/**/}

    /**
     * @param name the name of the type
     * @return the type
     */
    @Nonnull
    public static FluidType createType(@Nonnull String name) {
        return createType(name, FluidType.createDefaultFunction());
    }

    /**
     * @param name   the name of the type
     * @param prefix the prefix for fluid names of this type
     * @return the type
     */
    @Nonnull
    public static FluidType createType(@Nonnull String name, @Nonnull String prefix) {
        return createType(name, FluidType.createDefaultFunction(prefix));
    }

    /**
     * @param name   the name of the type
     * @param prefix the prefix for fluid names of this type
     * @param suffix the suffix for the fluid names of this type
     * @return the type
     */
    @Nonnull
    public static FluidType createType(@Nonnull String name, @Nullable String prefix, @Nonnull String suffix) {
        return createType(name, FluidType.createDefaultFunction(prefix, suffix));
    }

    /**
     * @param name                 the name of the type
     * @param materialNameFunction function creating fluid names for materials
     * @return the type
     */
    @Nonnull
    public static FluidType createType(@Nonnull String name, @Nonnull Function<Material, String> materialNameFunction) {
        return registeredKeys.computeIfAbsent(name, n -> new FluidType(n, materialNameFunction));
    }

    /**
     * @param name the fluid type's name
     * @return the FluidType associated with the name
     */
    @Nullable
    public static FluidType getType(@Nonnull String name) {
        return registeredKeys.get(name);
    }
}
