package gregtech.api.fluids.info;

import gregtech.api.unification.material.Material;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A key for storing fluids of various types.
 * Create them with {@link FluidTypeKeys#createKey}
 */
public final class FluidTypeKey {

    private final String name;
    private final String prefix;
    private final String suffix;

    FluidTypeKey(@Nonnull String name, @Nullable String prefix, @Nullable String suffix) {
        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getFluidNameForMaterial(@Nonnull Material material) {
        StringBuilder builder = new StringBuilder();

        if (this.prefix != null) {
            builder.append(this.prefix).append(".");
        }

        builder.append(material);

        if (this.suffix != null) {
            builder.append(".").append(this.suffix);
        }

        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FluidTypeKey that = (FluidTypeKey) o;
        return name.equals(that.getName());
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Nonnull
    @Override
    public String toString() {
        return "FluidTypeKey{" +
                "name='" + name + '\'' +
                '}';
    }
}
