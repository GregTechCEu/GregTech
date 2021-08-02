package gregtech.api.unification.material.properties;

public class PropertyKey<T extends IMaterialProperty<T>> {

    public static final PropertyKey<BlastProperty> BLAST = new PropertyKey<>("blast", BlastProperty.class);
    public static final PropertyKey<DustProperty> DUST = new PropertyKey<>("dust", DustProperty.class);
    public static final PropertyKey<FluidPipeProperty> FLUID_PIPE = new PropertyKey<>("fluid_pipe", FluidPipeProperty.class);
    public static final PropertyKey<FluidProperty> FLUID = new PropertyKey<>("fluid", FluidProperty.class);
    public static final PropertyKey<GemProperty> GEM = new PropertyKey<>("gem", GemProperty.class);
    public static final PropertyKey<IngotProperty> INGOT = new PropertyKey<>("ingot", IngotProperty.class);
    public static final PropertyKey<ItemPipeProperty> ITEM_PIPE = new PropertyKey<>("item_pipe", ItemPipeProperty.class);
    public static final PropertyKey<OreProperty> ORE = new PropertyKey<>("ore", OreProperty.class);
    public static final PropertyKey<PlasmaProperty> PLASMA = new PropertyKey<>("plasma", PlasmaProperty.class);
    public static final PropertyKey<ToolProperty> TOOL = new PropertyKey<>("tool", ToolProperty.class);
    public static final PropertyKey<WireProperty> WIRE = new PropertyKey<>("wire", WireProperty.class);

    private final String key;
    private final Class<T> type;

    public PropertyKey(String key, Class<T> type) {
        this.key = key;
        this.type = type;
    }

    protected String getKey() {
        return key;
    }

    protected T constructDefault() {
        try {
            return type.newInstance();
        } catch (Exception e) {
            return null;
        }
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
