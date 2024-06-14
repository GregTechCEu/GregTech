package gregtech.api.util.virtualregistry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

class VirtualRegistryMap implements INBTSerializable<NBTTagCompound> {

    private static final Map<EntryType, Map<String, VirtualEntry>> registryMap = new EnumMap<>(
            EntryType.class);

    public VirtualRegistryMap(NBTTagCompound tag) {
        deserializeNBT(tag);
    }

    public VirtualRegistryMap() {}

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
