package gregtech.api.graphnet;

import gregtech.api.graphnet.logic.INetLogicEntry;
import gregtech.api.graphnet.logic.INetLogicEntryListener;
import gregtech.api.graphnet.logic.NetLogicData;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * MultiNodeHelpers are utility objects used to preserve sync between multiple nodes owned by different graphs. They do
 * this by <br>A) keeping a record of traversals to allow for blocking traversal when another net has been traversed
 * recently and <br>B) make sure that logic entries requiring it are the same object across all synced nodes. <br><br>
 * MultiNodeHelpers have no standard implementation and must be handled by a net and its nodes; see
 * {@link gregtech.api.graphnet.pipenet.WorldPipeNet} and {@link gregtech.api.graphnet.pipenet.WorldPipeNetNode}
 * for an example of this in action.
 */
public class MultiNodeHelper implements INetLogicEntryListener {

    protected final Object2ObjectOpenHashMap<IGraphNet, NetNode> handledNodes = new Object2ObjectOpenHashMap<>();

    protected final Object2ObjectOpenHashMap<IGraphNet, @NotNull Long> recentTransferNets = new Object2ObjectOpenHashMap<>();
    protected final int transferTimeout;

    protected final NetLogicData mergedData = new NetLogicData();

    public MultiNodeHelper(int transferTimeout) {
        this.transferTimeout = transferTimeout;
    }

    public boolean traverse(IGraphNet net, long queryTick, boolean simulate) {
        var iter = recentTransferNets.object2ObjectEntrySet().fastIterator();
        boolean allowed = true;
        while (iter.hasNext()) {
            var next = iter.next();
            if (net.clashesWith(next.getKey())) {
                if (next.getValue() <= queryTick) {
                    iter.remove();
                } else {
                    allowed = false;
                    break;
                }
            }
        }
        if (allowed && !simulate) {
            recentTransferNets.put(net, queryTick + transferTimeout);
        }
        return allowed;
    }

    @Override
    public void markLogicEntryAsUpdated(INetLogicEntry<?, ?> entry, boolean fullChange) {
        // TODO have a helper or something on clientside to avoid redundant packets
        handledNodes.forEach((k, v) -> v.getData().markLogicEntryAsUpdated(entry, fullChange));
    }

    public void addNode(NetNode node) {
        handledNodes.put(node.getNet(), node);
        List<INetLogicEntry<?, ?>> toSet = new ObjectArrayList<>();
        for (INetLogicEntry<?, ?> entry : node.getData().getEntries()) {
            if (entry.mergedToMultiNodeHelper()) {
                INetLogicEntry<?, ?> existing = mergedData.getLogicEntryNullable(entry);
                if (existing != null) {
                    existing.merge(node, entry);
                    // don't put it into the data yet because we're currently iterating through the data's entries.
                    toSet.add(existing);
                } else {
                    entry.registerToMultiNodeHelper(this);
                    mergedData.setLogicEntry(entry);
                }
            }
        }
        for (INetLogicEntry<?, ?> entry : toSet) {
            node.getData().setLogicEntry(entry);
        }
    }

    public void removeNode(NetNode node) {
        if (handledNodes.remove(node.getNet(), node)) {
            for (INetLogicEntry<?, ?> entry : this.mergedData.getEntries()) {
                node.getData().removeLogicEntry(entry);
                entry.unmerge(node);
            }
        }
    }
}
