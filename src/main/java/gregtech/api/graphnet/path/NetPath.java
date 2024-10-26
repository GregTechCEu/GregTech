package gregtech.api.graphnet.path;

import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.logic.NetLogicData;

import com.google.common.collect.ImmutableCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public interface NetPath {

    @NotNull
    @Unmodifiable
    <N extends NetNode> ImmutableCollection<N> getOrderedNodes();

    @NotNull
    default <N extends NetNode> N getSourceNode() {
        ImmutableCollection<NetNode> nodes = getOrderedNodes();
        return (N) nodes.asList().get(0);
    }

    @NotNull
    default <N extends NetNode> N getTargetNode() {
        ImmutableCollection<NetNode> nodes = getOrderedNodes();
        return (N) nodes.asList().get(nodes.size() - 1);
    }

    @NotNull
    @Unmodifiable
    <E extends NetEdge> ImmutableCollection<E> getOrderedEdges();

    double getWeight();

    @NotNull
    NetPath reversed();

    NetLogicData getUnifiedNodeData();

    @Nullable
    NetLogicData getUnifiedEdgeData();
}
