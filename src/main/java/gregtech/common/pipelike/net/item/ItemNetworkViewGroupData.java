package gregtech.common.pipelike.net.item;

import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.graphnet.group.GroupData;
import gregtech.api.graphnet.group.NodeCacheGroupData;
import gregtech.api.graphnet.net.NetEdge;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.pipenet.NodeExposingCapabilities;
import gregtech.api.graphnet.traverse.EdgeDirection;
import gregtech.api.graphnet.traverse.NetClosestIterator;
import gregtech.api.graphnet.traverse.NetIterator;

import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class ItemNetworkViewGroupData extends NodeCacheGroupData<ItemNetworkView> {

    @Override
    protected ItemNetworkView getNew(@NotNull NetNode node) {
        // use a list to preserve 'found order' from the iterator,
        // so closer handlers are lower in our handler list's slot order.
        List<IItemHandler> handlerList = new ObjectArrayList<>();
        BiMap<IItemHandler, NetNode> map = HashBiMap.create();
        NetIterator iter = new NetClosestIterator(node, EdgeDirection.ALL);
        while (iter.hasNext()) {
            NetNode next = iter.next();
            if (next instanceof NodeExposingCapabilities exposer) {
                IItemHandler handler = exposer.getProvider().getCapability(
                        CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                        exposer.exposedFacing());
                if (handler != null && ItemCapabilityObject.instanceOf(handler) == null) {
                    handlerList.add(handler);
                    map.put(handler, next);
                }
            }
        }
        return new ItemNetworkView(new ItemHandlerList(handlerList), map);
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
        return Pair.of(this, new ItemNetworkViewGroupData());
    }

    // unused since we override splitAcross
    @Override
    protected @NotNull NodeCacheGroupData<ItemNetworkView> buildFilteredCache(@NotNull Set<NetNode> filterNodes) {
        return this;
    }
}
