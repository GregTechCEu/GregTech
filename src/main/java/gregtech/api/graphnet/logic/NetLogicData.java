package gregtech.api.graphnet.logic;

import gregtech.api.network.IPacket;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.INBTSerializable;

import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NetLogicData implements INBTSerializable<NBTTagList>, IPacket, INetLogicEntryListener {

    private final Reference2ObjectOpenHashMap<NetLogicType<?>, NetLogicEntry<?, ?>> logicEntrySet;

    private final ReferenceArrayList<ILogicDataListener> listeners = new ReferenceArrayList<>(1);

    public NetLogicData() {
        logicEntrySet = new Reference2ObjectOpenHashMap<>(4);
    }

    private NetLogicData(Reference2ObjectOpenHashMap<NetLogicType<?>, NetLogicEntry<?, ?>> logicEntrySet) {
        this.logicEntrySet = logicEntrySet;
    }

    /**
     * If the {@link NetLogicEntry#union(NetLogicEntry)} operation is not supported for this entry,
     * nothing happens if an entry is already present.
     */
    public NetLogicData mergeLogicEntry(NetLogicEntry<?, ?> entry) {
        NetLogicEntry<?, ?> current = logicEntrySet.get(entry.getType());
        if (current == null) return setLogicEntry(entry);

        if (entry.getClass().isInstance(current)) {
            entry = current.union(entry);
            if (entry == null) return this;
        }
        return setLogicEntry(entry);
    }

    public NetLogicData setLogicEntry(NetLogicEntry<?, ?> entry) {
        entry.registerToNetLogicData(this);
        logicEntrySet.put(entry.getType(), entry);
        this.markLogicEntryAsUpdated(entry, true);
        return this;
    }

    /**
     * Returns all registered logic entries; this should be treated in read-only manner.
     */
    public ObjectCollection<NetLogicEntry<?, ?>> getEntries() {
        return logicEntrySet.values();
    }

    public void clearData() {
        logicEntrySet.clear();
        logicEntrySet.trim(4);
    }

    public NetLogicData removeLogicEntry(@NotNull NetLogicEntry<?, ?> entry) {
        return removeLogicEntry(entry.getType());
    }

    public NetLogicData removeLogicEntry(@NotNull NetLogicType<?> type) {
        NetLogicEntry<?, ?> entry = logicEntrySet.remove(type);
        if (entry != null) {
            entry.deregisterFromNetLogicData(this);
            this.listeners.forEach(l -> l.markChanged(entry, true, true));
            logicEntrySet.trim();
        }
        return this;
    }

    @Override
    public void markLogicEntryAsUpdated(NetLogicEntry<?, ?> entry, boolean fullChange) {
        for (int i = 0; i < listeners.size(); i++) {
            ILogicDataListener l = listeners.get(i);
            l.markChanged(entry, false, fullChange);
        }
    }

    public boolean hasLogicEntry(@NotNull NetLogicType<?> type) {
        return logicEntrySet.containsKey(type);
    }

    @Nullable
    public <T extends NetLogicEntry<T, ?>> T getLogicEntryNullable(@NotNull NetLogicType<T> type) {
        return type.cast(logicEntrySet.get(type));
    }

    @NotNull
    public <T extends NetLogicEntry<T, ?>> T getLogicEntryDefaultable(@NotNull NetLogicType<T> type) {
        return type.cast(logicEntrySet.getOrDefault(type, type.getDefault()));
    }

    @Contract("null, null -> null; !null, _ -> new; _, !null -> new")
    public static @Nullable NetLogicData unionNullable(@Nullable NetLogicData sourceData,
                                                       @Nullable NetLogicData targetData) {
        if (sourceData == null && targetData == null) return null;
        return union(sourceData == null ? targetData : sourceData, sourceData == null ? null : targetData);
    }

    @Contract("_, _ -> new")
    public static @NotNull NetLogicData union(@NotNull NetLogicData sourceData, @Nullable NetLogicData targetData) {
        Reference2ObjectOpenHashMap<NetLogicType<?>, NetLogicEntry<?, ?>> newLogic = new Reference2ObjectOpenHashMap<>(
                sourceData.logicEntrySet);
        if (targetData != null) {
            for (NetLogicType<?> key : newLogic.keySet()) {
                newLogic.computeIfPresent(key, (k, v) -> v.union(targetData.logicEntrySet.get(k)));
            }
            targetData.logicEntrySet.forEach((key, value) -> newLogic.computeIfAbsent(key, k -> value.union(null)));
        }
        return new NetLogicData(newLogic);
    }

    @Contract("_, _ -> new")
    public static @NotNull NetLogicData union(@NotNull NetLogicData first, @NotNull NetLogicData... others) {
        Reference2ObjectOpenHashMap<NetLogicType<?>, NetLogicEntry<?, ?>> newLogic = new Reference2ObjectOpenHashMap<>(
                first.logicEntrySet);
        for (NetLogicData other : others) {
            for (NetLogicType<?> key : newLogic.keySet()) {
                newLogic.computeIfPresent(key, (k, v) -> v.union(other.logicEntrySet.get(k)));
            }
            other.logicEntrySet.forEach((key, value) -> newLogic.computeIfAbsent(key, k -> value.union(null)));
        }
        return new NetLogicData(newLogic);
    }

    @Override
    public NBTTagList serializeNBT() {
        NBTTagList list = new NBTTagList();
        for (NetLogicEntry<?, ?> entry : getEntries()) {
            NBTBase nbt = entry.serializeNBT();
            if (nbt == null) continue;
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Type", entry.getType().getName());
            tag.setTag("Tag", nbt);
            list.appendTag(tag);
        }
        return list;
    }

    @Override
    public void deserializeNBT(NBTTagList nbt) {
        for (int i = 0; i < nbt.tagCount(); i++) {
            NBTTagCompound tag = nbt.getCompoundTagAt(i);
            NetLogicType<?> type = NetLogicRegistry.getTypeNullable(tag.getString("Type"));
            if (type == null) continue;
            NetLogicEntry<?, ?> entry = this.logicEntrySet.get(type);
            if (entry == null) entry = type.getNew();
            entry.deserializeNBTNaive(tag.getTag("Tag"));
            this.logicEntrySet.put(type, entry);
        }
    }

    @Override
    public void encode(PacketBuffer buf) {
        int count = 0;
        for (NetLogicEntry<?, ?> entry : getEntries()) {
            if (entry.shouldEncode()) count++;
        }
        buf.writeVarInt(count);
        for (NetLogicEntry<?, ?> entry : getEntries()) {
            if (entry.shouldEncode()) writeEntry(buf, entry, true);
        }
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.logicEntrySet.clear();
        int entryCount = buf.readVarInt();
        for (int i = 0; i < entryCount; i++) {
            readEntry(buf);
        }
        this.logicEntrySet.trim();
    }

    public static void writeEntry(@NotNull PacketBuffer buf, @NotNull NetLogicEntry<?, ?> entry, boolean fullChange) {
        buf.writeVarInt(NetLogicRegistry.getNetworkID(entry));
        buf.writeBoolean(fullChange);
        entry.encode(buf, fullChange);
    }

    /**
     * @return the net logic entry decoded to.
     */
    @Nullable
    public NetLogicEntry<?, ?> readEntry(@NotNull PacketBuffer buf) {
        int id = buf.readVarInt();
        boolean fullChange = buf.readBoolean();
        NetLogicType<?> type = NetLogicRegistry.getType(id);
        NetLogicEntry<?, ?> existing = this.getLogicEntryNullable(type);
        boolean add = false;
        if (existing == null) {
            // never partially decode into a new entry
            if (!fullChange) return null;
            existing = type.getNew();
            add = true;
        }
        try {
            existing.decode(buf, fullChange);
        } catch (Exception ignored) {
            NetLogicRegistry.throwDecodingError();
        }
        // make sure to add after decoding, so we don't notify listeners with an empty logic entry
        if (add) this.setLogicEntry(existing);
        return existing;
    }

    /**
     * Adds a listener to a weak set which is then notified for as long as it is not collected by the garbage collector.
     *
     * @param listener the listener.
     * @return the callback for the listener, allowing access to the registered listener and the ability to retire it in
     *         the future.
     *         WARNING- FAILING TO RETIRE THE LISTENER WHEN IT IS NO LONGER IN USE WILL CAUSE A MEMORY LEAK
     */
    public <T extends ILogicDataListener> ListenerCallback<T> addListener(T listener) {
        int index = listeners.size();
        listeners.add(listener);
        return new ListenerCallback<>() {

            @Override
            public T getListener() {
                return listener;
            }

            @Override
            public void retire() {
                listeners.remove(index);
            }
        };
    }

    public interface ILogicDataListener {

        void markChanged(NetLogicEntry<?, ?> updatedEntry, boolean removed, boolean fullChange);
    }

    public interface ListenerCallback<T extends ILogicDataListener> {

        T getListener();

        void retire();
    }
}
