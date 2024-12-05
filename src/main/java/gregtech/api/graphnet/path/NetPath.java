package gregtech.api.graphnet.path;

import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.net.NetNode;

import com.google.common.collect.ImmutableCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public interface NetPath {

    @NotNull
    @Unmodifiable
    ImmutableCollection<NetNode> getOrderedNodes();

    @NotNull
    default NetNode getSourceNode() {
        ImmutableCollection<NetNode> nodes = getOrderedNodes();
        return nodes.asList().get(0);
    }

    @NotNull
    default NetNode getTargetNode() {
        ImmutableCollection<NetNode> nodes = getOrderedNodes();
        return nodes.asList().get(nodes.size() - 1);
    }

    @NotNull
    @Unmodifiable
    ImmutableCollection<NetEdge> getOrderedEdges();

    double getWeight();

    @NotNull
    NetPath reversed();

    NetLogicData getUnifiedNodeData();

    @Nullable
    NetLogicData getUnifiedEdgeData();
}
