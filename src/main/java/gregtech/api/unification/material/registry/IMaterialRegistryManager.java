package gregtech.api.unification.material.registry;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;

import javax.annotation.Nonnull;
import java.util.Collection;

public interface IMaterialRegistryManager {

    /**
     * Create a registry for a modid. Accessible when in phase {@link Phase#PRE}.
     *
     * @param modid the mod id for the registry
     * @return the registry for the mod
     */
    @Nonnull
    MaterialRegistry createRegistry(@Nonnull String modid);

    /**
     * Get a mod's registry. Accessible during all phases.
     *
     * @param modid the modid of the mod
     * @return the registry associated with the mod, or the GregTech registry if it does not have one
     */
    @Nonnull
    MaterialRegistry getRegistry(@Nonnull String modid);

    /**
     * Get a mod's registry. Accessible during all phases.
     *
     * @param networkId the network ID of the registry
     * @return the registry associated with the network ID, or the GregTech registry if it does not have one
     */
    @Nonnull
    MaterialRegistry getRegistry(int networkId);

    /**
     * Accessible when in phases:
     * <ul>
     * <li>{@link Phase#OPEN}</li>
     * <li>{@link Phase#CLOSED}</li>
     * <li>{@link Phase#FROZEN}</li>
     * </ul>
     *
     * @return all the Material Registries
     */
    @Nonnull
    Collection<MaterialRegistry> getRegistries();

    /**
     * Accessible when in phases:
     * <ul>
     * <li>{@link Phase#CLOSED}</li>
     * <li>{@link Phase#FROZEN}</li>
     * </ul>
     *
     * @return all registered materials.
     */
    @Nonnull
    Collection<Material> getRegisteredMaterials();

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
    Material getMaterial(String name);

    /**
     * @return the current phase in the material registration process
     * @see Phase
     */
    @Nonnull
    Phase getPhase();

    default boolean canModifyMaterials() {
        return this.getPhase() != Phase.FROZEN && this.getPhase() != Phase.PRE;
    }

    enum Phase {
        /** Material Registration and Modification is not started */
        PRE,
        /** Material Registration and Modification is available */
        OPEN,
        /** Material Registration is unavailable and only Modification is available */
        CLOSED,
        /** Material Registration and Modification is unavailable */
        FROZEN
    }
}
