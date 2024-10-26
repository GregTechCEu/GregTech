package gregtech.api.graphnet.graph;

import gregtech.api.graphnet.GraphNetBacker;
import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.function.Function;
import java.util.function.Supplier;

public class NetUndirectedGraph extends SimpleWeightedGraph<GraphVertex, GraphEdge> implements INetGraph {

    public NetUndirectedGraph(Supplier<GraphVertex> vertexSupplier, Supplier<GraphEdge> edgeSupplier) {
        super(vertexSupplier, edgeSupplier);
    }

    @Override
    public boolean isDirected() {
        return false;
    }

    public static Function<IGraphNet, INetGraph> standardBuilder() {
        return iGraphNet -> new NetUndirectedGraph(() -> new GraphVertex(iGraphNet.getNewNode()),
                () -> new GraphEdge(iGraphNet.getNewEdge()));
    }
}
