package gregtech.api.graphnet.path;

import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.logic.NetLogicData;

import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public abstract class AbstractNetPath<N extends NetNode, E extends NetEdge> implements INetPath<N, E> {

    protected final List<N> nodes;

    protected final List<E> edges;

    protected final double weight;

    protected NetLogicData unifiedNodeData;
    protected NetLogicData unifiedEdgeData;

    public AbstractNetPath(List<N> nodes, List<E> edges, double weight) {
        this.nodes = nodes;
        this.edges = edges;
        this.weight = weight;
    }

    @Override
    public List<N> getOrderedNodes() {
        return nodes;
    }

    @Override
    public List<E> getOrderedEdges() {
        return edges;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public NetLogicData getUnifiedNodeData() {
        if (unifiedNodeData == null) {
            if (nodes.size() == 1) {
                unifiedNodeData = nodes.get(0).getData();
            } else if (nodes.size() == 2) {
                unifiedNodeData = NetLogicData.union(nodes.get(0).getData(), nodes.get(1).getData());
            } else {
                unifiedNodeData = NetLogicData.union(nodes.get(0).getData(),
                        nodes.subList(1, nodes.size()).stream().map(NetNode::getData).toArray(NetLogicData[]::new));
            }
        }
        return unifiedNodeData;
    }

    @Override
    @Nullable
    public NetLogicData getUnifiedEdgeData() {
        if (unifiedEdgeData == null) {
            if (edges.size() == 0) {
                return null;
            } else if (edges.size() == 1) {
                unifiedEdgeData = edges.get(0).getData();
            } else if (edges.size() == 2) {
                unifiedEdgeData = NetLogicData.union(edges.get(0).getData(), edges.get(1).getData());
            } else {
                unifiedEdgeData = NetLogicData.union(edges.get(0).getData(),
                        edges.subList(1, edges.size()).stream().map(NetEdge::getData).toArray(NetLogicData[]::new));
            }
        }
        return unifiedEdgeData;
    }
}
