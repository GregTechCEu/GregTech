package gregtech.api.util.virtualregistry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VirtualRegistryMap implements INBTSerializable<NBTTagCompound>, Map<EntryType, Map<String, VirtualEntry>> {

    private static final Map<EntryType, Map<String, VirtualEntry>> registryMap = new EnumMap<>(
            EntryType.class);

    public VirtualRegistryMap(NBTTagCompound tag) {
        deserializeNBT(tag);
    }

    public VirtualRegistryMap() {}

    public VirtualEntry getEntry(EntryType type, String name) {
        return get(type).get(name);
    }

    public void addEntry(VirtualEntry entry) {
        computeIfAbsent(entry.getType(), k -> new HashMap<>())
                .put(entry.getName(), entry);
    }

    public boolean contains(EntryType type, String name) {
        if (!containsKey(type))
            return false;

        return get(type).containsKey(name);
    }

    public void deleteEntry(EntryType type, String name) {
        get(type).remove(name);
    }

    @Override
    public int size() {
        return registryMap.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return registryMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return registryMap.containsValue(value);
    }

    @Override
    public Map<String, VirtualEntry> get(Object key) {
        return registryMap.get(key);
    }

    @Nullable
    @Override
    public Map<String, VirtualEntry> put(EntryType key, Map<String, VirtualEntry> value) {
        return registryMap.put(key, value);
    }

    @Override
    public Map<String, VirtualEntry> remove(Object key) {
        return registryMap.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends EntryType, ? extends Map<String, VirtualEntry>> m) {
        registryMap.putAll(m);
    }

    public void clear() {
        registryMap.clear();
    }

    @NotNull
    @Override
    public Set<EntryType> keySet() {
        return registryMap.keySet();
    }

    @Override
    public @NotNull Collection<Map<String, VirtualEntry>> values() {
        return registryMap.values();
    }

    @Override
    public Set<Entry<EntryType, Map<String, VirtualEntry>>> entrySet() {
        return registryMap.entrySet();
    }

    @Override
    public @NotNull NBTTagCompound serializeNBT() {
        var tag = new NBTTagCompound();
        for (var type : registryMap.keySet()) {
            var typeTag = new NBTTagCompound();
            var entries = registryMap.get(type);
            for (var name : entries.keySet()) {
                typeTag.setTag(name, entries.get(name).serializeNBT());
            }
            String key = type.getName() + ":" + type.ordinal();
            tag.setTag(key, typeTag);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        for (var entryType : nbt.getKeySet()) {
            String[] split = entryType.split(":");
            if (split.length != 2) continue;

            EntryType type = EntryType.VALUES[Integer.parseInt(split[1])];

            var virtualEntries = nbt.getCompoundTag(entryType);
            for (var name : virtualEntries.getKeySet()) {
                var entry = VirtualRegistryBase.createEntry(type);
                if (entry == null) continue;

                entry.deserializeNBT(virtualEntries.getCompoundTag(name));
                registryMap.computeIfAbsent(type, key -> new HashMap<>())
                        .put(name, entry);
            }
        }
    }
}
