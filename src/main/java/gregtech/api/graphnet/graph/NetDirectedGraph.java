package gregtech.api.graphnet.graph;

import gregtech.api.graphnet.GraphNetBacker;
import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

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
        return iGraphNet -> new NetDirectedGraph(() -> new GraphVertex(iGraphNet.getNewNode()),
                () -> new GraphEdge(iGraphNet.getNewEdge()));
    }
}
