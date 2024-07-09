package gregtech.api.pipenet.graph;

import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetNode;
import gregtech.api.pipenet.WorldPipeNetBase;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.edge.NetEdge;
import gregtech.api.pipenet.edge.SimulatorKey;
import gregtech.api.pipenet.predicate.IPredicateTestObject;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.function.Supplier;

public class CustomDirectedGraph<PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>,
        E extends NetEdge> extends SimpleDirectedWeightedGraph<NetNode<PT, NDT, E>, E>
                                implements ICustomGraph<PT, NDT, E> {

    private WorldPipeNetBase<NDT, PT, E> net = null;

    private IPredicateTestObject testObject;
    private SimulatorKey simulator;
    private long queryTick;

    @Override
    public void setOwningNet(WorldPipeNetBase<NDT, PT, E> net) {
        if (this.net != null)
            throw new IllegalStateException("Tried to set the owning net of an already initialized graph!");
        this.net = net;
    }

    @Override
    public void prepareForDynamicWeightAlgorithmRun(IPredicateTestObject testObject, SimulatorKey simulator,
                                                    long queryTick) {
        if (!net.usesDynamicWeights()) throw new IllegalStateException("Net does not support dynamic weights!");
        this.testObject = testObject;
        this.simulator = simulator;
        this.queryTick = queryTick;
    }

    public CustomDirectedGraph(Class<? extends E> edgeClass) {
        super(edgeClass);
    }

    public CustomDirectedGraph(Supplier<NetNode<PT, NDT, E>> vertexSupplier, Supplier<E> edgeSupplier) {
        super(vertexSupplier, edgeSupplier);
    }

    @Override
    public double getEdgeWeight(E edge) {
        if (net.usesDynamicWeights()) {
            return edge.getPredicate().test(testObject) ? edge.getDynamicWeight(testObject, simulator, queryTick) :
                    Double.POSITIVE_INFINITY;
        } else return super.getEdgeWeight(edge);
    }
}
