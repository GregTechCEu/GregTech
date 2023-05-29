package gregtech.api.unification.material.registry;

import crafttweaker.annotations.ZenRegister;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTControlledRegistry;
import gregtech.api.util.GTLog;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

@ZenClass("mods.gregtech.material.MaterialRegistry")
@ZenRegister
public class MaterialRegistry extends GTControlledRegistry<String, Material> {

    private static int networkIdCounter;

    private final int networkId = networkIdCounter++;
    private final String modid;

    private boolean isRegistryClosed = false;
    private Material fallbackMaterial = null;


    public MaterialRegistry(@Nonnull String modid) {
        super(Short.MAX_VALUE);
        this.modid = modid;
    }

    public void register(Material value) {
        register(value.getId(), value.toString(), value);
    }

    @Override
    public void register(int id, @Nonnull String key, @Nonnull Material value) {
        if (isRegistryClosed) {
            GTLog.logger.error("Materials cannot be registered in the PostMaterialEvent! Must be added in the MaterialEvent. Skipping material {}...", key);
            return;
        }
        super.register(id, key, value);
    }

    public void closeRegistry() {
        this.isRegistryClosed = true;
    }

    /**
     * Set the fallback material for this registry.
     * Using {@link #getObjectById(int)} or related will still return {@code null} when an entry cannot be found.
     * This is only for manual fallback usage.
     *
     * @param material the fallback material
     */
    public void setFallbackMaterial(@Nonnull Material material) {
        this.fallbackMaterial = material;
    }

    /**
     * Using {@link #getObjectById(int)} or related will still return {@code null} when an entry cannot be found.
     * This is only for manual fallback usage.
     *
     * @return the fallback material, used for when another material does not exist
     */
    @Nonnull
    public Material getFallbackMaterial() {
        if (this.fallbackMaterial == null) {
            this.fallbackMaterial = MaterialRegistrationManager.GREGTECH_REGISTRY.getFallbackMaterial();
        }
        return this.fallbackMaterial;
    }

    /**
     * @return the network ID for this registry
     */
    public int getNetworkId() {
        return this.networkId;
    }

    @Nonnull
    public String getModid() {
        return modid;
    }

    @ZenMethod
    @Nullable
    public static Material get(@Nonnull String modid, @Nonnull String name) {
        return MaterialRegistrationManager.getRegistry(modid).getObject(name);
    }

    @ZenMethod
    public Collection<Material> getAllMaterials() {
        return Collections.unmodifiableCollection(this.registryObjects.values());
    }
}
