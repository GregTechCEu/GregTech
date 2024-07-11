package gregtech.api.graphnet.path;

import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.edge.NetEdge;

import java.util.List;

public abstract class AbstractNetPath<N extends NetNode, E extends NetEdge> implements INetPath<N, E> {

    protected final List<N> nodes;

    protected final List<E> edges;

    protected final double weight;

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
}
