package gregtech.api.pipenet;

import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.flow.FlowChannel;
import gregtech.api.pipenet.flow.FlowChannelManager;
import gregtech.api.pipenet.flow.WorldPipeFlowNetG;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.INBTSerializable;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NetGroup<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
        NodeDataType extends INodeData<NodeDataType>> implements INBTSerializable<NBTTagCompound> {

    public final WorldPipeNetG<NodeDataType, PipeType> net;

    private final Graph<NodeG<PipeType, NodeDataType>, NetEdge> graph;

    private final Set<NodeG<PipeType, NodeDataType>> nodes;

    @Nullable
    private FlowChannelManager<PipeType, NodeDataType> channelManager = null;

    private final AbstractGroupData<PipeType, NodeDataType> data;

    public NetGroup(Graph<NodeG<PipeType, NodeDataType>, NetEdge> graph, WorldPipeNetG<NodeDataType, PipeType> net) {
        this.graph = graph;
        this.nodes = new ObjectOpenHashSet<>();
        this.net = net;
        this.data = net.getBlankGroupData();
    }

    public NetGroup(Graph<NodeG<PipeType, NodeDataType>, NetEdge> graph, WorldPipeNetG<NodeDataType, PipeType> net,
                    Set<NodeG<PipeType, NodeDataType>> nodes) {
        this.graph = graph;
        this.nodes = nodes;
        this.net = net;
        this.nodes.forEach(b -> b.setGroup(this));
        this.data = net.getBlankGroupData();
    }

    private void clear() {
        this.nodes.clear();
        this.channelManager = null;
    }

    protected void addNode(NodeG<PipeType, NodeDataType> node) {
        this.nodes.add(node);
        node.setGroup(this);
        this.connectionChange(node);
    }

    protected void addNodes(Set<NodeG<PipeType, NodeDataType>> nodes) {
        this.nodes.addAll(nodes);
        nodes.forEach(a -> {
            a.setGroup(this);
            this.connectionChange(a);
        });
    }

    @SafeVarargs
    protected final void addNodes(NodeG<PipeType, NodeDataType>... nodes) {
        for (NodeG<PipeType, NodeDataType> node : nodes) {
            this.addNode(node);
            this.connectionChange(node);
        }
    }

    public void connectionChange(NodeG<PipeType, NodeDataType> node) {
        if (!this.net.isFlow()) return;
        Map<EnumFacing, TileEntity> map = node.getConnecteds();
        // if map is null, then we're in world data load phase - do nothing
        if (map == null) return;
        if (map.size() != 0) {
            this.getChannelManager().addSink(node);
        } else {
            this.getChannelManager().removeSink(node);
        }
    }

    /**
     * Merges the groups of an edge if necessary.
     * 
     * @param source the source node of the edge
     * @param target the target node of the edge
     */
    public static void mergeEdge(NodeG<?, ?> source, NodeG<?, ?> target) {
        NetGroup<?, ?> sourceGroup = source.getGroupUnsafe();
        NetGroup<?, ?> targetGroup = target.getGroupUnsafe();
        if (sourceGroup == targetGroup) {
            if (sourceGroup == null) {
                sourceGroup = source.getGroupSafe();
            } else {
                sourceGroup.clearCaches();
                return;
            }
        }
        if (sourceGroup != null) {
            sourceGroup.mergeNode(target);
        } else {
            assert targetGroup != null;
            targetGroup.mergeNode(source);
        }
    }

    protected void mergeNode(NodeG<?, ?> node) {
        NodeG<PipeType, NodeDataType> cast = (NodeG<PipeType, NodeDataType>) node;
        NetGroup<PipeType, NodeDataType> group = cast.getGroupUnsafe();
        if (group != null) {
            this.addNodes(group.getNodes());
            if (this.net.isFlow()) {
                this.getChannelManager().merge(group.getChannelManager());
            }
            group.clear();
        } else addNode(cast);
        this.clearCaches();
    }

    /**
     * Split this group by removing a node. Automatically removes the node from the graph.
     * 
     * @param source node to remove
     * @return Whether the node existed in the graph
     */
    public boolean splitNode(NodeG<PipeType, NodeDataType> source) {
        if (this.graph.containsVertex(source)) {
            this.prepSplit();
            this.clearCaches();
            List<NodeG<?, ?>> targets = graph.outgoingEdgesOf(source).stream().map(a -> {
                // handling so undirected graphs don't throw an error
                if (net.isDirected()) return a.getTarget();
                if (a.getTarget().getNodePos() != source.getNodePos()) return a.getTarget();
                return a.getSource();
            }).collect(Collectors.toList());
            this.graph.removeVertex(source);
            this.nodes.remove(source);
            if (this.net.isFlow()) {
                this.getChannelManager().removeNode(source);
            }
            while (!targets.isEmpty()) {
                // get the lastmost target; if this throws a cast exception, something is very wrong with the graph.
                NodeG<PipeType, NodeDataType> target = (NodeG<PipeType, NodeDataType>) targets
                        .remove(targets.size() - 1);

                Set<NodeG<PipeType, NodeDataType>> targetGroup = new ObjectOpenHashSet<>();
                BreadthFirstIterator<NodeG<PipeType, NodeDataType>, NetEdge> i = new BreadthFirstIterator<>(graph,
                        target);
                NodeG<PipeType, NodeDataType> temp;
                while (i.hasNext()) {
                    temp = i.next();
                    targetGroup.add(temp);
                    // if we find a target node in our search, remove it from the list
                    targets.remove(temp);
                }
                this.nodes.removeAll(targetGroup);
                if (targetGroup.size() != 0) {
                    if (this.net.isFlow()) {
                        // remove our owned nodes from their manager, and remove their nodes from our manager.
                        // also make sure to finish split from their perspective.
                        new NetGroup<>(this.graph, this.net, targetGroup)
                                .setChannelManager(this.getChannelManager().subManager(this.nodes))
                                .finishSplit();
                        this.getChannelManager().removeNodes(targetGroup);
                    } else {
                        new NetGroup<>(this.graph, this.net, targetGroup);
                    }
                }
            }
            this.finishSplit();
            return true;
        }
        return false;
    }

    /**
     * Split this group by removing an edge. Automatically removes the edge from the graph.
     * 
     * @param source source of the edge
     * @param target target of the edge
     * @return Whether the edge existed in the graph
     */
    public boolean splitEdge(NodeG<PipeType, NodeDataType> source, NodeG<PipeType, NodeDataType> target) {
        if (graph.removeEdge(source, target) != null) {
            this.prepSplit();
            this.clearCaches();
            Set<NodeG<PipeType, NodeDataType>> targetGroup = new ObjectOpenHashSet<>();
            BreadthFirstIterator<NodeG<PipeType, NodeDataType>, NetEdge> i = new BreadthFirstIterator<>(graph, target);
            NodeG<PipeType, NodeDataType> temp;
            while (i.hasNext()) {
                temp = i.next();
                // if there's a another complete path to the source node from the target node, there's no need to split
                if (source == temp) {
                    this.finishSplit();
                    return true;
                }
                targetGroup.add(temp);
            }
            this.nodes.removeAll(targetGroup);
            if (targetGroup.size() != 0) {
                if (this.net.isFlow()) {
                    // remove our owned nodes from their manager, and remove their nodes from our manager.
                    // also make sure to finish split from their perspective.
                    new NetGroup<>(this.graph, this.net, targetGroup)
                            .setChannelManager(this.getChannelManager().subManager(this.nodes))
                            .finishSplit();
                    this.getChannelManager().removeNodes(targetGroup);
                } else {
                    new NetGroup<>(this.graph, this.net, targetGroup);
                }
            }
            this.finishSplit();
            return true;
        }
        return false;
    }

    private void prepSplit() {
        if (this.net.isFlow()) {
            this.getChannelManager().disconnectSuperNodes();
        }
    }

    private void finishSplit() {
        if (this.net.isFlow()) {
            this.getChannelManager().reconnectSuperNodes();
        }
    }

    /**
     * For memory considerations, returns the uncloned set. Do not modify this directly.
     */
    public Set<NodeG<PipeType, NodeDataType>> getNodes() {
        return nodes;
    }

    protected void clearCaches() {
        this.nodes.forEach(NodeG::clearPathCache);
    }

    public Graph<NodeG<PipeType, NodeDataType>, NetEdge> getGraph() {
        return graph;
    }

    public AbstractGroupData<PipeType, NodeDataType> getData() {
        return this.data;
    }

    public void setChannel(Object key, FlowChannel<PipeType, NodeDataType> channel) {
        this.getChannelManager().setChannel(key, channel);
    }

    @Nullable
    public FlowChannel<PipeType, NodeDataType> getChannel(Object key) {
        return this.getChannelManager().getChannel(key);
    }

    private NetGroup<PipeType, NodeDataType> setChannelManager(FlowChannelManager<PipeType, NodeDataType> manager) {
        this.channelManager = manager;
        return this;
    }

    private FlowChannelManager<PipeType, NodeDataType> getChannelManager() {
        if (this.channelManager == null) {
            this.channelManager = new FlowChannelManager<>((WorldPipeFlowNetG<NodeDataType, PipeType>) this.net);
        }
        return this.channelManager;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        int i = 0;
        for (NodeG<PipeType, NodeDataType> node : this.nodes) {
            tag.setLong(String.valueOf(i), node.getLongPos());
            if (this.net.isFlow()) {
                // should I do some weird, bit-based encoding with an adaptable number of longs instead of booleans?
                tag.setBoolean("F" + i, node.hasConnecteds());
            }
            i++;
        }
        tag.setInteger("NodeCount", i);
        return tag;
    }

    /**
     * Use {@link NBTBuilder} instead, this does nothing.
     */
    @Override
    @Deprecated
    public void deserializeNBT(NBTTagCompound nbt) {}

    private void addSinks(Set<NodeG<PipeType, NodeDataType>> sinks) {
        for (NodeG<PipeType, NodeDataType> sink : sinks) {
            this.getChannelManager().addSink(sink);
        }
    }

    static final class NBTBuilder<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
            NodeDataType extends INodeData<NodeDataType>> implements INBTBuilder {

        private final Set<NodeG<PipeType, NodeDataType>> nodes;
        private final Set<NodeG<PipeType, NodeDataType>> sinks;

        private final Graph<NodeG<PipeType, NodeDataType>, NetEdge> graph;
        private final WorldPipeNetG<NodeDataType, PipeType> net;

        NBTBuilder(Map<Long, NodeG<PipeType, NodeDataType>> longPosMap, NBTTagCompound tag, boolean isFlow,
                   Graph<NodeG<PipeType, NodeDataType>, NetEdge> graph, WorldPipeNetG<NodeDataType, PipeType> net) {
            nodes = new ObjectOpenHashSet<>();
            if (isFlow) sinks = new ObjectOpenHashSet<>();
            else sinks = null;
            for (int i = 0; i < tag.getInteger("NodeCount"); i++) {
                NodeG<PipeType, NodeDataType> node = longPosMap.get(tag.getLong(String.valueOf(i)));
                nodes.add(node);
                if (isFlow && tag.getBoolean("F" + i)) {
                    sinks.add(node);
                }
            }
            this.graph = graph;
            this.net = net;
        }

        @Override
        public void build() {
            NetGroup<PipeType, NodeDataType> g = new NetGroup<>(graph, net, nodes);
            if (net.isFlow()) g.addSinks(sinks);
        }
    }
}
