package gregtech.api.graphnet.alg;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.alg.iter.IteratorFactory;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.graph.GraphVertex;
import gregtech.api.graphnet.path.NetPath;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NetAlgorithmWrapper {

    private final IGraphNet net;
    private final boolean recomputeEveryCall;
    @Nullable
    private INetAlgorithm alg;

    private final AlgorithmBuilder builder;

    public NetAlgorithmWrapper(IGraphNet net, @NotNull AlgorithmBuilder builder, boolean recomputeEveryCall) {
        this.net = net;
        this.builder = builder;
        this.recomputeEveryCall = recomputeEveryCall;
    }

    public IGraphNet getNet() {
        return net;
    }

    public void invalidate() {
        this.alg = null;
    }

    public <Path extends NetPath<?, ?>> IteratorFactory<Path> getPathsIterator(GraphVertex source,
                                                                               NetPathMapper<Path> remapper,
                                                                               IPredicateTestObject testObject,
                                                                               @Nullable SimulatorKey simulator,
                                                                               long queryTick) {
        if (!recomputeEveryCall) net.getGraph().prepareForAlgorithmRun(testObject, simulator, queryTick);
        if (alg == null) alg = builder.build(net, recomputeEveryCall);
        return alg.getPathsIteratorFactory(source, remapper);
    }
}
