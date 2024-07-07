package gregtech.api.metatileentity.registry;

import gregtech.api.GTValues;

import net.minecraftforge.fml.common.eventhandler.Event;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Map;

public final class MTEManager {

    private static MTEManager instance;

    private static int networkId;
    private static MTERegistry internalRegistry;

    private final Map<String, MTERegistry> registryMap = new Object2ObjectOpenHashMap<>();
    private final Int2ObjectMap<MTERegistry> networkMap = new Int2ObjectOpenHashMap<>();

    /**
     * @return the global MTE Manager instance
     */
    @ApiStatus.Internal
    public static @NotNull MTEManager getInstance() {
        if (instance == null) {
            instance = new MTEManager();
            internalRegistry = instance.createRegistry(GTValues.MODID);
        }
        return instance;
    }

    private MTEManager() {}

    /**
     * @param modid the modid of the registry
     * @return the registry associated with the modid, otherwise the default registry
     */
    public @NotNull MTERegistry getRegistry(@NotNull String modid) {
        MTERegistry registry = registryMap.get(modid);
        if (registry == null) {
            throw new IllegalArgumentException("No MTE registry exists for modid " + modid);
        }
        return registry;
    }

    /**
     * Create an MTE Registry
     *
     * @param modid the modid for the registry
     * @return the created registry
     */
    public @NotNull MTERegistry createRegistry(@NotNull String modid) {
        if (registryMap.containsKey(modid)) {
            throw new IllegalArgumentException("MTE Registry for modid " + modid + " is already registered");
        }
        MTERegistry registry = new MTERegistry(modid, ++networkId);
        registryMap.put(modid, registry);
        networkMap.put(networkId, registry);
        return registry;
    }

    /**
     * @param networkId the network id of the registry
     * @return the registry associated with the network id, otherwise the default registry
     */
    public @NotNull MTERegistry getRegistry(int networkId) {
        MTERegistry registry = networkMap.get(networkId);
        return registry == null ? internalRegistry : registry;
    }

    /**
     * @return all the available MTE registries
     */
    public @NotNull @UnmodifiableView Collection<@NotNull MTERegistry> getRegistries() {
        return registryMap.values();
    }

    /**
     * Event during which MTE Registries should be added by mods.
     * <p>
     * Use {@link #createRegistry(String)} to create a new MTE registry.
     */
    public static class MTERegistryEvent extends Event {}
}
