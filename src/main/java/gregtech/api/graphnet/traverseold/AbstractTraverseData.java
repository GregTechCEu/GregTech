package gregtech.api.graphnet.traverseold;

import gregtech.api.graphnet.net.IGraphNet;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.path.NetPath;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import org.jetbrains.annotations.Nullable;

public abstract class AbstractTraverseData<N extends NetNode, P extends NetPath<N, ?>> implements ITraverseData<N, P> {

    private final IGraphNet net;
    private final IPredicateTestObject testObject;
    private final SimulatorKey simulator;
    private final long queryTick;

    public AbstractTraverseData(IGraphNet net, IPredicateTestObject testObject, SimulatorKey simulator,
                                long queryTick) {
        this.net = net;
        this.testObject = testObject;
        this.simulator = simulator;
        this.queryTick = queryTick;
    }

    @Override
    public IGraphNet getGraphNet() {
        return net;
    }

    @Override
    public IPredicateTestObject getTestObject() {
        return testObject;
    }

    @Override
    public @Nullable SimulatorKey getSimulatorKey() {
        return simulator;
    }

    public boolean simulating() {
        return simulator != null;
    }

    @Override
    public long getQueryTick() {
        return queryTick;
    }
}
