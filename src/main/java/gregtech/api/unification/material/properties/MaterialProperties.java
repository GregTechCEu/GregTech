package gregtech.api.unification.material.properties;

import gregtech.api.unification.material.Material;
import gregtech.api.util.GTLog;
import gregtech.common.ConfigHolder;

import java.util.*;

public class MaterialProperties {

    private static final Set<PropertyKey<?>> baseTypes = new HashSet<>(Arrays.asList(
            PropertyKey.PLASMA, PropertyKey.FLUID, PropertyKey.DUST,
            PropertyKey.INGOT, PropertyKey.GEM, PropertyKey.EMPTY
    ));

    @SuppressWarnings("unused")
    public static void addBaseType(PropertyKey<?> baseTypeKey) {
        baseTypes.add(baseTypeKey);
    }

    private final Map<PropertyKey<? extends IMaterialProperty>, IMaterialProperty> propertyMap;
    private Material material;

    public MaterialProperties() {
        propertyMap = new HashMap<>();
    }

    public boolean isEmpty() {
        return propertyMap.isEmpty();
    }

    public <T extends IMaterialProperty> T getProperty(PropertyKey<T> key) {
        return key.cast(propertyMap.get(key));
    }

    public <T extends IMaterialProperty> boolean hasProperty(PropertyKey<T> key) {
        return propertyMap.get(key) != null;
    }

    public <T extends IMaterialProperty> void setProperty(PropertyKey<T> key, IMaterialProperty value) {
        if (value == null) throw new IllegalArgumentException("Material Property must not be null!");
        if (hasProperty(key))
            throw new IllegalArgumentException("Material Property " + key.toString() + " already registered!");
        propertyMap.put(key, value);
        propertyMap.remove(PropertyKey.EMPTY);
    }

    public <T extends IMaterialProperty> void ensureSet(PropertyKey<T> key, boolean verify) {
        if (!hasProperty(key)) {
            propertyMap.put(key, key.constructDefault());
            propertyMap.remove(PropertyKey.EMPTY);
            if (verify) verify();
        }
    }

    public <T extends IMaterialProperty> void ensureSet(PropertyKey<T> key) {
        ensureSet(key, false);
    }

    public void verify() {
        List<IMaterialProperty> oldList;
        do {
            oldList = new ArrayList<>(propertyMap.values());
            oldList.forEach(p -> p.verifyProperty(this));
        } while (oldList.size() != propertyMap.size());
    }

    public void verifyLate() {
        List<IMaterialProperty> oldList;
        do {
            oldList = new ArrayList<>(propertyMap.values());
            oldList.forEach(p -> p.verifyPropertyLate(this));
        } while (oldList.size() != propertyMap.size());
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
