package gregtech.common.pipelike.net.fluid;

import gregtech.api.capability.impl.FluidHandlerList;
import gregtech.api.graphnet.group.GroupData;
import gregtech.api.graphnet.group.NodeCacheGroupData;
import gregtech.api.graphnet.net.NetEdge;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.pipenet.NodeExposingCapabilities;
import gregtech.api.graphnet.traverse.EdgeDirection;
import gregtech.api.graphnet.traverse.NetClosestIterator;
import gregtech.api.graphnet.traverse.NetIterator;

import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class FluidNetworkViewGroupData extends NodeCacheGroupData<FluidNetworkView> {

    @Override
    protected FluidNetworkView getNew(@NotNull NetNode node) {
        // use a list to preserve 'found order' from the iterator,
        // so closer handlers are earlier in our handler list's extraction/insertion preference
        List<IFluidHandler> handlerList = new ObjectArrayList<>();
        BiMap<IFluidHandler, NetNode> map = HashBiMap.create();
        NetIterator iter = new NetClosestIterator(node, EdgeDirection.ALL);
        while (iter.hasNext()) {
            NetNode next = iter.next();
            if (next instanceof NodeExposingCapabilities exposer) {
                IFluidHandler handler = exposer.getProvider().getCapability(
                        CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                        exposer.exposedFacing());
                if (handler != null && FluidCapabilityObject.instanceOf(handler) == null) {
                    map.put(handler, next);
                    handlerList.add(handler);
                }
            }
        }
        return new FluidNetworkView(new FluidHandlerList(handlerList), map);
    }

    @Override
    public void notifyOfBridgingEdge(@NotNull NetEdge edge) {
        invalidateAll();
    }

    @Override
    public void notifyOfRemovedEdge(@NotNull NetEdge edge) {
        invalidateAll();
    }

    @Override
    protected @Nullable GroupData mergeAcross(@Nullable GroupData other, @NotNull NetEdge edge) {
        invalidateAll();
        return this;
    }

    @Override
    public @NotNull Pair<GroupData, GroupData> splitAcross(@NotNull Set<NetNode> sourceNodes,
                                                           @NotNull Set<NetNode> targetNodes) {
        invalidateAll();
        return Pair.of(this, new FluidNetworkViewGroupData());
    }

    // unused since we override splitAcross
    @Override
    protected @NotNull NodeCacheGroupData<FluidNetworkView> buildFilteredCache(@NotNull Set<NetNode> filterNodes) {
        return this;
    }
}
