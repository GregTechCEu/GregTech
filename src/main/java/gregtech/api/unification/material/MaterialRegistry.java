package gregtech.api.unification.material;

import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.util.GTControlledRegistry;
import gregtech.api.util.GTLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MaterialRegistry {

    private MaterialRegistry() {
    }

    private static final GTControlledRegistry<String, Material> MATERIAL_REGISTRY = new GTControlledRegistry<>(Short.MAX_VALUE);
    private static final GTControlledRegistry<String, Material> FLUID_REGISTRY = new GTControlledRegistry<>(Integer.MAX_VALUE);

    private static List<Material> DEFERRED_REGISTRY = new ArrayList<>();

    public static void freeze() {
        GTLog.logger.info("Freezing material registry...");
        DEFERRED_REGISTRY.forEach(MaterialRegistry::finalizeRegistry);
        DEFERRED_REGISTRY = null; // destroy the deferred registry
        MATERIAL_REGISTRY.freezeRegistry();
        FLUID_REGISTRY.freezeRegistry();
    }

    public static boolean isFrozen() {
        return MATERIAL_REGISTRY.isFrozen();
    }

    private static final AtomicInteger fluidCounter = new AtomicInteger(0);

    private static void finalizeRegistry(Material material) {
        if (material.getProperties().allMatch(p -> p instanceof FluidProperty))
            FLUID_REGISTRY.register(material.id, material.name, material);
        else MATERIAL_REGISTRY.register(fluidCounter.getAndIncrement(), material.name, material);
    }

    public static void register(Material material) {
        DEFERRED_REGISTRY.add(material);
    }
}
