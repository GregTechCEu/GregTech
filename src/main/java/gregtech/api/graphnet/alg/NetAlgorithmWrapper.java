package gregtech.api.graphnet.alg;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.alg.iter.IteratorFactory;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.graph.GraphVertex;
import gregtech.api.graphnet.path.INetPath;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Function;

public class NetAlgorithmWrapper {

    private final IGraphNet net;
    @Nullable
    private INetAlgorithm alg;

    private final Function<IGraphNet, @NotNull INetAlgorithm> builder;

    public NetAlgorithmWrapper(IGraphNet net, @NotNull Function<IGraphNet, @NotNull INetAlgorithm> builder) {
        this.net = net;
        this.builder = builder;
    }

    public IGraphNet getNet() {
        return net;
    }

    public void invalidate() {
        this.alg = null;
    }

    public <Path extends INetPath<?, ?>> IteratorFactory<Path> getPathsIterator(GraphVertex source,
                                                                                NetPathMapper<Path> remapper) {
        if (alg == null) alg = builder.apply(net);
        return alg.getPathsIteratorFactory(source, remapper);
    }
}
