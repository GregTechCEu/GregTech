package gregtech.api.graphnet.graph;

import gregtech.api.graphnet.net.IGraphNet;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.function.Function;
import java.util.function.Supplier;

public class NetDirectedGraph extends SimpleDirectedWeightedGraph<GraphVertex, GraphEdge> implements INetGraph {

    public NetDirectedGraph(Supplier<GraphVertex> vertexSupplier, Supplier<GraphEdge> edgeSupplier) {
        super(vertexSupplier, edgeSupplier);
    }

    @Override
    public boolean isDirected() {
        return true;
    }

    public static Function<IGraphNet, INetGraph> standardBuilder() {
        return iGraphNet -> new NetDirectedGraph(() -> new GraphVertex(iGraphNet.getDefaultNodeType().getNew(iGraphNet)),
                () -> new GraphEdge(iGraphNet.getDefaultEdgeType().getNew(iGraphNet)));
    }
}
