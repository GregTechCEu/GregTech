package gregtech.common.pipelike.net.fluid;

import gregtech.api.capability.impl.FluidHandlerList;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.path.NetPath;
import gregtech.api.util.collection.ListHashSet;

import net.minecraftforge.fluids.capability.IFluidHandler;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class FluidNetworkView {

    public static final FluidNetworkView EMPTY = FluidNetworkView.of(ImmutableBiMap.of());
    private final FluidHandlerList handler;
    private final BiMap<IFluidHandler, NetNode> handlerNetNodeBiMap;
    private @Nullable Map<NetNode, ListHashSet<NetPath>> incomingCache;
    private @Nullable Map<NetNode, ListHashSet<NetPath>> outgoingCache;

    public FluidNetworkView(FluidHandlerList handler, BiMap<IFluidHandler, NetNode> handlerNetNodeBiMap) {
        this.handler = handler;
        this.handlerNetNodeBiMap = handlerNetNodeBiMap;
    }

    public static FluidNetworkView of(BiMap<IFluidHandler, NetNode> handlerNetNodeBiMap) {
        return new FluidNetworkView(new FluidHandlerList(handlerNetNodeBiMap.keySet()), handlerNetNodeBiMap);
    }

    public FluidHandlerList getHandler() {
        return handler;
    }

    public BiMap<IFluidHandler, NetNode> getBiMap() {
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
