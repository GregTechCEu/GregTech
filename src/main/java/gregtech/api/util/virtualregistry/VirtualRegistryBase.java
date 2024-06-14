package gregtech.api.util.virtualregistry;

import gregtech.api.GTValues;
import gregtech.api.util.GTLog;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VirtualRegistryBase extends WorldSavedData {

    private static final String DATA_ID = GTValues.MODID + ".virtual_entry_data";
    private static final String PUBLIC_KEY = "Public";
    private static final String PRIVATE_KEY = "Private";
    private static final Map<UUID, RegistryMap> PRIVATE_REGISTRIES = new HashMap<>();
    private static final RegistryMap PUBLIC_REGISTRY = new RegistryMap();

    public VirtualRegistryBase(String name) {
        super(name);
    }

    public static VirtualEntry getEntry(@Nullable UUID owner, EntryType type, String name) {
        if (owner == null)
            return PUBLIC_REGISTRY.getEntry(type, name);

        return PRIVATE_REGISTRIES.get(owner).getEntry(type, name);
    }

    public static void addEntry(@Nullable UUID owner, EntryType type, String name) {
        if (owner == null)
            PUBLIC_REGISTRY.addEntry(type, name);

        PRIVATE_REGISTRIES.computeIfAbsent(owner, key -> new RegistryMap())
                .addEntry(type, name);
    }

    /**
     * Retrieves a tank from the registry, creating it if it does not exist
     *
     * @param owner     The uuid of the player the tank is private to, or null if the tank is public
     * @param type      The type of the entry
     * @param name      The name of the entry
     * @return The tank object
     */
    public static VirtualEntry getOrCreate(UUID owner, EntryType type, String name) {
        if (!PRIVATE_REGISTRIES.containsKey(owner) && !PRIVATE_REGISTRIES.get(owner).contains(type, name)) {
            addEntry(owner, type, name);
        }
        return getEntry(owner, type, name);
    }

    /**
     * Removes an entry from the registry. Use with caution!
     *
     * @param owner        The uuid of the player the entry is private to, or null if the entry is public
     * @param type         Type of the registry to remove from
     * @param name         The name of the entry
     */
    public static void deleteEntry(@Nullable UUID owner, EntryType type, String name) {
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

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey(PUBLIC_KEY)) {
            NBTTagCompound publicEntries = nbt.getCompoundTag(PUBLIC_KEY);
            PUBLIC_REGISTRY.deserializeNBT(publicEntries);
        }
        if (nbt.hasKey(PRIVATE_KEY)) {
            NBTTagCompound privateEntries = nbt.getCompoundTag(PRIVATE_KEY);
            for (String owner : privateEntries.getKeySet()) {
                var privateMap = privateEntries.getCompoundTag(owner);
                PRIVATE_REGISTRIES.put(UUID.fromString(owner), new RegistryMap(privateMap));
            }
        }
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound tag) {
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

    private static class RegistryMap implements INBTSerializable<NBTTagCompound> {

        private static final Map<EntryType, Map<String, VirtualEntry>> registryMap = new EnumMap<>(EntryType.class);

        public RegistryMap(NBTTagCompound tag) {
            deserializeNBT(tag);
        }

        public RegistryMap() {}

        public VirtualEntry getEntry(EntryType type, String name) {
            return registryMap.get(type).get(name);
        }

        public void addEntry(EntryType type, String name) {
            registryMap.computeIfAbsent(type, k -> new HashMap<>())
                    .put(name, VirtualEntry.of(type, name));
        }

        public boolean contains(EntryType type, String name) {
            if (!registryMap.containsKey(type))
                return false;

            return registryMap.get(type).containsKey(name);
        }

        public void deleteEntry(EntryType type, String name) {
            registryMap.get(type).remove(name);
        }

        public void clear() {
            registryMap.clear();
        }

        @Override
        public @NotNull NBTTagCompound serializeNBT() {
            var tag = new NBTTagCompound();
            for (var type : registryMap.keySet()) {
                var typeTag = new NBTTagCompound();
                var entries = registryMap.get(type);
                for (var name : entries.keySet()) {
                    typeTag.setTag(name, entries.get(name).getEntry());
                }
                String key = type.getName() + ":" + type.ordinal();
                tag.setTag(key, typeTag);
            }
            return tag;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            for (var entryTypes : nbt.getKeySet()) {
                String[] split = entryTypes.split(":");
                if (split.length != 2) continue;

                EntryType type = EntryType.VALUES[Integer.parseInt(split[1])];

                var entryNames = nbt.getCompoundTag(entryTypes);
                for (var name : entryNames.getKeySet()) {
                    registryMap.computeIfAbsent(type, key -> new HashMap<>())
                            .put(name, VirtualEntry.fromNBT(entryNames.getCompoundTag(name)));
                }
            }
        }
    }

    public static class VirtualEntry {

        private static final String DEFAULT_COLOR = "FFFFFFFF";
        private static final String NAME_KEY = "entry_name";
        private static final String COLOR_KEY = "color";
        private static final String TYPE_KEY = "entry_type";
        private static final String ENTRY_KEY = "entry_tag";
        private final NBTTagCompound entry;

        private VirtualEntry(EntryType type, String name, String color, NBTTagCompound entry) {
            var tag = new NBTTagCompound();
            tag.setByte(TYPE_KEY, (byte) type.ordinal());
            tag.setString(NAME_KEY, name);
            tag.setString(COLOR_KEY, color);
            tag.setTag(ENTRY_KEY, entry);
            this.entry = tag;
        }

        private VirtualEntry(NBTTagCompound entry) {
            this.entry = entry;
        }

        public static VirtualEntry of(EntryType type, String name, String color, NBTTagCompound entry) {
            return new VirtualEntry(type, name, color, entry);
        }

        public static VirtualEntry of(EntryType type, String name) {
            return new VirtualEntry(type, name, DEFAULT_COLOR, new NBTTagCompound());
        }

        public static VirtualEntry fromNBT(NBTTagCompound entry) {
            return new VirtualEntry(entry);
        }

        public EntryType getType() {
            return EntryType.VALUES[this.entry.getByte(TYPE_KEY)];
        }

        public String getColor() {
            return this.entry.getString(COLOR_KEY);
        }

        public void setColor(String color) {
            this.entry.setString(COLOR_KEY, color == null ? DEFAULT_COLOR : color);
        }

        public String getName() {
            return this.entry.getString(NAME_KEY);
        }

        public NBTTagCompound getEntry() {
            return this.entry.getCompoundTag(ENTRY_KEY);
        }

        public void setEntry(NBTTagCompound entry) {
            this.entry.setTag(ENTRY_KEY, entry == null ? new NBTTagCompound() : entry);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof VirtualEntry other)) return false;
            return this.getType() == other.getType() &&
                    this.getName().equals(other.getName());

        }
    }
}
