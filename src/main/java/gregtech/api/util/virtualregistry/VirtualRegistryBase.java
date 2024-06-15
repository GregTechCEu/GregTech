package gregtech.api.util.virtualregistry;

import gregtech.api.GTValues;
import gregtech.api.util.GTLog;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@SuppressWarnings("SameParameterValue")
public class VirtualRegistryBase extends WorldSavedData {

    private static final String DATA_ID = GTValues.MODID + ".virtual_entry_data";
    private static final String PUBLIC_KEY = "Public";
    private static final String PRIVATE_KEY = "Private";
    private static final Map<UUID, VirtualRegistryMap> PRIVATE_REGISTRIES = new HashMap<>();
    private static final VirtualRegistryMap PUBLIC_REGISTRY = new VirtualRegistryMap();

    private static final Map<EntryType, Supplier<? extends VirtualEntry>> factoryRegistry = new EnumMap<>(EntryType.class);

    public VirtualRegistryBase(String name) {
        super(name);
    }

    protected static VirtualEntry getEntry(@Nullable UUID owner, EntryType type, String name) {
        if (owner == null)
            return PUBLIC_REGISTRY.getEntry(type, name);

        return PRIVATE_REGISTRIES.get(owner).getEntry(type, name);
    }

    protected static void addEntry(@Nullable UUID owner, VirtualEntry entry) {
        if (owner == null)
            PUBLIC_REGISTRY.addEntry(entry);

        PRIVATE_REGISTRIES.computeIfAbsent(owner, key -> new VirtualRegistryMap())
                .addEntry(entry);
    }

    protected static boolean hasEntry(@Nullable UUID owner, EntryType type, String name) {
        return owner == null ? PUBLIC_REGISTRY.contains(type, name) :
                PRIVATE_REGISTRIES.containsKey(owner) && PRIVATE_REGISTRIES.get(owner).contains(type, name);
    }

    /**
     * Removes an entry from the registry. Use with caution!
     *
     * @param owner        The uuid of the player the entry is private to, or null if the entry is public
     * @param type         Type of the registry to remove from
     * @param name         The name of the entry
     */
    protected static void deleteEntry(@Nullable UUID owner, EntryType type, String name) {
        if (owner == null && PUBLIC_REGISTRY.contains(type, name)) {
            PUBLIC_REGISTRY.deleteEntry(type, name);
        } else if (owner != null && PRIVATE_REGISTRIES.containsKey(owner)) {
            PRIVATE_REGISTRIES.get(owner).deleteEntry(type, name);
        } else {
            GTLog.logger.warn("Attempted to delete {} entry {} of type {}, which does not exist",
                    owner == null ? "public" : String.format("private [%s]", owner), name, type);
        }
    }

    /**
     * To be called on server stopped event
     */
    public static void clearMaps() {
        PRIVATE_REGISTRIES.clear();
        PUBLIC_REGISTRY.clear();
    }

    protected static void registerFactory(EntryType type, Supplier<? extends VirtualEntry> factory) {
        if (!factoryRegistry.containsKey(type))
            factoryRegistry.put(type, factory);
    }

    public static VirtualEntry createEntry(EntryType type) {
        if (factoryRegistry.containsKey(type))
            return factoryRegistry.get(type).get();

        return null;
    }

    @Override
    public final void readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey(PUBLIC_KEY)) {
            NBTTagCompound publicEntries = nbt.getCompoundTag(PUBLIC_KEY);
            PUBLIC_REGISTRY.deserializeNBT(publicEntries);
        }
        if (nbt.hasKey(PRIVATE_KEY)) {
            NBTTagCompound privateEntries = nbt.getCompoundTag(PRIVATE_KEY);
            for (String owner : privateEntries.getKeySet()) {
                var privateMap = privateEntries.getCompoundTag(owner);
                PRIVATE_REGISTRIES.put(UUID.fromString(owner), new VirtualRegistryMap(privateMap));
            }
        }
    }

    @NotNull
    @Override
    public final NBTTagCompound writeToNBT(@NotNull NBTTagCompound tag) {
        var privateTag = new NBTTagCompound();
        for (var owner : PRIVATE_REGISTRIES.keySet()) {
            privateTag.setTag(owner.toString(), PRIVATE_REGISTRIES.get(owner).serializeNBT());
        }
        tag.setTag(PRIVATE_KEY, privateTag);
        tag.setTag(PUBLIC_KEY, PUBLIC_REGISTRY.serializeNBT());
        return tag;
    }

    @Override
    public boolean isDirty() {
        // can't think of a good way to mark dirty other than always
        return true;
    }

    /**
     * To be called on world load event
     */
    public static void initializeStorage(World world) {
        MapStorage storage = world.getMapStorage();
        VirtualRegistryBase instance = (VirtualRegistryBase) storage.getOrLoadData(VirtualRegistryBase.class, DATA_ID);

        if (instance == null) {
            instance = new VirtualRegistryBase(DATA_ID);
            storage.setData(DATA_ID, instance);
        }
    }
}
