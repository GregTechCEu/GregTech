package gregtech.api.util.virtualregistry;

import gregtech.api.GTValues;
import gregtech.api.util.GTLog;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

@SuppressWarnings("SameParameterValue")
public class VirtualEnderRegistry extends WorldSavedData {

    private static final String DATA_ID = GTValues.MODID + ".virtual_entry_data";
    private static final String OLD_DATA_ID = GTValues.MODID + ".vtank_data";
    private static final String PUBLIC_KEY = "Public";
    private static final String PRIVATE_KEY = "Private";
    private static final Map<UUID, VirtualRegistryMap> VIRTUAL_REGISTRIES = new HashMap<>();

    public VirtualEnderRegistry(String name) {
        super(name);
    }

    public static <T extends VirtualEntry> T getEntry(@Nullable UUID owner, EntryTypes<T> type, String name) {
        return getRegistry(owner).getEntry(type, name);
    }

    public static void addEntry(@Nullable UUID owner, String name, VirtualEntry entry) {
        getRegistry(owner).addEntry(name, entry);
    }

    public static boolean hasEntry(@Nullable UUID owner, EntryTypes<?> type, String name) {
        return getRegistry(owner).contains(type, name);
    }

    public static @NotNull <T extends VirtualEntry> T getOrCreateEntry(@Nullable UUID owner, EntryTypes<T> type,
                                                                       String name) {
        if (!hasEntry(owner, type, name))
            addEntry(owner, name, type.createInstance());

        return getEntry(owner, type, name);
    }

    /**
     * Removes an entry from the registry. Use with caution!
     *
     * @param owner The uuid of the player the entry is private to, or null if the entry is public
     * @param type  Type of the registry to remove from
     * @param name  The name of the entry
     */
    public static void deleteEntry(@Nullable UUID owner, EntryTypes<?> type, String name) {
        var registry = getRegistry(owner);
        if (registry.contains(type, name)) {
            registry.deleteEntry(type, name);
            return;
        }
        GTLog.logger.warn("Attempted to delete {} entry {} of type {}, which does not exist",
                owner == null ? "public" : String.format("private [%s]", owner), name, type);
    }

    public static <T extends VirtualEntry> void deleteEntry(@Nullable UUID owner, EntryTypes<T> type, String name,
                                                            Predicate<T> shouldDelete) {
        T entry = getEntry(owner, type, name);
        if (entry != null && shouldDelete.test(entry))
            deleteEntry(owner, type, name);
    }

    public static Set<String> getEntryNames(UUID owner, EntryTypes<?> type) {
        return getRegistry(owner).getEntryNames(type);
    }

    /**
     * To be called on server stopped event
     */
    public static void clearMaps() {
        VIRTUAL_REGISTRIES.clear();
    }

    private static VirtualRegistryMap getRegistry(UUID owner) {
        return VIRTUAL_REGISTRIES.computeIfAbsent(owner, key -> new VirtualRegistryMap());
    }

    @Override
    public final void readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey(PUBLIC_KEY)) {
            VIRTUAL_REGISTRIES.put(null, new VirtualRegistryMap(nbt.getCompoundTag(PUBLIC_KEY)));
        }
        if (nbt.hasKey(PRIVATE_KEY)) {
            NBTTagCompound privateEntries = nbt.getCompoundTag(PRIVATE_KEY);
            for (String owner : privateEntries.getKeySet()) {
                var privateMap = privateEntries.getCompoundTag(owner);
                VIRTUAL_REGISTRIES.put(UUID.fromString(owner), new VirtualRegistryMap(privateMap));
            }
        }
    }

    @NotNull
    @Override
    public final NBTTagCompound writeToNBT(@NotNull NBTTagCompound tag) {
        var privateTag = new NBTTagCompound();
        for (var owner : VIRTUAL_REGISTRIES.keySet()) {
            var mapTag = VIRTUAL_REGISTRIES.get(owner).serializeNBT();
            if (owner != null) {
                privateTag.setTag(owner.toString(), mapTag);
            } else {
                tag.setTag(PUBLIC_KEY, mapTag);
            }
        }
        tag.setTag(PRIVATE_KEY, privateTag);
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
    @SuppressWarnings("DataFlowIssue")
    public static void initializeStorage(World world) {
        MapStorage storage = world.getMapStorage();

        VirtualEnderRegistry instance = (VirtualEnderRegistry) storage.getOrLoadData(VirtualEnderRegistry.class,
                DATA_ID);
        VirtualEnderRegistry old = (VirtualEnderRegistry) storage.getOrLoadData(VirtualEnderRegistry.class,
                OLD_DATA_ID);

        if (instance == null) {
            instance = new VirtualEnderRegistry(DATA_ID);
            storage.setData(DATA_ID, instance);
        }

        if (old != null) {
            instance.readFromNBT(old.serializeNBT());
            var file = world.getSaveHandler().getMapFileFromName(OLD_DATA_ID);
            var split = file.getName().split("\\.");
            var stringBuilder = new StringBuilder(split[0])
                    .append('.')
                    .append(split[1])
                    .append(".backup")
                    .append('.')
                    .append(split[2]);
            if (file.renameTo(new File(file.getParent(), stringBuilder.toString()))) {
                file.deleteOnExit();
                GTLog.logger.warn("Moved Virtual Tank Data to new format, created backup!");
            }
        }
    }
}
