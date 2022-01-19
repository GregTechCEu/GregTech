package gregtech.api.unification.material.properties;

import gregtech.api.unification.material.Material;

import java.util.*;

public class MaterialProperties {

    private static final Set<PropertyKey<?>> baseTypes = new HashSet<>(Arrays.asList(
            PropertyKey.PLASMA, PropertyKey.FLUID, PropertyKey.DUST,
            PropertyKey.INGOT, PropertyKey.GEM
    ));

    @SuppressWarnings("unused")
    public static void addBaseType(PropertyKey<?> baseTypeKey) {
        baseTypes.add(baseTypeKey);
    }

    private final Map<PropertyKey<? extends IMaterialProperty<?>>, IMaterialProperty<?>> propertyMap;
    private Material material;

    public MaterialProperties() {
        propertyMap = new HashMap<>();
    }

    public <T extends IMaterialProperty<T>> T getProperty(PropertyKey<T> key) {
        return key.cast(propertyMap.get(key));
    }

    public <T extends IMaterialProperty<T>> boolean hasProperty(PropertyKey<T> key) {
        return propertyMap.get(key) != null;
    }

    public <T extends IMaterialProperty<T>> void setProperty(PropertyKey<T> key, IMaterialProperty<T> value) {
        if (value == null) throw new IllegalArgumentException("Material Property must not be null!");
        if (hasProperty(key))
            throw new IllegalArgumentException("Material Property " + key.toString() + " already registered!");
        propertyMap.put(key, value);
    }

    public <T extends IMaterialProperty<T>> void ensureSet(PropertyKey<T> key, boolean verify) {
        if (!hasProperty(key)) {
            propertyMap.put(key, key.constructDefault());
            if (verify) verify();
        }
    }

    public <T extends IMaterialProperty<T>> void ensureSet(PropertyKey<T> key) {
        ensureSet(key, false);
    }

    public void verify() {
        List<IMaterialProperty<?>> oldList;
        do {
            oldList = new ArrayList<>(propertyMap.values());
            oldList.forEach(p -> p.verifyProperty(this));
        } while (oldList.size() != propertyMap.size());

        //if (propertyMap.keySet().stream().noneMatch(baseTypes::contains)) {
        //    throw new IllegalArgumentException("Material must have at least one of: " + baseTypes + " specified!");
        //}
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        propertyMap.forEach((k, v) -> sb.append(k.toString()).append("\n"));
        return sb.toString();
    }
}
