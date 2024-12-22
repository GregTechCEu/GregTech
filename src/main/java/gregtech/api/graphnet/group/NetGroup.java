package gregtech.api.graphnet.group;

import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.net.IGraphNet;
import gregtech.api.graphnet.net.NetEdge;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.traverse.iter.EdgeDirection;
import gregtech.api.graphnet.traverse.iter.NetBreadthIterator;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.jgrapht.Graphs;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class NetGroup {

    public final IGraphNet net;

    final @NotNull Set<NetNode> nodes;

    private final @NotNull Int2ObjectMap<Set<NetNode>> sortingNodes;

    private @Nullable GroupData data;

    private GroupGraphView graphView;

    public NetGroup(IGraphNet net) {
        this(net, new ObjectOpenHashSet<>(), new Int2ObjectOpenHashMap<>());
    }

    public NetGroup(@NotNull IGraphNet net, @NotNull Set<NetNode> nodes) {
        this(net, nodes, net.getBlankGroupData());
    }

    public NetGroup(@NotNull IGraphNet net, @NotNull Set<NetNode> nodes, @Nullable GroupData data) {
        this(net, nodes, new Int2ObjectOpenHashMap<>(), data);
        for (NetNode node : nodes) {
            initialSort(node);
        }
    }

    public NetGroup(@NotNull IGraphNet net, @NotNull Set<NetNode> nodes,
                    @NotNull Int2ObjectMap<Set<NetNode>> sortingNodes) {
        this(net, nodes, sortingNodes, net.getBlankGroupData());
    }

    public NetGroup(@NotNull IGraphNet net, @NotNull Set<NetNode> nodes,
                    @NotNull Int2ObjectMap<Set<NetNode>> sortingNodes, @Nullable GroupData data) {
        this.net = net;
        this.data = data;
        if (data != null) data.withGroup(this);
        this.nodes = nodes;
        this.sortingNodes = sortingNodes;
        nodes.forEach(this::onAddedToGroup);
    }

    private void initialSort(NetNode node) {
        int key = node.getSortingKey();
        Set<NetNode> s = this.sortingNodes.get(key);
        if (s == null) this.sortingNodes.put(key, s = new ObjectOpenHashSet<>());
        s.add(node);
    }

    public void addNode(NetNode node) {
        this.nodes.add(node);
        onAddedToGroup(node);
        initialSort(node);
    }

    private void addNodes(Collection<NetNode> nodes) {
        this.nodes.addAll(nodes);
        for (NetNode node : nodes) {
            onAddedToGroup(node);
            initialSort(node);
        }
    }

    @ApiStatus.Internal
    public void removeNode(NetNode node) {
        this.nodes.remove(node);
    }

    private void removeNodes(Collection<NetNode> nodes) {
        this.nodes.removeAll(nodes);
    }

    private void clearNodes() {
        this.nodes.clear();
    }

    private void onAddedToGroup(@NotNull NetNode node) {
        node.setGroup(this);
    }

    public void notifySortingChange(NetNode node, int oldKey, int newKey) {
        Set<NetNode> old = this.sortingNodes.get(oldKey);
        if (old != null) {
            old.remove(node);
            if (old.size() == 0) this.sortingNodes.remove(oldKey);
        }
        Set<NetNode> n = this.sortingNodes.get(newKey);
        if (n == null) this.sortingNodes.put(newKey, n = new ObjectOpenHashSet<>());
        n.add(node);
    }

    public static MergeDirection isEdgeAllowed(@NotNull NetNode source, @NotNull NetNode target) {
        NetGroup sourceGroup = source.getGroupUnsafe();
        NetGroup targetGroup = target.getGroupUnsafe();

        if (sourceGroup == null || targetGroup == null || sourceGroup == targetGroup) return MergeDirection.NULL;
        return GroupData.mergeAllowed(sourceGroup.getData(), targetGroup.getData());
    }

    /**
     * Merges the groups on either side of an edge if necessary.
     * 
     * @param edge the edge to merge across
     */
    public static void mergeEdge(@NotNull NetEdge edge, @NotNull MergeDirection direction) {
        NetNode source = edge.getSource();
        NetNode target = edge.getTarget();
        assert source != null;
        assert target != null;
        NetGroup sourceGroup = source.getGroupUnsafe();
        NetGroup targetGroup = target.getGroupUnsafe();
        if (sourceGroup == targetGroup) {
            if (sourceGroup == null) {
                sourceGroup = source.getGroupSafe();
            } else {
                GroupData data = sourceGroup.getData();
                if (data != null) data.notifyOfBridgingEdge(edge);
                return;
            }
        }
        if (sourceGroup != null) {
            sourceGroup.mergeNode(target, edge, direction.source());
        } else {
            targetGroup.mergeNode(source, edge, direction.target());
        }
    }

    private void mergeNode(@NotNull NetNode node, @NotNull NetEdge edge, boolean dataMergeTo) {
        NetGroup group = node.getGroupUnsafe();
        if (group != null) {
            this.addNodes(group.getNodes());
            GroupData data = group.getData();
            if (data != null) {
                if (this.data == null) this.data = data;
                else this.data = dataMergeTo ? this.data.mergeAcross(data, edge) : data.mergeAcross(this.data, edge);
            }
        } else addNode(node);
    }

    /**
     * Split this group by removing a node. Automatically removes the node from the backing graph.
     * 
     * @param source node to remove
     */
    public void splitNode(NetNode source) {
        if (!this.net.containsNode(source)) return;
        Stream<GraphEdge> stream = this.net.getGraph().edgesOf(source.wrapper).stream();
        GroupData data = getData();
        if (data != null) stream = stream.peek(e -> data.notifyOfRemovedEdge(e.wrapped));
        ObjectLinkedOpenHashSet<NetNode> targets = stream
                .map(a -> Graphs.getOppositeVertex(net.getGraph(), a, source.wrapper).getWrapped())
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ObjectLinkedOpenHashSet::new));
        this.net.getBacker().removeVertex(source.wrapper);
        this.removeNode(source);
        while (!targets.isEmpty()) {
            NetNode target = targets.removeLast();
            Set<NetNode> targetGroup = new ObjectOpenHashSet<>();
            NetBreadthIterator i = new NetBreadthIterator(target, EdgeDirection.ALL);
            NetNode temp;
            while (i.hasNext()) {
                temp = i.next();
                if (temp == source) continue;
                targetGroup.add(temp);
                // if we find a target node in our search, remove it from the list
                targets.remove(temp);
            }
            this.removeNodes(targetGroup);
            if (!targetGroup.isEmpty()) {
                new NetGroup(this.net, targetGroup);
            }
        }
    }

    /**
     * Split this group by removing an edge. Automatically removes the edge from the graph.
     * 
     * @param source source of the edge
     * @param target target of the edge
     * @return Whether the edge existed in the graph
     */
    public boolean splitEdge(@NotNull NetNode source, @NotNull NetNode target) {
        GroupData data = getData();
        NetEdge edge = this.net.getEdge(source, target);
        if (edge == null) return false;
        if (data != null) data.notifyOfRemovedEdge(edge);
        if (this.net.getBacker().removeEdge(source.wrapper, target.wrapper) != null) {
            Set<NetNode> targetGroup = new ObjectOpenHashSet<>();
            NetBreadthIterator i = new NetBreadthIterator(target, EdgeDirection.ALL);
            NetNode temp;
            while (i.hasNext()) {
                temp = i.next();
                // if there's another complete path to the source node from the target node, there's no need to split
                if (source == temp) return true;
                targetGroup.add(temp);
            }
            this.removeNodes(targetGroup);
            if (targetGroup.size() != 0) {
                if (data == null) new NetGroup(this.net, targetGroup);
                else {
                    Pair<GroupData, GroupData> split = data.splitAcross(this.nodes, targetGroup);
                    this.data = split.getLeft();
                    new NetGroup(this.net, targetGroup, split.getRight());
                }
            }
            return true;
        }
        return false;
    }

    @NotNull
    @UnmodifiableView
    public Set<NetNode> getNodes() {
        return nodes;
    }

    @NotNull
    @UnmodifiableView
    public Set<NetNode> getNodesUnderKey(int key) {
        Set<NetNode> set = sortingNodes.get(key);
        return set == null ? Collections.emptySet() : set;
    }

    @NotNull
    @UnmodifiableView
    public Int2ObjectMap<Set<NetNode>> getSortingNodes() {
        return sortingNodes;
    }

    public @Nullable GroupData getData() {
        return this.data;
    }

    public GroupGraphView getGraphView() {
        if (graphView == null) graphView = new GroupGraphView(this);
        return graphView;
    }
}
