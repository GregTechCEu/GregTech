package gregtech.api.graphnet.logic;

import gregtech.api.graphnet.gather.GTGraphGatherables;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

public final class NetLogicData implements INBTSerializable<NBTTagList> {

    private final Map<String, INetLogicEntry<?, ?>> logicEntrySet;
    
    public NetLogicData() {
        logicEntrySet = new Object2ObjectOpenHashMap<>();
    }

    private NetLogicData(Map<String, INetLogicEntry<?, ?>> logicEntrySet) {
        this.logicEntrySet = logicEntrySet;
    }

    public NetLogicData mergeLogicEntry(INetLogicEntry<?, ?> entry) {
        INetLogicEntry<?, ?> current = logicEntrySet.get(entry.getName());
        if (entry.getClass().isInstance(current)) {
            entry = current.union(entry);
        }
        return setLogicEntry(entry);
    }

    public NetLogicData setLogicEntry(INetLogicEntry<?, ?> entry) {
        logicEntrySet.put(entry.getName(), entry);
        return this;
    }

    public NetLogicData removeLogicEntry(@NotNull INetLogicEntry<?, ?> key) {
        logicEntrySet.remove(key.getName());
        return this;
    }

    @Nullable
    public <T extends INetLogicEntry<T, ?>> T getLogicEntryNullable(@NotNull T key) {
        try {
            return (T) logicEntrySet.get(key.getName());
        } catch (ClassCastException ignored) {
            return null;
        }
    }

    @NotNull
    public <T extends INetLogicEntry<T, ?>> T getLogicEntryDefaultable(@NotNull T key) {
        try {
            T returnable = (T) logicEntrySet.get(key.getName());
            return returnable == null ? key : returnable;
        } catch (ClassCastException ignored) {
            return key;
        }
    }

    public static NetLogicData union(NetLogicData sourceData, NetLogicData targetData) {
        Map<String, INetLogicEntry<?, ?>> newLogic = new Object2ObjectOpenHashMap<>(sourceData.logicEntrySet);
        newLogic.replaceAll((k, v) -> v.union(targetData.logicEntrySet.get(k)));
        targetData.logicEntrySet.forEach((key, value) -> newLogic.computeIfAbsent(key, k -> value.union(null)));
        return new NetLogicData(newLogic);
    }

    @Override
    public NBTTagList serializeNBT() {
        NBTTagList list = new NBTTagList();
        for (INetLogicEntry<?, ?> entry : logicEntrySet.values()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setTag("Tag", entry.serializeNBT());
            tag.setString("Name", entry.getName());
            list.appendTag(tag);
        }
        return list;
    }

    @Override
    public void deserializeNBT(NBTTagList nbt) {
        for (int i = 0; i < nbt.tagCount(); i++) {
            NBTTagCompound tag = nbt.getCompoundTagAt(i);
            String key = tag.getString("Name");
            INetLogicEntry<?, ?> entry = this.logicEntrySet.get(key);
            if (entry == null) entry = getSupplier(key).get();
            if (entry == null) continue;
            entry.deserializeNBTNaive(tag.getTag("Tag"));
        }
    }

    private static Supplier<INetLogicEntry<?, ?>> getSupplier(String identifier) {
        return GTGraphGatherables.getLogicsRegistry().getOrDefault(identifier, () -> null);
    }
}
