package gregtech.api.pipenet.flow;

import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NodeG;
import gregtech.api.pipenet.block.IPipeType;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;

public class FlowChannelManager<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
        NodeDataType extends INodeData<NodeDataType>> {

    private final WorldPipeFlowNetG<NodeDataType, PipeType> net;

    protected final Set<NodeG<PipeType, NodeDataType>> activeSinks = new ObjectOpenHashSet<>();

    private final Map<Object, FlowChannel<PipeType, NodeDataType>> channels = new Object2ObjectOpenHashMap<>();

    public FlowChannelManager(WorldPipeFlowNetG<NodeDataType, PipeType> net) {
        this.net = net;
        WeakReference<FlowChannelManager<?, ?>> ref = new WeakReference<>(this);
        FlowChannelTicker.addManager(net.getWorld(), ref);
        this.net.addManager(ref);
    }

    void clearAlgs() {
        this.channels.values().forEach(FlowChannel::clearAlg);
    }

    public void tick() {
        if (this.activeSinks.size() == 0) return;
        this.channels.forEach((k, v) -> v.evaluate());
        if (this.net.unhandledOldNodes.size() != 0) {
            for (NodeG<PipeType, NodeDataType> node : this.net.unhandledOldNodes) {
                node.getGroupSafe().connectionChange(node);
            }
        }
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

    public void addSink(NodeG<PipeType, NodeDataType> sink) {
        if (this.activeSinks.add(sink)) this.net.getPipeGraph().addEdge(sink, this.net.getSuperSink());
    }

    public void removeSink(NodeG<PipeType, NodeDataType> sink) {
        if (this.activeSinks.remove(sink)) this.net.getPipeGraph().removeEdge(sink, this.net.getSuperSink());
    }

    public void setChannel(Object key, FlowChannel<PipeType, NodeDataType> channel) {
        this.channels.put(key, channel.setManager(this));
    }

    public void disconnectSuperNodes() {
        this.channels.forEach((key, value) -> value.disconnectSuperNodes());
        for (NodeG<PipeType, NodeDataType> node : activeSinks) {
            this.net.getPipeGraph().removeEdge(node, getSuperSink());
        }
    }

    public void reconnectSuperNodes() {
        this.channels.forEach((key, value) -> value.reconnectSuperNodes());
        for (NodeG<PipeType, NodeDataType> node : activeSinks) {
            this.net.getPipeGraph().addEdge(node, getSuperSink());
        }
    }

    @Nullable
    public FlowChannel<PipeType, NodeDataType> getChannel(Object key) {
        return this.channels.get(key);
    }

    public void removeChannel(Object key) {
        this.channels.remove(key);
    }
}
