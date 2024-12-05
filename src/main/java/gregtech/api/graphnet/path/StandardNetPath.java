package gregtech.api.graphnet.path;

import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.net.NetNode;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class StandardNetPath implements NetPath {

    protected final ImmutableCollection<NetNode> nodes;

    protected final ImmutableCollection<NetEdge> edges;

    protected final double weight;

    protected final int hash;

    protected StandardNetPath reversed;

    protected NetLogicData unifiedNodeData;
    protected NetLogicData unifiedEdgeData;

    public StandardNetPath(@NotNull ImmutableCollection<NetNode> nodes, @NotNull ImmutableCollection<NetEdge> edges,
                           double weight) {
        this.nodes = nodes;
        this.edges = edges;
        this.weight = weight;
        ImmutableList<NetNode> listForm = nodes.asList();
        this.hash = Objects.hash(nodes, edges, weight, listForm.get(0), listForm.get(listForm.size() - 1));
    }

    public StandardNetPath(@NotNull StandardNetPath reverse) {
        ImmutableCollection.Builder<NetNode> builderNodes = reverse.nodes instanceof ImmutableSet ?
                ImmutableSet.builder() : ImmutableList.builder();
        List<NetNode> nodesList = reverse.nodes.asList();
        for (int i = nodesList.size(); i > 0; i--) {
            builderNodes.add(nodesList.get(i - 1));
        }
        ImmutableCollection.Builder<NetEdge> builderEdges = reverse.edges instanceof ImmutableSet ?
                ImmutableSet.builder() : ImmutableList.builder();
        List<NetEdge> edgesList = reverse.edges.asList();
        for (int i = edgesList.size(); i > 0; i--) {
            builderEdges.add(edgesList.get(i - 1));
        }
        this.nodes = builderNodes.build();
        this.edges = builderEdges.build();
        this.weight = reverse.weight;
        ImmutableList<NetNode> listForm = nodes.asList();
        this.hash = Objects.hash(nodes, edges, weight, listForm.get(0), listForm.get(listForm.size() - 1));
        this.reversed = reverse;
    }

    @Override
    public @NotNull @Unmodifiable ImmutableCollection<NetNode> getOrderedNodes() {
        return nodes;
    }

    @Override
    public @NotNull @Unmodifiable ImmutableCollection<NetEdge> getOrderedEdges() {
        return edges;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public NetLogicData getUnifiedNodeData() {
        if (unifiedNodeData == null) {
            ImmutableList<? extends NetNode> nodesList = nodes.asList();
            if (nodes.size() == 1) {
                unifiedNodeData = nodesList.get(0).getData();
            } else if (nodes.size() == 2) {
                unifiedNodeData = NetLogicData.union(nodesList.get(0).getData(), nodesList.get(1).getData());
            } else {
                unifiedNodeData = NetLogicData.union(nodesList.get(0).getData(),
                        nodes.stream().skip(1).map(NetNode::getData).toArray(NetLogicData[]::new));
            }
        }
        return unifiedNodeData;
    }

    @Override
    @Nullable
    public NetLogicData getUnifiedEdgeData() {
        if (unifiedEdgeData == null) {
            ImmutableList<? extends NetEdge> edgesList = edges.asList();
            if (edges.size() == 0) {
                return null;
            } else if (edges.size() == 1) {
                unifiedEdgeData = edgesList.get(0).getData();
            } else if (edges.size() == 2) {
                unifiedEdgeData = NetLogicData.union(edgesList.get(0).getData(), edgesList.get(1).getData());
            } else {
                unifiedEdgeData = NetLogicData.union(edgesList.get(0).getData(),
                        edges.stream().skip(1).map(NetEdge::getData).toArray(NetLogicData[]::new));
            }
        }
        return unifiedEdgeData;
    }

    @Override
    public @NotNull StandardNetPath reversed() {
        if (reversed == null) {
            reversed = new StandardNetPath(this);
        }
        return reversed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StandardNetPath that = (StandardNetPath) o;
        return Double.compare(that.weight, weight) == 0 && that.getSourceNode() == getSourceNode() &&
                that.getTargetNode() == getTargetNode() && Objects.equals(nodes, that.nodes) &&
                Objects.equals(edges, that.edges);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    public static final class Builder implements PathBuilder {

        public final List<NetNode> nodes = new ObjectArrayList<>();
        public final List<NetEdge> edges = new ObjectArrayList<>();

        public Builder(@NotNull NetNode startingNode) {
            nodes.add(startingNode);
        }

        @Override
        @Contract("_, _ -> this")
        public Builder addToEnd(@NotNull NetNode node, @NotNull NetEdge edge) {
            NetNode end = nodes.get(nodes.size() - 1);
            if (edge.getOppositeNode(node) != end)
                throw new IllegalArgumentException("Edge does not link last node and new node!");
            nodes.add(node);
            edges.add(edge);
            return this;
        }

        @Override
        @Contract("_, _ -> this")
        public Builder addToStart(@NotNull NetNode node, @NotNull NetEdge edge) {
            NetNode end = nodes.get(0);
            if (edge.getOppositeNode(node) != end)
                throw new IllegalArgumentException("Edge does not link last node and new node!");
            nodes.add(0, node);
            edges.add(0, edge);
            return this;
        }

        @Override
        @Contract("-> this")
        public Builder reverse() {
            Collections.reverse(nodes);
            Collections.reverse(edges);
            return this;
        }

        @Override
        public StandardNetPath build() {
            return new StandardNetPath(ImmutableSet.copyOf(nodes), ImmutableSet.copyOf(edges),
                    edges.stream().mapToDouble(NetEdge::getWeight).sum());
        }
    }
}
