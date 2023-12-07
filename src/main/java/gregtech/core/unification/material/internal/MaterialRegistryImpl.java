package gregtech.core.unification.material.internal;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.core.CoreModule;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class MaterialRegistryImpl extends MaterialRegistry {

    private static int networkIdCounter;

    private final int networkId = networkIdCounter++;
    private final String modid;

    private boolean isRegistryClosed = false;
    private Material fallbackMaterial = null;

    protected MaterialRegistryImpl(@NotNull String modid) {
        super();
        this.modid = modid;
    }

    @Override
    public void register(Material material) {
        this.register(material.getId(), material.toString(), material);
    }

    @Override
    public void register(int id, @NotNull String key, @NotNull Material value) {
        if (isRegistryClosed) {
            CoreModule.logger.error(
                    "Materials cannot be registered in the PostMaterialEvent (or after)! Must be added in the MaterialEvent. Skipping material {}...",
                    key);
            return;
        }
        super.register(id, key, value);
    }

    @NotNull
    @Override
    public Collection<Material> getAllMaterials() {
        return Collections.unmodifiableCollection(this.registryObjects.values());
    }

    @Override
    public void setFallbackMaterial(@NotNull Material material) {
        this.fallbackMaterial = material;
    }

    @NotNull
    @Override
    public Material getFallbackMaterial() {
        if (this.fallbackMaterial == null) {
            this.fallbackMaterial = MaterialRegistryManager.getInstance().getDefaultFallback();
        }
        return this.fallbackMaterial;
    }

    @Override
    public int getNetworkId() {
        return this.networkId;
    }

    @NotNull
    @Override
    public String getModid() {
        return this.modid;
    }

    public void closeRegistry() {
        this.isRegistryClosed = true;
    }
}
