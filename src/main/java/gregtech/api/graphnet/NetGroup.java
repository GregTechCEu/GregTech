package gregtech.api.graphnet;

import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.graph.GraphVertex;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class NetGroup {

    public final IGraphNet net;

    private final Set<NetNode> nodes;

    private final AbstractGroupData data;

    public NetGroup(IGraphNet net) {
        this(net, new ObjectOpenHashSet<>());
    }

    public NetGroup(IGraphNet net,
                    Set<NetNode> nodes) {
        this.net = net;
        this.data = net.getBlankGroupData();
        if (data != null) data.withGroup(this);
        this.nodes = nodes;
        nodes.forEach(this::onAddedToGroup);
    }

    public void addNode(NetNode node) {
        this.nodes.add(node);
        this.onAddedToGroup(node);
    }

    protected void addNodes(Collection<NetNode> nodes) {
        this.nodes.addAll(nodes);
        nodes.forEach(this::onAddedToGroup);
    }

    protected void removeNode(NetNode node) {
        this.nodes.remove(node);
    }

    protected void removeNodes(Collection<NetNode> nodes) {
        this.nodes.removeAll(nodes);
    }

    protected void clearNodes() {
        this.nodes.clear();
    }

    protected void onAddedToGroup(NetNode node) {
        node.setGroup(this);
    }

    /**
     * Merges the groups of an edge if necessary.
     * 
     * @param source the source node of the edge
     * @param target the target node of the edge
     */
    public static void mergeEdge(NetNode source, NetNode target) {
        NetGroup sourceGroup = source.getGroupUnsafe();
        NetGroup targetGroup = target.getGroupUnsafe();
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

    protected void mergeNode(NetNode node) {
        NetGroup group = node.getGroupUnsafe();
        if (group != null) {
            this.addNodes(group.getNodes());
            group.clearNodes();
        } else addNode(node);
        this.clearPathCaches();
    }

    /**
     * Split this group by removing a node. Automatically removes the node from the backing graph.
     * 
     * @param source node to remove
     */
    public void splitNode(NetNode source) {
        if (!this.net.containsNode(source)) return;
        this.clearPathCaches();
        List<NetNode> targets = this.net.getGraph().outgoingEdgesOf(source.wrapper).stream().map(a -> {
            // handling so undirected graphs don't throw an error
            if (net.getGraph().isDirected() || Objects.equals(getTarget(a).wrapped, source)) return getTarget(a).wrapped;
            return getSource(a).wrapped;
        }).collect(Collectors.toList());
        this.net.getBacker().removeVertex(source.wrapper);
        this.removeNode(source);
        while (!targets.isEmpty()) {
            NetNode target = targets.remove(targets.size() - 1);

            Set<NetNode> targetGroup = new ObjectOpenHashSet<>();
            Iterator<NetNode> i = this.net.breadthIterator(target);
            NetNode temp;
            while (i.hasNext()) {
                temp = i.next();
                targetGroup.add(temp);
                // if we find a target node in our search, remove it from the list
                targets.remove(temp);
            }
            this.removeNodes(targetGroup);
            if (targetGroup.size() != 0) {
                new NetGroup(this.net, targetGroup);
            }
        }
    }

    private GraphVertex getSource(GraphEdge graphEdge) {
        return this.net.getGraph().getEdgeSource(graphEdge);
    }

    private GraphVertex getTarget(GraphEdge graphEdge) {
        return this.net.getGraph().getEdgeTarget(graphEdge);
    }

    /**
     * Split this group by removing an edge. Automatically removes the edge from the graph.
     * 
     * @param source source of the edge
     * @param target target of the edge
     * @return Whether the edge existed in the graph
     */
    public boolean splitEdge(NetNode source, NetNode target) {
        if (this.net.getBacker().removeEdge(source.wrapper, target.wrapper) != null) {
            this.clearPathCaches();
            Set<NetNode> targetGroup = new ObjectOpenHashSet<>();
            Iterator<NetNode> i = this.net.breadthIterator(target);
            NetNode temp;
            while (i.hasNext()) {
                temp = i.next();
                // if there's another complete path to the source node from the target node, there's no need to split
                if (source == temp) return true;
                targetGroup.add(temp);
            }
            this.removeNodes(targetGroup);
            if (targetGroup.size() != 0) {
                new NetGroup(this.net, targetGroup);
            }
            return true;
        }
        return false;
    }

    /**
     * For memory considerations, returns the uncloned set. Do not modify this directly.
     */
    public Set<NetNode> getNodes() {
        return nodes;
    }

    public void clearPathCaches() {
        this.getNodes().forEach(NetNode::clearPathCache);
    }

    public AbstractGroupData getData() {
        return this.data;
    }
}
