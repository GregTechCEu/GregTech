package gregtech.api.unification.material.properties;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PropertyKey<T extends IMaterialProperty<T>> {

    public static final PropertyKey<BlastProperty> BLAST = new PropertyKey<>("blast", BlastProperty.class);
    public static final PropertyKey<DustProperty> DUST = new PropertyKey<>("dust", DustProperty.class);
    public static final PropertyKey<FluidPipeProperties> FLUID_PIPE = new PropertyKey<>("fluid_pipe", FluidPipeProperties.class, ItemPipeProperties.class);
    public static final PropertyKey<FluidProperty> FLUID = new PropertyKey<>("fluid", FluidProperty.class);
    public static final PropertyKey<GemProperty> GEM = new PropertyKey<>("gem", GemProperty.class, IngotProperty.class);
    public static final PropertyKey<IngotProperty> INGOT = new PropertyKey<>("ingot", IngotProperty.class, GemProperty.class);
    public static final PropertyKey<ItemPipeProperties> ITEM_PIPE = new PropertyKey<>("item_pipe", ItemPipeProperties.class, FluidPipeProperties.class);
    public static final PropertyKey<OreProperty> ORE = new PropertyKey<>("ore", OreProperty.class);
    public static final PropertyKey<PlasmaProperty> PLASMA = new PropertyKey<>("plasma", PlasmaProperty.class);
    public static final PropertyKey<ToolProperty> TOOL = new PropertyKey<>("tool", ToolProperty.class);
    public static final PropertyKey<WireProperties> WIRE = new PropertyKey<>("wire", WireProperties.class);

    private final List<Class<? extends IMaterialProperty<?>>> incompatibleTypes = new ArrayList<>();
    private final String key;
    private final Class<T> type;

    @SafeVarargs
    public PropertyKey(String key, Class<T> type, Class<? extends IMaterialProperty<?>>... incompatibleTypes) {
        this.key = key;
        this.type = type;
        this.incompatibleTypes.addAll(Arrays.asList(incompatibleTypes));
    }

    protected String getKey() {
        return key;
    }

    public T constructDefault() {
        try {
            return type.newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isIncompatible(@Nonnull PropertyKey<?> otherKey) {
        return this.incompatibleTypes.contains(otherKey.type) || otherKey.incompatibleTypes.contains(this.type);
    }

    public T cast(IMaterialProperty<?> property) {
        return this.type.cast(property);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PropertyKey) {
            return ((PropertyKey<?>) o).getKey().equals(key);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return key;
    }
}
