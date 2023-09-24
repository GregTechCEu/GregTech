package gregtech.api.unification.material.registry;

import gregtech.api.unification.material.Material;
import gregtech.api.util.GTControlledRegistry;

import javax.annotation.Nonnull;
import java.util.Collection;

public abstract class MaterialRegistry extends GTControlledRegistry<String, Material> {

    public MaterialRegistry() {
        super(Short.MAX_VALUE);
    }

    public abstract void register(Material material);

    @Nonnull
    public abstract Collection<Material> getAllMaterials();

    /**
     * Set the fallback material for this registry.
     * Using {@link #getObjectById(int)} or related will still return {@code null} when an entry cannot be found.
     * This is only for manual fallback usage.
     *
     * @param material the fallback material
     */
    public abstract void setFallbackMaterial(@Nonnull Material material);

    /**
     * Using {@link #getObjectById(int)} or related will still return {@code null} when an entry cannot be found.
     * This is only for manual fallback usage.
     *
     * @return the fallback material, used for when another material does not exist
     */
    @Nonnull
    public abstract Material getFallbackMaterial();

    /**
     * @return the network ID for this registry
     */
    public abstract int getNetworkId();

    @Nonnull
    public abstract String getModid();
}
