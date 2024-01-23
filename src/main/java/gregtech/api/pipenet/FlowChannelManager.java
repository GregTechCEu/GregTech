package gregtech.api.pipenet;

import gregtech.api.pipenet.block.IPipeType;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class FlowChannelManager<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
        NodeDataType extends INodeData<NodeDataType>> {

    private final WorldPipeFlowNetG<NodeDataType, PipeType> net;

    protected final Set<NodeG<PipeType, NodeDataType>> activeSinks = new ObjectOpenHashSet<>();

    private final Map<Object, FlowChannel<PipeType, NodeDataType>> channels = new Object2ObjectOpenHashMap<>();

    public FlowChannelManager(WorldPipeFlowNetG<NodeDataType, PipeType> net) {
        this.net = net;
        FlowChannelTicker.addManager(net.getWorld(), this);
    }

    public void tick() {
        channels.forEach((k, v) -> v.evaluate());
    }

    /**
     * Updates active nodes based on the active nodes of another manager.
     */
    public FlowChannelManager<PipeType, NodeDataType> merge(FlowChannelManager<PipeType, NodeDataType> otherManager) {
        this.activeSinks.addAll(otherManager.activeSinks);
        for (Map.Entry<Object, FlowChannel<PipeType, NodeDataType>> entry : otherManager.channels.entrySet()) {
            this.channels.merge(entry.getKey(), entry.getValue(), FlowChannel::merge);
        }
        return this;
    }

    /**
     * Generates a new manager that filters this manager's active nodes by removing the provided nodes.
     */
    public FlowChannelManager<PipeType, NodeDataType> subManager(Set<NodeG<PipeType, NodeDataType>> nodes) {
        FlowChannelManager<PipeType, NodeDataType> newManager = new FlowChannelManager<>(this.net);
        newManager.channels.putAll(this.channels);
        newManager.channels.forEach((key, value) -> {
            value.setManager(newManager);
            value.activeSources.removeAll(nodes);
            for (NodeG<PipeType, NodeDataType> node : nodes) {
                value.receiveSidesMap.remove(node);
            }
        });
        newManager.activeSinks.addAll(this.activeSinks);
        newManager.activeSinks.removeAll(nodes);
        return newManager;
    }

    /**
     * Filters this manager's active nodes by removing the provided nodes.
     */
    public void removeNodes(Set<NodeG<PipeType, NodeDataType>> nodes) {
        this.activeSinks.removeAll(nodes);
        this.channels.forEach((key, value) -> value.removeNodes(nodes));
    }

    public void removeNode(NodeG<PipeType, NodeDataType> node) {
        this.activeSinks.remove(node);
        this.channels.forEach((key, value) -> value.removeNode(node));
    }

    public NodeG<PipeType, NodeDataType> getSuperSource() {
        return this.net.getSuperSource();
    }

    public NodeG<PipeType, NodeDataType> getSuperSink() {
        return this.net.getSuperSink();
    }

    public Set<NodeG<PipeType, NodeDataType>> getActiveSinks() {
        return activeSinks;
    }

    public void setChannel(Object key, FlowChannel<PipeType, NodeDataType> channel) {
        this.channels.put(key, channel.setManager(this));
    }

    @Nullable
    public FlowChannel<PipeType, NodeDataType> getChannel(Object key) {
        return this.channels.get(key);
    }

    public void removeChannel(Object key) {
        this.channels.remove(key);
    }
}
