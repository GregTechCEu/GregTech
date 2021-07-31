package gregtech.api.unification.material;

import gregtech.api.util.GTControlledRegistry;
import gregtech.api.util.GTLog;

import java.util.ArrayList;
import java.util.List;

public class MaterialRegistry {

    private MaterialRegistry() {
    }

    private static final GTControlledRegistry<String, Material> MATERIAL_REGISTRY = new GTControlledRegistry<>(Short.MAX_VALUE);

    private static List<Material> DEFERRED_REGISTRY = new ArrayList<>();

    public static void freeze() {
        GTLog.logger.info("Freezing material registry...");
        DEFERRED_REGISTRY.forEach(MaterialRegistry::finalizeRegistry);
        DEFERRED_REGISTRY = null; // destroy the deferred registry
        MATERIAL_REGISTRY.freezeRegistry();
    }

    public static boolean isFrozen() {
        return MATERIAL_REGISTRY.isFrozen();
    }

    private static void finalizeRegistry(Material material) {
        material.verifyMaterial();
        MATERIAL_REGISTRY.register(material.id, material.name, material);
    }

    public static void register(Material material) {
        DEFERRED_REGISTRY.add(material);
    }
}
