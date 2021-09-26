package gregtech.api.unification.material;

import com.google.common.collect.Lists;
import crafttweaker.annotations.ZenRegister;
import gregtech.api.util.GTControlledRegistry;
import gregtech.api.util.GTLog;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@ZenClass("mods.gregtech.material.MaterialRegistry")
@ZenRegister
public class MaterialRegistry {

    private MaterialRegistry() {
    }

    public static final GTControlledRegistry<String, Material> MATERIAL_REGISTRY = new GTControlledRegistry<>(Short.MAX_VALUE);

    private static List<Material> DEFERRED_REGISTRY = new ArrayList<>();

    public static void finalizeMaterials(boolean shouldFreeze) {
        if (shouldFreeze) GTLog.logger.info("Freezing material registry...");
        DEFERRED_REGISTRY.forEach(MaterialRegistry::finalizeRegistry);
        DEFERRED_REGISTRY.forEach(MaterialRegistry::postVerify);
        DEFERRED_REGISTRY = shouldFreeze ? null : new ArrayList<>(); // destroy the deferred registry
        if (shouldFreeze) MATERIAL_REGISTRY.freeze();
    }

    public static boolean isFrozen() {
        return MATERIAL_REGISTRY.isFrozen();
    }

    private static void finalizeRegistry(Material material) {
        material.verifyMaterial();
    }

    private static void postVerify(Material material) {
        material.postVerify();
        MATERIAL_REGISTRY.register(material.getId(), material.toString(), material);
    }

    public static void register(Material material) {
        DEFERRED_REGISTRY.add(material);
    }

    @ZenMethod
    @Nullable
    public static Material get(String name) {
        return MATERIAL_REGISTRY.getObject(name);
    }

    @ZenMethod
    public static List<Material> getAllMaterials() {
        return Lists.newArrayList(MATERIAL_REGISTRY);
    }
}
