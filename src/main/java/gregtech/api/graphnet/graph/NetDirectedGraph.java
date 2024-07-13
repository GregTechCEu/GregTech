package gregtech.api.graphnet.graph;

import gregtech.api.graphnet.GraphNetBacker;
import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.function.Function;
import java.util.function.Supplier;

public class NetDirectedGraph extends SimpleDirectedWeightedGraph<GraphVertex, GraphEdge> implements INetGraph {

    private GraphNetBacker net = null;

    private IPredicateTestObject testObject;
    private SimulatorKey simulator;
    private long queryTick;

    public NetDirectedGraph(Supplier<GraphVertex> vertexSupplier, Supplier<GraphEdge> edgeSupplier) {
        super(vertexSupplier, edgeSupplier);
    }

    @Override
    public void setOwningNet(GraphNetBacker net) {
        if (this.net != null)
            throw new IllegalStateException("Tried to set the owning net of an already initialized graph!");
        this.net = net;
    }

    @Override
    public void prepareForAlgorithmRun(IPredicateTestObject testObject, SimulatorKey simulator, long queryTick) {
        this.testObject = testObject;
        this.simulator = simulator;
        this.queryTick = queryTick;
    }

    @Override
    public boolean isDirected() {
        return true;
    }

    @Override
    public double getEdgeWeight(GraphEdge graphEdge) {
        if (!graphEdge.getSource().wrapped.traverse(queryTick, true) ||
                !graphEdge.getTarget().wrapped.traverse(queryTick, true))
            return Double.POSITIVE_INFINITY;

        if (graphEdge.wrapped.test(testObject)) {
            if (net.dynamicWeights()) {
                return graphEdge.wrapped.getDynamicWeight(testObject, simulator, queryTick, () -> super.getEdgeWeight(graphEdge));
            } else return super.getEdgeWeight(graphEdge);
        } else return Double.POSITIVE_INFINITY;
    }

    public static Function<IGraphNet, INetGraph> standardBuilder() {
        return iGraphNet -> new NetDirectedGraph(() -> new GraphVertex(iGraphNet.getNewNode()),
                () -> new GraphEdge(iGraphNet.getNewEdge()));
    }
}
