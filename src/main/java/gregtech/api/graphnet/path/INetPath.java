package gregtech.api.graphnet.path;

import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.logic.NetLogicData;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface INetPath<N extends NetNode, T extends NetEdge> {
    List<N> getOrderedNodes();

    List<T> getOrderedEdges();

    double getWeight();

    default boolean matches(INetPath<?, ?> other) {
        return getWeight() == other.getWeight() &&
                getOrderedNodes().equals(other.getOrderedNodes()) && getOrderedEdges().equals(other.getOrderedEdges());
    }

    NetLogicData getUnifiedNodeData();

    @Nullable
    NetLogicData getUnifiedEdgeData();
}
