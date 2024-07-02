package gregtech.api.pipenet;

import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.edge.NetEdge;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jgrapht.Graph;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NetGroup<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
        NodeDataType extends INodeData<NodeDataType>,
        Edge extends NetEdge> implements INBTSerializable<NBTTagCompound> {

    public final WorldPipeNetBase<NodeDataType, PipeType, Edge> net;

    private final Graph<NetNode<PipeType, NodeDataType, Edge>, Edge> graph;

    private final Set<NetNode<PipeType, NodeDataType, Edge>> nodes;

    private final AbstractGroupData<PipeType, NodeDataType> data;

    public NetGroup(Graph<NetNode<PipeType, NodeDataType, Edge>, Edge> graph,
                    WorldPipeNetBase<NodeDataType, PipeType, Edge> net) {
        this(graph, net, new ObjectOpenHashSet<>());
    }

    public NetGroup(Graph<NetNode<PipeType, NodeDataType, Edge>, Edge> graph,
                    WorldPipeNetBase<NodeDataType, PipeType, Edge> net,
                    Set<NetNode<PipeType, NodeDataType, Edge>> nodes) {
        this.graph = graph;
        this.net = net;
        this.data = net.getBlankGroupData();
        if (data != null) data.withGroup(this);
        this.nodes = nodes;
        nodes.forEach(this::onAddedToGroup);
    }

    protected void addNode(NetNode<PipeType, NodeDataType, Edge> node) {
        this.nodes.add(node);
        this.onAddedToGroup(node);
    }

    protected void addNodes(Collection<NetNode<PipeType, NodeDataType, Edge>> nodes) {
        this.nodes.addAll(nodes);
        nodes.forEach(this::onAddedToGroup);
    }

    protected void removeNode(NetNode<PipeType, NodeDataType, Edge> node) {
        this.nodes.remove(node);
    }

    protected void removeNodes(Collection<NetNode<PipeType, NodeDataType, Edge>> nodes) {
        this.nodes.removeAll(nodes);
    }

    protected void clearNodes() {
        this.nodes.clear();
    }

    protected void onAddedToGroup(NetNode<PipeType, NodeDataType, Edge> node) {
        node.setGroup(this);
    }

    public void connectionChange(NetNode<PipeType, NodeDataType, Edge> node) {
        // TODO simplify path search by only checking nodes that have connections
        // use net's connection capabilities
    }

    /**
     * Merges the groups of an edge if necessary.
     * 
     * @param source the source node of the edge
     * @param target the target node of the edge
     */
    public static <PT extends Enum<PT> & IPipeType<NDT>,
            NDT extends INodeData<NDT>, E extends NetEdge> void mergeEdge(
                                                                          NetNode<PT, NDT, E> source,
                                                                          NetNode<PT, NDT, E> target) {
        NetGroup<PT, NDT, E> sourceGroup = source.getGroupUnsafe();
        NetGroup<PT, NDT, E> targetGroup = target.getGroupUnsafe();
        if (sourceGroup == targetGroup) {
            if (sourceGroup == null) {
                sourceGroup = source.getGroupSafe();
            } else {
                sourceGroup.clearPathCaches();
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

    protected void mergeNode(NetNode<PipeType, NodeDataType, Edge> node) {
        NetGroup<PipeType, NodeDataType, Edge> group = node.getGroupUnsafe();
        if (group != null) {
            this.addNodes(group.getNodes());
            group.clearNodes();
        } else addNode(node);
        this.clearPathCaches();
    }

    /**
     * Split this group by removing a node. Automatically removes the node from the graph.
     * 
     * @param source node to remove
     * @return Whether the node existed in the graph
     */
    public boolean splitNode(NetNode<PipeType, NodeDataType, Edge> source) {
        if (this.graph.containsVertex(source)) {
            this.clearPathCaches();
            List<NetNode<?, ?, ?>> targets = graph.outgoingEdgesOf(source).stream().map(a -> {
                // handling so undirected graphs don't throw an error
                if (net.isDirected() || a.getTarget().getNodePos() != source.getNodePos()) return a.getTarget();
                return a.getSource();
            }).collect(Collectors.toList());
            this.graph.removeVertex(source);
            this.removeNode(source);
            while (!targets.isEmpty()) {
                // get the last target; if this throws a cast exception, something is very wrong with the graph.
                @SuppressWarnings("unchecked")
                NetNode<PipeType, NodeDataType, Edge> target = (NetNode<PipeType, NodeDataType, Edge>) targets
                        .remove(targets.size() - 1);

                Set<NetNode<PipeType, NodeDataType, Edge>> targetGroup = new ObjectOpenHashSet<>();
                BreadthFirstIterator<NetNode<PipeType, NodeDataType, Edge>, Edge> i = new BreadthFirstIterator<>(graph,
                        target);
                NetNode<PipeType, NodeDataType, Edge> temp;
                while (i.hasNext()) {
                    temp = i.next();
                    targetGroup.add(temp);
                    // if we find a target node in our search, remove it from the list
                    targets.remove(temp);
                }
                this.removeNodes(targetGroup);
                if (targetGroup.size() != 0) {
                    new NetGroup<>(this.graph, this.net, targetGroup);
                }
            }
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
    public boolean splitEdge(NetNode<PipeType, NodeDataType, Edge> source,
                             NetNode<PipeType, NodeDataType, Edge> target) {
        if (graph.removeEdge(source, target) != null) {
            this.clearPathCaches();
            Set<NetNode<PipeType, NodeDataType, Edge>> targetGroup = new ObjectOpenHashSet<>();
            BreadthFirstIterator<NetNode<PipeType, NodeDataType, Edge>, Edge> i = new BreadthFirstIterator<>(graph,
                    target);
            NetNode<PipeType, NodeDataType, Edge> temp;
            while (i.hasNext()) {
                temp = i.next();
                // if there's a another complete path to the source node from the target node, there's no need to split
                if (source == temp) return true;
                targetGroup.add(temp);
            }
            this.removeNodes(targetGroup);
            if (targetGroup.size() != 0) {
                new NetGroup<>(this.graph, this.net, targetGroup);
            }
            return true;
        }
        return false;
    }

    /**
     * For memory considerations, returns the uncloned set. Do not modify this directly.
     */
    public Set<NetNode<PipeType, NodeDataType, Edge>> getNodes() {
        return nodes;
    }

    protected void clearPathCaches() {
        this.getNodes().forEach(NetNode::clearPathCache);
    }

    public Graph<NetNode<PipeType, NodeDataType, Edge>, Edge> getGraph() {
        return graph;
    }

    public AbstractGroupData<PipeType, NodeDataType> getData() {
        return this.data;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        int i = 0;
        for (NetNode<PipeType, NodeDataType, Edge> node : this.nodes) {
            tag.setLong(String.valueOf(i), node.getLongPos());
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

    public static final class NBTBuilder<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
            NodeDataType extends INodeData<NodeDataType>, E extends NetEdge> implements INBTBuilder {

        private final Set<NetNode<PipeType, NodeDataType, E>> nodes;
        private final Graph<NetNode<PipeType, NodeDataType, E>, E> graph;
        private final WorldPipeNetBase<NodeDataType, PipeType, E> net;

        public NBTBuilder(Map<Long, NetNode<PipeType, NodeDataType, E>> longPosMap, NBTTagCompound tag,
                          Graph<NetNode<PipeType, NodeDataType, E>, E> graph,
                          WorldPipeNetBase<NodeDataType, PipeType, E> net) {
            nodes = new ObjectOpenHashSet<>();
            for (int i = 0; i < tag.getInteger("NodeCount"); i++) {
                NetNode<PipeType, NodeDataType, E> node = longPosMap.get(tag.getLong(String.valueOf(i)));
                nodes.add(node);
            }
            this.graph = graph;
            this.net = net;
        }

        @Override
        public void build() {
            NetGroup<PipeType, NodeDataType, E> g = new NetGroup<>(graph, net, nodes);
        }
    }
}
