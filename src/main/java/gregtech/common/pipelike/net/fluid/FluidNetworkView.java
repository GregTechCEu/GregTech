package gregtech.common.pipelike.net.fluid;

import gregtech.api.capability.impl.FluidHandlerList;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.path.NetPath;
import gregtech.api.util.collection.ListHashSet;

import net.minecraftforge.fluids.capability.IFluidHandler;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class FluidNetworkView {

    public static final FluidNetworkView EMPTY = FluidNetworkView.of(ImmutableBiMap.of());
    private final FluidHandlerList handler;
    private final BiMap<IFluidHandler, NetNode> handlerNetNodeBiMap;
    private @Nullable Map<NetNode, ListHashSet<NetPath>> pathCache;

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

    public Map<NetNode, ListHashSet<NetPath>> getPathCache() {
        if (pathCache == null) pathCache = new Reference2ReferenceOpenHashMap<>();
        return pathCache;
    }

    public ListHashSet<NetPath> getPathCache(@NotNull NetNode target) {
        if (pathCache == null) pathCache = new Reference2ReferenceOpenHashMap<>();
        return pathCache.computeIfAbsent(target, n -> new ListHashSet<>(1));
    }
}
