package gregtech.api.unification.material.registry;

import com.google.common.base.Preconditions;
import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public final class MaterialRegistrationManager {

    private static final Object2ObjectMap<String, MaterialRegistry> REGISTRIES = new Object2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<MaterialRegistry> NETWORK_IDS = new Int2ObjectArrayMap<>();

    static final MaterialRegistry GREGTECH_REGISTRY = createInternalRegistry();

    @Nullable
    private static Collection<Material> registeredMaterials;

    @Nonnull
    private static Phase registrationPhase = Phase.PRE;

    private MaterialRegistrationManager() {}

    /**
     * Create a registry for a modid. Accessible when in phase {@link Phase#PRE}.
     *
     * @param modid the mod id for the registry
     * @return the registry for the mod
     */
    @Nonnull
    public static MaterialRegistry createRegistry(@Nonnull String modid) {
        if (getPhase() != Phase.PRE) {
            throw new IllegalStateException("Cannot create registries in phase " + getPhase());
        }

        Preconditions.checkArgument(!REGISTRIES.containsKey(modid),
                "Material registry already exists for modid %s", modid);
        MaterialRegistry registry = new MaterialRegistry(modid);
        REGISTRIES.put(modid, registry);
        NETWORK_IDS.put(registry.getNetworkId(), registry);
        return registry;
    }

    /**
     * Needed to prevent an {@link ExceptionInInitializerError}
     * @return the internal GT registry
     */
    @Nonnull
    private static MaterialRegistry createInternalRegistry() {
        MaterialRegistry registry = new MaterialRegistry(GTValues.MODID);
        REGISTRIES.put(GTValues.MODID, registry);
        return registry;
    }

    /**
     * Get a mod's registry. Accessible during all phases.
     *
     * @param modid the modid of the mod
     * @return the registry associated with the mod, or the GregTech registry if it does not have one
     */
    @Nonnull
    public static MaterialRegistry getRegistry(@Nonnull String modid) {
        MaterialRegistry registry = REGISTRIES.get(modid);
        return registry == null ? GREGTECH_REGISTRY : registry;
    }

    /**
     * Get a mod's registry. Accessible during all phases.
     *
     * @param networkId the network ID of the registry
     * @return the registry associated with the network ID, or the GregTech registry if it does not have one
     */
    @Nonnull
    public static MaterialRegistry getRegistry(int networkId) {
        MaterialRegistry registry = NETWORK_IDS.get(networkId);
        return registry == null ? GREGTECH_REGISTRY : registry;
    }

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
    public static Collection<MaterialRegistry> getRegistries() {
        if (getPhase() == Phase.PRE) {
            throw new IllegalStateException("Cannot get all material registries during phase " + getPhase());
        }
        return Collections.unmodifiableCollection(REGISTRIES.values());
    }

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
    public static Collection<Material> getRegisteredMaterials() {
        if (registeredMaterials == null ||
                (getPhase() != Phase.CLOSED && getPhase() != Phase.FROZEN)) {
            throw new IllegalStateException("Cannot retrieve all materials before registration");
        }
        return registeredMaterials;
    }

    /**
     * @return the current phase in the material registration process
     * @see Phase
     */
    @Nonnull
    public static Phase getPhase() {
        return registrationPhase;
    }

    /**
     * Internal use <strong>only</strong>.
     *
     * @param phase the phase to transition to
     */
    public static void transitionPhase(@Nonnull Phase phase) {
        int value = phase.ordinal() - getPhase().ordinal();
        if (value == 1) {
            registrationPhase = phase;
            switch (registrationPhase) {
                case OPEN -> unfreezeRegistries();
                case CLOSED -> closeRegistries();
                case FROZEN -> freezeRegistries();
                case PRE -> throw new IllegalArgumentException("Impossible state reached");
            }
        } else if (value > 1) {
            throw new IllegalArgumentException("Cannot skip phases");
        } else if (value == 0) {
            throw new IllegalArgumentException("MaterialRegistrationManager is already in phase " + phase);
        } else {
            throw new IllegalArgumentException("Cannot transition to already completed phase " + phase);
        }
    }

    private static void unfreezeRegistries() {
        REGISTRIES.values().forEach(MaterialRegistry::unfreeze);
    }

    private static void closeRegistries() {
        REGISTRIES.values().forEach(MaterialRegistry::closeRegistry);
        Collection<Material> collection = new ArrayList<>();
        for (MaterialRegistry registry : REGISTRIES.values()) {
            collection.addAll(registry.getAllMaterials());
        }
        registeredMaterials = Collections.unmodifiableCollection(collection);
    }

    public static boolean canModifyMaterials() {
        return getPhase() != Phase.FROZEN && getPhase() != Phase.PRE;
    }

    private static void freezeRegistries() {
        REGISTRIES.values().forEach(MaterialRegistry::freeze);
    }

    public enum Phase {
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
