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

    FluidType(@Nonnull String name, Function<Material, String> materialNameFunction) {
        this.name = name;
        this.materialNameFunction = materialNameFunction;
    }

    @Nonnull
    public String getName() {
        return name;
    }

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

    @Nonnull
    public static Function<Material, String> createDefaultFunction() {
        return Material::toString;
    }

    @Nonnull
    public static Function<Material, String> createDefaultFunction(@Nonnull String prefix) {
        return material -> prefix + "." + material;
    }

    @Nonnull
    public static Function<Material, String> createDefaultFunction(@Nullable String prefix, @Nonnull String suffix) {
        return material -> {
            StringBuilder builder = new StringBuilder();

            if (prefix != null) builder.append(prefix).append(".");

            return builder.append(material).append(".").append(suffix).toString();
        };
    }
}
