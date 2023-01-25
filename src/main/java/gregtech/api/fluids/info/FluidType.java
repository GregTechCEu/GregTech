package gregtech.api.fluids.info;

import gregtech.api.unification.material.Material;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * A key for storing fluids of various types.
 * Create them with {@link FluidTypes#createType}
 */
public final class FluidType {

    private final String name;
    private final Function<Material, String> materialNameFunction;

    /**
     * @param name                 the name of the fluid type
     * @param materialNameFunction a function creating a fluid name for a material
     */
    FluidType(@Nonnull String name, Function<Material, String> materialNameFunction) {
        this.name = name;
        this.materialNameFunction = materialNameFunction;
    }

    /**
     * @return the default naming function
     */
    @Nonnull
    public static Function<Material, String> createDefaultFunction() {
        return Material::toString;
    }

    /**
     * @return the default naming function, with a prefix
     */
    @Nonnull
    public static Function<Material, String> createDefaultFunction(@Nonnull String prefix) {
        return material -> prefix + "." + material;
    }

    /**
     * @return the default naming function, with an optional prefix, and a suffix
     */
    @Nonnull
    public static Function<Material, String> createDefaultFunction(@Nullable String prefix, @Nonnull String suffix) {
        return material -> {
            StringBuilder builder = new StringBuilder();

            if (prefix != null) builder.append(prefix).append(".");

            return builder.append(material).append(".").append(suffix).toString();
        };
    }

    /**
     * @return the name of this type
     */
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * @param material the material whose name should be created
     * @return the fluid name for the material and this type
     */
    @Nonnull
    public String getFluidNameForMaterial(@Nonnull Material material) {
        return this.materialNameFunction.apply(material);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FluidType that = (FluidType) o;
        return name.equals(that.getName());
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Nonnull
    @Override
    public String toString() {
        return "FluidType{" +
                "name='" + name + '\'' +
                '}';
    }
}
