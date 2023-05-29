package gregtech.api.unification.material;

import gregtech.api.GTValues;
import gregtech.api.unification.material.registry.MaterialRegistryManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class MaterialHelpers {

    private MaterialHelpers() {}

    /**
     * Get a material from a String in formats:
     * <ul>
     * <li>{@code "modid:registry_name"}</li>
     * <li>{@code "registry_name"} - where modid is inferred to be {@link GTValues#MODID}</li>
     * </ul>
     *
     * @param name the name of the material in the above format
     * @return the material associated with the name
     */
    @Nullable
    public static Material getMaterial(@Nonnull String name) {
        if (!name.isEmpty()) {
            String modid;
            String materialName;
            int index = name.indexOf(':');
            if (index >= 0) {
                modid = name.substring(0, index);
                materialName = name.substring(index + 1);
            } else {
                modid = GTValues.MODID;
                materialName = name;
            }
            return MaterialRegistryManager.getRegistry(modid).getObject(materialName);
        }
        return null;
    }
}
