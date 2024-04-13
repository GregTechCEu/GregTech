package gregtech.api.metatileentity.registry;

import gregtech.api.GTValues;

import net.minecraftforge.fml.common.eventhandler.Event;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ShortMap;
import it.unimi.dsi.fastutil.shorts.Short2ShortOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Map;

public final class MTEManager {

    private static MTEManager instance;

    private static int networkId;
    private static MTERegistry internalRegistry;

    private final Map<String, MTERegistry> map = new Object2ObjectOpenHashMap<>();
    private final Int2ObjectMap<MTERegistry> networkMap = new Int2ObjectOpenHashMap<>();
    private final Short2ObjectMap<String> dataFixNameMap = new Short2ObjectOpenHashMap<>();
    private final Short2ShortMap dataFixMetaMap = new Short2ShortOpenHashMap();

    /**
     * @return the global MTE Manager instance
     */
    public static @NotNull MTEManager getInstance() {
        if (instance == null) {
            instance = new MTEManager();
            internalRegistry = instance.createRegistry(GTValues.MODID);
        }
        return instance;
    }

    private MTEManager() {}

    /**
     * Create an MTE Registry
     *
     * @param modid the modid for the registry
     * @return the created registry
     */
    public @NotNull MTERegistry createRegistry(@NotNull String modid) {
        if (map.containsKey(modid)) {
            throw new IllegalArgumentException("MTE Registry for modid " + modid + " is already registered");
        }
        MTERegistry registry = new MTERegistry(modid, ++networkId);
        map.put(modid, registry);
        networkMap.put(networkId, registry);
        return registry;
    }

    /**
     * @param modid the modid of the registry
     * @return the registry associated with the modid, otherwise the default registry
     */
    public @NotNull MTERegistry getRegistry(@NotNull String modid) {
        MTERegistry registry = map.get(modid);
        return registry == null ? internalRegistry : registry;
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
        return map.values();
    }

    public void registerDataFix(@NotNull String postModid, int preMeta, int postMeta) {
        dataFixNameMap.put((short) preMeta, postModid);
        dataFixMetaMap.put((short) preMeta, (short) postMeta);
    }

    /**
     * @param originalMeta the original meta saved
     * @return the new meta to use instead if present, otherwise the original metadata
     */
    public short getFixedMeta(short originalMeta) {
        if (dataFixMetaMap.containsKey(originalMeta)) {
            return dataFixMetaMap.get(originalMeta);
        }
        return originalMeta;
    }

    /**
     * @param meta the original meta saved
     * @return the new modid to use instead if present, otherwise null
     */
    public @Nullable String getFixedModid(short meta) {
        return dataFixNameMap.get(meta);
    }

    /**
     * @param meta the original meta saved
     * @return if an MTE Data Fix is needed
     */
    public boolean needsDataFix(short meta) {
        return dataFixMetaMap.containsKey(meta);
    }

    /**
     * Event during which MTE Registries should be added by mods.
     * <p>
     * Use {@link #createRegistry(String)} to create a new MTE registry.
     */
    public static class MTERegistryEvent extends Event {}
}
