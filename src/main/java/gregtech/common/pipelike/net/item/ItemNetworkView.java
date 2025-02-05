package gregtech.common.pipelike.net.item;

import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.path.NetPath;
import gregtech.api.util.collection.ListHashSet;

import net.minecraftforge.items.IItemHandler;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class ItemNetworkView {

    public static final ItemNetworkView EMPTY = ItemNetworkView.of(ImmutableBiMap.of());
    private final ItemHandlerList handler;
    private final BiMap<IItemHandler, NetNode> handlerNetNodeBiMap;
    private @Nullable Map<NetNode, ListHashSet<NetPath>> incomingCache;
    private @Nullable Map<NetNode, ListHashSet<NetPath>> outgoingCache;

    public ItemNetworkView(ItemHandlerList handler, BiMap<IItemHandler, NetNode> handlerNetNodeBiMap) {
        this.handler = handler;
        this.handlerNetNodeBiMap = handlerNetNodeBiMap;
    }

    public static ItemNetworkView of(BiMap<IItemHandler, NetNode> handlerNetNodeBiMap) {
        return new ItemNetworkView(new ItemHandlerList(handlerNetNodeBiMap.keySet()), handlerNetNodeBiMap);
    }

    public ItemHandlerList getHandler() {
        return handler;
    }

    public BiMap<IItemHandler, NetNode> getBiMap() {
        return handlerNetNodeBiMap;
    }

    public Map<NetNode, ListHashSet<NetPath>> outgoingCache() {
        if (outgoingCache == null) outgoingCache = new Reference2ReferenceOpenHashMap<>();
        return outgoingCache;
    }

    public Map<NetNode, ListHashSet<NetPath>> incomingCache() {
        if (incomingCache == null) incomingCache = new Reference2ReferenceOpenHashMap<>();
        return incomingCache;
    }
}
