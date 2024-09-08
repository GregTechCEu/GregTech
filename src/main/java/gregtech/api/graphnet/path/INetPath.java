package gregtech.api.graphnet.path;

import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.logic.NetLogicData;

import org.jetbrains.annotations.Nullable;
import org.jgrapht.alg.util.ToleranceDoubleComparator;

import java.util.List;

public interface INetPath<N extends NetNode, T extends NetEdge> {

    ToleranceDoubleComparator WEIGHT_COMPARATOR = new ToleranceDoubleComparator();

    List<N> getOrderedNodes();

    default N getSourceNode() {
        return getOrderedNodes().get(0);
    }

    default N getTargetNode() {
        List<N> nodes = getOrderedNodes();
        return nodes.get(nodes.size() - 1);
    }

    List<T> getOrderedEdges();

    double getWeight();

    NetLogicData getUnifiedNodeData();

    @Nullable
    NetLogicData getUnifiedEdgeData();
}