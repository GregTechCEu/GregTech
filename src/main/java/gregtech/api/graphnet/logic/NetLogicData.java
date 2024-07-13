package gregtech.api.graphnet.logic;

import gregtech.api.graphnet.gather.GTGraphGatherables;

import gregtech.api.util.IDirtyNotifiable;

import gregtech.api.util.function.TriConsumer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class NetLogicData implements INBTSerializable<NBTTagList> {

    // TODO caching logic on simple logics to reduce amount of reduntant creation?
    private final Object2ObjectOpenHashMap<String, INetLogicEntry<?, ?>> logicEntrySet;

    private final Set<LogicDataListener> listeners = new ObjectOpenHashSet<>();
    
    public NetLogicData() {
        logicEntrySet = new Object2ObjectOpenHashMap<>(4);
    }

    private NetLogicData(Object2ObjectOpenHashMap<String, INetLogicEntry<?, ?>> logicEntrySet) {
        this.logicEntrySet = logicEntrySet;
    }

    /**
     * If the {@link INetLogicEntry#union(INetLogicEntry)} operation is not supported for this entry,
     * nothing happens if an entry is already present.
     */
    public NetLogicData mergeLogicEntry(INetLogicEntry<?, ?> entry) {
        INetLogicEntry<?, ?> current = logicEntrySet.get(entry.getName());
        if (current == null) return setLogicEntry(entry);

        if (entry.getClass().isInstance(current)) {
            entry = current.union(entry);
            if (entry == null) return this;
        }
        return setLogicEntry(entry);
    }

    public NetLogicData setLogicEntry(INetLogicEntry<?, ?> entry) {
        entry.registerToNetLogicData(this);
        logicEntrySet.put(entry.getName(), entry);
        this.markLogicEntryAsUpdated(entry, true);
        return this;
    }

    public NetLogicData removeLogicEntry(@NotNull INetLogicEntry<?, ?> key) {
        return removeLogicEntry(key.getName());
    }

    public NetLogicData removeLogicEntry(@NotNull String key) {
        INetLogicEntry<?, ?> entry = logicEntrySet.remove(key);
        if (entry != null) this.listeners.forEach(l -> l.markChanged(entry, true, true));
        logicEntrySet.trim();
        return this;
    }

    public void markLogicEntryAsUpdated(INetLogicEntry<?, ?> entry, boolean fullChange) {
        this.listeners.forEach(l -> l.markChanged(entry, false, fullChange));
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

    @Contract("_, _ -> new")
    public static @NotNull NetLogicData union(@NotNull NetLogicData sourceData, @Nullable NetLogicData targetData) {
        Object2ObjectOpenHashMap<String, INetLogicEntry<?, ?>> newLogic =
                new Object2ObjectOpenHashMap<>(sourceData.logicEntrySet);
        if (targetData != null) {
            for (String key : newLogic.keySet()) {
                newLogic.computeIfPresent(key, (k, v) -> v.union(targetData.logicEntrySet.get(k)));
            }
            targetData.logicEntrySet.forEach((key, value) -> newLogic.computeIfAbsent(key, k -> value.union(null)));
        }
        return new NetLogicData(newLogic);
    }

    @Contract("_, _ -> new")
    public static @NotNull NetLogicData union(@NotNull NetLogicData first, @NotNull NetLogicData... others) {
        Object2ObjectOpenHashMap<String, INetLogicEntry<?, ?>> newLogic =
                new Object2ObjectOpenHashMap<>(first.logicEntrySet);
        for (NetLogicData other : others) {
            for (String key : newLogic.keySet()) {
                newLogic.computeIfPresent(key, (k, v) -> v.union(other.logicEntrySet.get(k)));
            }
            other.logicEntrySet.forEach((key, value) -> newLogic.computeIfAbsent(key, k -> value.union(null)));
        }
        return new NetLogicData(newLogic);
    }

    public void addListener(LogicDataListener listener) {
        this.listeners.add(listener);
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

    public final class LogicDataListener {
        private final TriConsumer<INetLogicEntry<?, ?>, Boolean, Boolean> listener;

        public LogicDataListener(TriConsumer<INetLogicEntry<?, ?>, Boolean, Boolean> listener) {
            this.listener = listener;
        }

        private void markChanged(INetLogicEntry<?, ?> updatedEntry, boolean removed, boolean fullChange) {
            this.listener.accept(updatedEntry, removed, fullChange);
        }

        public void invalidate() {
            listeners.remove(this);
        }
    }
}
